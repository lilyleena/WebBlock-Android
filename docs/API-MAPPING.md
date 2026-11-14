# WebBlock owner app (Android) → v2 API mapping

How every repository call in `:features` maps to the WebBlock v2 API. It follows the iOS app's mapping
(`webblock-ios` `dev` a88b8ee, `docs/API-MAPPING.md`) **call for call**. This is the same design applied to the
Android code, not a second one. Section 3 lists where the Android code differs today and what changes to close the gap.

- **Contracts:** `web-block-contracts`, branch `dev` at `bdb5390`. Files: `openapi/{user,project,notification,payment,provisioning}-v2.yaml`,
  read with `git show dev:openapi/<service>-v2.yaml`.
- **Base URL:** `https://wb-api.roa-dev.digital/` by default. `api.base.url` in `local.properties` (git-ignored) overrides it.
  All paths below are relative to it.
- **Who uses it:** site owners only (realm role `user`). No admin routes and no sign-up.
- **Status:** 14 Nov 2026. Data layer drafted on `android-v2-data` (wire types, mappers, token store, auth). Every gap
  G1–G8 is decided (section 4); the owner's decisions of 14 Nov are in section 4 too.

## 1. Rules shared by every call (the same as iOS)

| Topic | Rule | Android side |
| --- | --- | --- |
| Auth | `Authorization: Bearer <accessToken>` from the `TokenSet`. Only `/sessions*` and `/password-resets*` are public. | An OkHttp interceptor in `:core` adds the header. |
| Tokens | Refreshed with `POST /api/user/v2/sessions/refresh` at 80 % of `refreshExpiresIn` (or 30 s before `expiresIn`, whichever comes first), and once after any `401`. A refresh answering `400`/`401` ends the session (sign-in screen). The new refresh token always replaces the old (Keycloak rotates it). **`refreshExpiresIn` ≤ 0** means the expiry is unknown (A8): the session is kept, and the app refreshes only near the access token's expiry or after a `401`. | An OkHttp `Authenticator` does the one refresh after `401`, behind a mutex so parallel calls share one refresh. Tokens are encrypted with an AES-GCM key in the Android Keystore (not exportable) and the ciphertext sits in app-private storage; the token file is excluded from backup. |
| Long session (G8) | `POST /sessions` sends `"stayLoggedIn": true`: an offline session, about 30 days idle, no maximum. | Same. |
| Sign out | `POST /api/user/v2/sessions/revocations {refreshToken}` → `204`, then the stored tokens are cleared, also when the revocation fails. | Same. |
| Body | JSON, camelCase, ISO 8601 UTC times, UUID strings (Knock notification ids are opaque strings). No envelope. | `java.time` (minSdk 26) for times. Nullable Kotlin types for every field the contract marks nullable. |
| Lists | `Page<T>` = `{items, page (zero-based), size, totalItems}`; page until `totalItems`. Sizes: Project 200, Notification 100, Provisioning 50. | Same. |
| Errors | RFC 9457 `application/problem+json`. Code = end of `type` (`urn:webblock:problem:<code>`). Show `title` and `detail`. Field errors: `fieldErrors` map (User, Notification) or `errors[] {field, code}` (Project, Provisioning). | One problem parser in `:core`, mapped to the existing `DataState` error. |
| Request id | The app sends no `X-Request-Id` (the edge replaces it). It logs the answer's `X-Request-Id` (or the problem's `requestId`) with each error. | Logged in debug builds only. |
| Rate limits | Only the OTP routes answer `429`. `otp-resend-too-soon` sends `Retry-After` (seconds): "Wait N seconds" ("a minute" without it). `otp-too-many-attempts`: ask for a new code. Sign-in and refresh never answer `429`. | Same. |
| Owner check | Another owner's website answers `404`, like a missing one. | Same. |

## 2. Call → route mapping

### 2.1 Sign in and password reset (User v2, `/api/user/v2`): `AuthRepository`

