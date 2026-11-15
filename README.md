# WebBlocks Owner (Android)

The WebBlock owner app for Android: website owners sign in, open one of their live websites and manage its
**Products**, **Business** information and **Monitor** (deploys, uptime, plan usage), and read their notifications.
It talks to the WebBlock **v2 API**. Accounts are created on the WebBlock website; the app never creates one.

It is built on the KSHRD Android starter (Kotlin, Jetpack Compose, Clean Architecture + MVVM, multi-module).

- Min SDK 26, Target SDK 36, compile SDK 37
- Kotlin 2.4, AGP 9.2, Gradle 9.4, Jetpack Compose (Material 3), Hilt, Retrofit + kotlinx.serialization,
  Navigation Compose (typed routes)
- Application id: `com.kshrd.starter` (the app is named "WebBlocks Owner" on the device)

## Configuration

The app has no `.env` file. At **build** time `:core/build.gradle.kts` reads `local.properties` (git-ignored, at the
repository root) into `BuildConfig`. Start from [`local.properties.example`](local.properties.example): copy it to
`local.properties` and change what you need. A missing key uses its default.

| Key | Default | Meaning |
|-----|---------|---------|
| `sdk.dir` | set by Android Studio | Path to your Android SDK. |
| `api.base.url` | `https://wb-api.roa-dev.digital/` | Base URL of the WebBlock v2 API. It **must end with a slash**. |
| `use.mock.data` | `false` | `false`: every screen uses the v2 API. `true`: every screen uses the in-memory sample data, with no network (see [Mock mode](#mock-mode)). |
| `auth.realm.url` | `https://wb-auth.roa-dev.digital/realms/webblock` | Keycloak realm for "Continue with Google". |
| `google.sign.in` | `true` | Shows "Continue with Google" with the v2 API. In mock mode it has no effect: the button always opens a sample chooser. |
| `sign.up.url` | empty | Reserved for the web app's sign-up page. Not used yet: no screen reads it, so the value has no effect today. |

The values are compiled into the app, so **a change needs a rebuild** (Build > Rebuild, or `./gradlew assembleDebug`)
and a reinstall.

## Get started

1. Open the folder in Android Studio. It creates `local.properties` with your SDK path; if it does not, copy
   `local.properties.example` to `local.properties` and set `sdk.dir`.
2. Run the `app` configuration on an emulator or a device. By default the app uses the v2 API on AWS; sign in with
   your own WebBlock owner account (see [Running against AWS](#running-against-aws)).
3. For UI work without a server or an account, switch to [mock mode](#mock-mode).
4. Read, in this order:
   - [`docs/API-MAPPING.md`](docs/API-MAPPING.md): every app call and the v2 route that serves it.
   - [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md): the module graph and the rules.
   - [`docs/presentation-conventions.md`](docs/presentation-conventions.md): the screen checklist.
   - [`docs/ADDING_A_FEATURE.md`](docs/ADDING_A_FEATURE.md): how to add a feature.
   - [`AGENTS.md`](AGENTS.md): the same rules, condensed, for AI coding assistants.

## The v2 API

Each screen's repository has two implementations: the v2 API (`features/.../remote/repository/Api*Repository.kt`,
the default) and the in-memory sample data (`*RepositoryImpl` with `features/**/data/source/InMemory*DataSource.kt`).
`use.mock.data` picks one for the whole app (`features/di/FeatureRepositoryModule.kt`).

- Calls, request and answer shapes, and every rule the app follows (tokens, errors, limits, photos) are in
  [`docs/API-MAPPING.md`](docs/API-MAPPING.md). The wire types and mappers live in `features/.../remote/`.
- Sign-in keeps the owner signed in (an offline session, about 30 days idle). The tokens are encrypted with a key in
  the Android Keystore, are excluded from backups, and are refreshed automatically. When the session ends, the app
  goes back to sign-in.
- "Continue with Google" opens Google's sign-in directly in the browser (through the WebBlock Keycloak realm, with
  `kc_idp_hint=google`) and comes back through `com.webblock.app:/oauth2redirect`. It only signs in to an existing
  WebBlock account.

## Mock mode

Set `use.mock.data=true` in `local.properties` and rebuild. Every screen then runs on sample data, with no network:

- Sign in with **any email containing "@" and a password of 8 or more characters**, for example `owner@example.com` /
  `password123`. Every sample account sees the same websites (Petal & Stem, Golden Crumb Bakery, Bean & Brew Café,
  Studio Lotus Yoga).
- The plan on Monitor follows the email: one starting with `ultra` shows Ultra, `pro` shows Pro, anything else Plus.
- "Continue with Google" opens a sample account chooser.
- Forgot password: any address gets a code, and any 6-digit code (for example `123456`) is accepted.

Set `use.mock.data=false` (or remove the line) and rebuild to go back to the v2 API.

## Running against AWS

1. Leave `use.mock.data` out (or `false`) and keep the default `api.base.url`.
2. Run the app on an emulator (any recent Google APIs x86_64 image) or a device with internet access.
3. Sign in with **your own** WebBlock owner account, created on the WebBlock website. Never put an account's password
   in the repository or in `local.properties`.
4. Errors: debug builds log every v2 call answered with a non-2xx status to logcat under the tag `WebBlockApi`, one
   line each: method, path, status, the problem code, `requestId` (the answer's `X-Request-Id`) and, for a refused
   field, `fields=field:CODE`. For example:
   `POST /api/project/v2/projects/{id}/products -> 400 validation-failed requestId=… fields=name:REQUIRED`.
   Quote the `requestId` when you report a problem. No body, password or token is ever logged.
   That tag does not cover everything: a failed token refresh and calls that got no answer are not in it, and a session
   that could not be restored on launch is logged under `ApiAuthRepository`. The `okhttp.OkHttpClient` tag shows every
   call's method, URL and status. To see all three:
   `adb logcat -s WebBlockApi ApiAuthRepository okhttp.OkHttpClient`.

## Commands

```bash
./gradlew assembleDebug        # build
./gradlew test                 # unit tests
./gradlew checkCode            # detekt, must pass before a pull request
./gradlew checkKtlint          # formatting report
./gradlew ktlintFormat         # fix formatting
```

`checkCode` runs detekt 1.23, which cannot run on JDK 25 (it fails while reading the Java version). The Gradle daemon
is pinned to JDK 25 by `gradle/gradle-daemon-jvm.properties` (`toolchainVersion=25`, resolved through foojay), so
setting `JAVA_HOME` or `org.gradle.java.home` does not change it. To run `checkCode`: have a JDK 21 installed,
temporarily set `toolchainVersion=21` in that file, run `./gradlew checkCode`, then restore the file
(`git checkout -- gradle/gradle-daemon-jvm.properties`). Never commit that change.

## Layout

```
app/        MainActivity, the navigation graph, the Google sign-in redirect
features/   feature slices: auth, website (My Websites), products, business, monitor, notifications;
            remote/ holds the v2 API (Retrofit interfaces, wire types, mappers, Api*Repository);
            note/ is the starter's sample, not part of the app's navigation
core-ui/    theme, previews, motion, shared components
core/       DataState, the v2 network stack (JSON, errors, tokens, session), typed destinations, network DI
docs/       API mapping, architecture, conventions, feature walkthrough
```

## WebBlocks Owner app (from the Figma)

Mobile Owner Portal for WebBlock, built from the Figma "iOS Owner App" and kept the same as the iOS app.

Flow: Splash → Intro (Skip) → Sign in (/ Forgot password) → My Websites → pick a website → portal tabs
**Products · Business · Monitor** (+ Notifications from the bell). A restored session goes from the intro straight to
My Websites. The selected website lives in `SessionStore` (`:core`), so switching it from the website chip on any tab
re-loads that tab for the new site.