| Android call | Route(s) | Notes (as iOS 2.1) |
| --- | --- | --- |
| `signIn(email, password)` | `POST /sessions {email, password, stayLoggedIn: true}` → `201 TokenSet`, then `GET /me` | `SessionOwner.name ← fullName`, `email ← email`, plus the plan (`FREE_TRIAL`, `PLUS`, `PRO`, `ULTRA`; `null` while Payment is down = trial limits, reloaded later). `401 invalid-credentials`; `403 account-suspended` has its own message. `ADMIN` without `USER` in `roles`: sign out and say the app is for site owners. |
| `signInWithGoogle()` | **Off** (G1; the owner's account-creation decision is pending). When on: AppAuth with PKCE (S256), `client_id=webblock-ios`, `redirect_uri=com.webblock.app:/oauth2redirect`, `kc_idp_hint=google`, `scope=openid email profile offline_access`, then `POST /sessions/google {code, codeVerifier, redirectUri}` → `TokenSet` | Behind a build flag, off. While off, the Google button is hidden. A different redirect goes to the coordinator first. **No new accounts (owner):** an unknown Google identity answers a problem (`404 account-not-found`): "Create your account on the WebBlock website first, then sign in here." No retry. `422 account-email-missing` no longer exists on this route (contracts dev 5a2c209). A "Create your account on the website" link is configurable and hidden until the coordinator gives the web app's sign-up URL. |
| `requestResetCode(email)` | `POST /password-resets {email}` → `202 {expiresInSeconds, resendAfterSeconds}` | Also the Resend button. Same answer whether or not the email has an account. |
| `verifyResetCode(email, code)` | `POST /password-resets/verifications {email, code}` → `201 {verificationToken, expiresInSeconds}` | `verificationToken` is kept in memory only, keyed by email. `400 otp-invalid` / `otp-expired`, `429 otp-too-many-attempts`. |
| `resetPassword(email, newPassword)` | `POST /password-resets/confirmations {verificationToken, password, confirmPassword}` → `204` | `confirmPassword = newPassword`. Every session ends. `400 verification-token-invalid` → start again; `422 password-too-weak` (8–128, upper, lower, digit). |
| *(new)* `restoreSession()` | `POST /sessions/refresh` → `GET /me` | On launch, from the stored refresh token. `401 session-expired` → sign-in. |
| *(new)* `signOut()` | `POST /sessions/revocations {refreshToken}` → `204` | Stored tokens cleared in every case. |

### 2.2 Websites (Project v2, `/api/project/v2`): `WebsiteRepository`

| Android call | Route(s) | Notes |
| --- | --- | --- |
| `getWebsites()` | `GET /projects?page=&size=200` | `id`, `name`, `businessType ← businessType ?? "—"`, `status` (`LIVE`/`DRAFT`/`SUSPENDED`), `url ← domain`, **`logoUrl ← logoUrl` (G4)**; a `null` logo falls back to the business-info logo until G4 is on AWS, then to the initials tile. Only `LIVE` sites are shown. **Hosting:** for each live site, Provisioning `GET /api/provisioning/v2/projects/{id}/status`; `online == false` → an **"Offline" badge**, the portal still opens and stays editable, with a note to renew on the web. |

### 2.3 Notifications (Notification v2, `/api/notification/v2`): `NotificationsRepository`

| Android call | Route(s) | Notes |
| --- | --- | --- |
| `observeNotifications()` | `GET /me/notifications?page=0&size=50` | `id` (Knock string), `source ← title`, `message`, `unread ← !read`, time text from `createdAt` ("2 minutes ago"), **`detail ← detail ?? ""`** (the screen falls back to `message`), logo of the matching website. **Target (G5):** `websiteId ← target.websiteId ?? websiteId`, `target ← target.tab` (`MONITOR → Monitor`, `PRODUCTS → Products`, `BUSINESS_INFO → Info`, `null` → no button), action titles "Open Monitor" / "Open Products" / "Open Business information". Without `target` (older server): a website's notification opens its Monitor. |
| `markAllRead()` | `PATCH /me/notifications {"read": true}` → `204` | |
| `markRead(id)` | `PATCH /me/notifications/{notificationId} {"read": true}` → `200 Notification` **(G6)** | Called when one is opened. Marked read on screen first; a failure is ignored. `404` = not in the caller's feed. |

### 2.4 Products (Project v2): `ProductRepository`

| Android call | Route(s) | Notes |
| --- | --- | --- |
| `observeCatalog(websiteId)` | `GET /projects/{id}/products?page=&size=200&sort=createdAt,desc` + `GET /projects/{id}/product-categories` (a `string[]`, owner's order) | `id`, `name`, `description`, `category`, `price` as a decimal string; **`null` = hidden price (G7)**, shown as "Price hidden"; `imageUrl`, `version`. Pages up to `totalItems` (Ultra allows 1000). |
| `uploadPhoto(sourceUri)` | `POST /projects/{id}/media` (multipart `file`) → `201 MediaAsset {id, url, name}` | JPEG or PNG, **2 MiB at most**. The app reads the `content://` URI, re-encodes to JPEG at lower quality until it fits, refuses it otherwise. **`422 STORAGE_LIMIT_REACHED`** → "Photo storage is full. Remove photos on the web." **`422 PHOTO_LIMIT_REACHED`** (the plan's photos-per-website limit, owner) has its own message. The repository call gains a `websiteId`. **Unused photos (owner):** after a photo is replaced or removed, the app deletes the old media asset with Project's new DELETE media route (`409` while still in use is fine); the exact route is still to come. |
| `addCategory(websiteId, name)` | `POST /projects/{id}/product-categories {name}` → `201` | Name rule: 2–40 letters, words joined by a space or `& ' ’ -`, no digits (`400 VALUE_INVALID`). `409 CATEGORY_NAME_TAKEN` is fine. |
| `saveProduct` (new) | `POST /projects/{id}/products` `ProductBody {name, description, category, price: "6.00" \| null, imageUrl}` → `201 Product` | `422` = unknown category or the plan's product limit. `imageUrl` must be one of this website's media URLs (Q2). |
| `saveProduct` (edit) | `PUT /projects/{id}/products/{productId}` with `version` → `200 Product` | `imageUrl` absent keeps the photo, `null` removes it. **`409` → "This product was changed elsewhere. Pull to refresh."** An empty price field keeps a hidden price hidden; typing a price shows it. |
| `deleteProduct(websiteId, productId)` | `DELETE /projects/{id}/products/{productId}` → `200 Product` | |

### 2.5 Business information (Project v2): `BusinessRepository`

| Android call | Route(s) | Notes |
| --- | --- | --- |
| `observeBusiness(websiteId)` | `GET /projects/{id}/business-info` + `GET /projects/{id}` | `name`, `type`, `url` come from `Website` (read-only in the app). `about ← description`, `phone`, `email`, `address`, `mapsLink ← mapsUrl ?? ""`. `hours[7]`: `{day: MONDAY…SUNDAY, open, opensAt "HH:mm", closesAt "HH:mm"}` ↔ `DayHours {day, isOpen, openMinutes, closeMinutes}`. |
| `saveAll` / `updateProfile` / `updateHours` | `PUT /projects/{id}/business-info` `{description, phone, email, address, mapsUrl, hours}` → `200 BusinessInfo` | The API has one PUT for both, so `updateProfile` and `updateHours` read the current info and send the whole body. `logoUrl` is left out (the logo is kept). An empty `mapsLink` is sent as `null`. |

### 2.6 Monitor (Payment, Provisioning, Project v2): `MonitorRepository`

`observeMonitor(websiteId)` makes these calls in parallel. If one fails, the rest still shows.

| `MonitorData` field | Source | Notes |
| --- | --- | --- |
| `health` | Provisioning `GET /api/provisioning/v2/projects/{id}/status` → `online` | `404 deployment-not-found` → offline. |
| `uptimePercent` + checked text | same → `uptimePercent` (decimal string, `"99.98"`) and `checkedAt` **(G2)** | Kept as a string (no float rounding). `null` → **"—"**. With a value: "99.98%" and "Uptime over the last 30 days · checked 5 minutes ago". |
| `succeededTotal`, `failedTotal` | Provisioning `GET /projects/{id}/releases/stats` → `{total, succeeded, failed}` | Success share "—" when `total` is 0. |
| `deploys` | Provisioning `GET /projects/{id}/releases?size=20` | `number`, `atMillis ← startedAt`, `succeeded ← status == SUCCEEDED`, `durationSeconds`, `startedBy ← startedBy?.fullName ?? "—"`, `liveFor` from `promotedAt` to the next newer `promotedAt` ("(current)" for the live one, `null` for a failed one). |
| `lastDeployAgo`, `lastDeployDetail` | the newest release | relative `finishedAt`; "Deploy N · 18 Oct, 2:15 PM". |
| `resetsOn` | Payment `GET /api/payment/v2/projects/{id}/usage` → `resetsAt` | `null` → "—". |
| usage meters (products, redeploys, websites) | same → `metrics[]` by `key` | **Used, limits and labels all from Payment** (`used`, `limit`, `label`), decision D1: **no hard-coded limits on the API path** (sample numbers live only in the InMemory sources). On the trial there is no `redeploys`: use `deploys`. **`limit: null` = unlimited** (an admin's choice): "12 / Unlimited", an empty bar, tiles say "No limit". **A metric Payment didn't send** shows "/ —" ("Limit not available"). `used: null` shows "—". A `null` label keeps the app's title. |
| photos meter | same → a `photos` metric (contracts dev 42bb1cd: label "Photos per website", cardLabel "Photos") | Shown **when Payment sends a `photos` metric** (owner: PLUS 30, PRO 200, ULTRA 1000, the trial 30 unconfirmed), with that metric's `used`, `limit` and `label`; never a number in the app. Photos = every upload in the website's media library. `used: null` (Project's count not live yet) → "— / 30" with an empty bar. Hidden without the metric. |
| `logs` | Project `GET /projects/{id}/activity?size=20` | `title`, `detail`, category from `type` (`DEPLOY`, `PRODUCT`, `BUSINESS_INFO`), `atMillis ← occurredAt`, `outcome ← FAILED` or success. Pro and Ultra only: **`403` = an empty log**, not an error. |

Not used: access stats and logs (admin-only), deploy commands, `deploy-access`.

## 3. Differences from iOS (what the Android code changes)

The mapping is the same. These are the Android model and code changes needed to reach it:

| Area | Android today | Change |
| --- | --- | --- |
| `Website` | `logoRes` (bundled drawable) only; no hosting state | Add `logoUrl: String?` and `isHostingOffline`; keep `logoRes` for the mock data. |
| `Product` | `price: Double` (non-null), no `version` | `price: BigDecimal?` (`null` = hidden), `version: Long?` (`null` = never loaded from the API → `POST`). |
| `ProductRepository.uploadPhoto(sourceUri)` | no website id | Add `websiteId` (media belongs to a website). |
| `AppNotification` | `timeText` is text; `logoRes` | Keep `timeText` computed from `createdAt`; add `logoUrl`. `detail`, `websiteId`, `target`, `actionTitle` already exist. |
| `MonitorData` | `Plan.max*` constants, `uptimePercent: Double?`, a `Photos` meter always shown, no labels, `Plan.logsEnabled` | Limits and labels on each meter from Payment; uptime as a string plus `checkedAt`; the photos meter only with a `photos` metric; logs empty on `403`. |
| `SessionOwner` | `id`, `name`, `email` | Add the plan. |
| `AuthRepository` | no `restoreSession` / `signOut`; Google opens a mock chooser sheet | Add both; hide the Google button while G1 is off. |
| Business save | `updateProfile` and `updateHours` separate | Both send the full PUT body (section 2.5). |
| Networking | Retrofit + Gson, no auth | Add the auth interceptor, `Authenticator`, problem parser and token store in `:core`. JSON with kotlinx-serialization (already a dependency) through Retrofit's own `converter-kotlinx-serialization`, so non-null Kotlin fields can't silently receive `null` (Gson's known gap). |
| Data sources | `InMemory*DataSource` only | Keep them for previews and unit tests (the iOS mock). The API sources are picked by a build flag; the debug build defaults to the API. |
| Sample `note` feature | starter template, not in iOS | Left as is (not part of the owner flows). |

## 4. Gaps and questions (all decided; see the iOS mapping sections 3 and 4)

G1 Google: off. G2 uptime: built, "—" when `null`. G3 photos meter: hidden. G4 `Website.logoUrl`: done. G5 bell
`detail` + `target`: done. G6 mark one read: done. G7 hidden price: done. G8 `stayLoggedIn`: done. Q1–Q8: answered as
in the iOS mapping (replaced photos count; own media URLs only; Offline badge, editable; one token for every service;
`Retry-After` on `otp-resend-too-soon` only; limits from Payment; "—" for a `null` business type).

Owner's decisions, 14 Nov 2026: no new accounts through Google (`account-not-found`); unused photos are deleted
(Project's DELETE media route, to come); a photos-per-website limit with Payment's `photos` metric and
`422 PHOTO_LIMIT_REACHED`; the realm's online SSO idle goes from 30 to 60 min (`refreshExpiresIn` 3600), with the
refresh rule unchanged; a LIVE site with OFFLINE hosting gets the Offline badge, stays editable, with a renew note.

## 5. Android-only open points

1. **Application id (owner, 14 Nov):** stays `com.kshrd.starter`. The Google redirect stays
   `com.webblock.app:/oauth2redirect`; AppAuth's scheme is set on its own (`appAuthRedirectScheme`), so the
   `webblock-ios` Keycloak client needs nothing new.
2. **Backup:** the manifest has `allowBackup="true"`. The token store is excluded from backup and device transfer
   (`dataExtractionRules`), so a restored phone signs in again.
