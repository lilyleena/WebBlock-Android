# AGENTS.md

Single source of truth for AI coding-agent instructions in this repository. `CLAUDE.md` is a
pointer to this file. Edit only this file.

**KSHRD Android Starter**: a Kotlin / Jetpack Compose template that shows the module structure
and conventions used by KSHRD Android apps, with one sample feature (`note`) and no backend.
Students copy this project to start their own apps.

## Project overview

- Min SDK 26 / Target SDK 36 · Kotlin 2.2.10 · Compose BOM 2024.09.00 · Hilt 2.59.2
- No network calls: `InMemoryNoteApi` fakes the backend. `api.base.url` in `local.properties`
  configures the Retrofit client for when a real API exists.

## Build and quality commands

```bash
./gradlew assembleDebug        # debug build
./gradlew test                 # unit tests, all modules
./gradlew :features:test       # unit tests, one module
./gradlew checkCode            # detekt (must pass before a PR)
./gradlew checkKtlint          # ktlint report (does not fail the build)
./gradlew ktlintFormat         # fix formatting
```

## Module graph

```
:app        → shell (MainActivity, StarterApplication, MainGraph)
:features   → feature slices (note)
:core-ui    → Compose design system (AppTheme, previews, motion, shared components)
:core       → DataState/UiState, RemoteDataSource, BaseResponse, OneShotEvents,
              typed navigation destinations, network DI
```

Feature modules must not depend on each other. Shared code goes through `:core` or `:core-ui`.
Full rules: `docs/ARCHITECTURE.md`.

## Architecture: Clean Architecture + MVVM

Each feature follows `data → domain → presentation` inside `com.kshrd.features.<feature>/`.
Walkthrough for a new feature: `docs/ADDING_A_FEATURE.md`.

### ViewModel pattern

```kotlin
@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val getNotes: GetNotesUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(NoteListUiState())
    val state = _state.asStateFlow()
    private val _effects = OneShotEvents<NoteListEffect>()
    val effects = _effects.events
    fun onEvent(event: NoteListEvent) { /* every user action comes through here */ }
}
```

- Immutable `StateFlow` only. Update with `_state.update { it.copy(...) }`.
- One-shot effects (navigation, snackbars) through `OneShotEvents`, never `StateFlow`.
- ViewModels call use cases only. Never a repository, service, API or `Context`.

### UiState / DataState

- `UiState<T>` is the presentation alias for `DataState<T>` (in `:core`).
- Branches: `Loading | Success | Error | MessageSuccess`. Handle all four. No `TODO()`.
- Prefer `DataStateScreenShell` from `core-ui/components/screen/` for the branching.

### Composables

- Stateful `*Screen` + stateless `*Content`. Previews use `*Content`.
- `collectAsStateWithLifecycle()`, not `collectAsState()`. `hiltViewModel()` for ViewModels.
- Stateless; no business logic inside composables.
- `OnEachEvent` for effects. `stringResource()` for all text.
- Max ~250 lines per composable; split into `components/`.
- Every component gets a preview with the shared annotations in `core-ui/preview/AppPreviews.kt`:

```kotlin
@ThemePreviews        // light + dark
@Composable
private fun NoteRowPreview() {
    PreviewSurface { NoteRow(note = previewNote, onClick = {}) }
}
```

`@FontScalePreviews` adds 100% + 150% font scale for text-heavy components. Preview functions
are `private fun *Preview()` at the bottom of the file.

## Navigation

- Typed destinations in `core/navigation/AppDestinations.kt` + `navController.navigateTo(destination)`.
- Graph wiring only in `app/ui/navigation/MainGraph.kt`. Feature screens take callbacks.
- Arguments: `data class` destination + `savedStateHandle.toRoute<T>()` in the ViewModel.

## Dependency injection (Hilt)

- `@InstallIn(SingletonComponent::class)` for app-wide bindings.
- `features/di/`: `FeatureApiModule` (`@Provides`), `FeatureServiceModule`, `FeatureRepositoryModule` (`@Binds`).
- Use cases with `@Inject constructor` need no module entry.
- Never inject `Context` into ViewModels; use `@ApplicationContext` in repositories or services.

## Gradle and dependencies

- All versions come from `gradle/libs.versions.toml`. Never hardcode a version in a module.
- Every Compose module applies `implementation(platform(libs.androidx.compose.bom))`.
- Tooling: `libs.androidx.compose.ui.tooling.preview` (`implementation`) and
  `libs.androidx.compose.ui.tooling` (`debugImplementation`).

## Network layer

- `CoreModule` provides OkHttp + Retrofit with `BuildConfig.API_BASE_URL`.
- Every `*ServiceImpl` wraps its call in `RemoteDataSource`, which maps HTTP errors and connection
  failures to `DataState.Error` in one place.
- Auth (bearer interceptor, single-flight refresh on 401) belongs in `core/network` when the app
  gets one. Never handle auth inline in a feature.

## Naming conventions

| Kind | Suffix | Example |
|------|--------|---------|
| ViewModel | `*ViewModel` | `NoteListViewModel` |
| Screen state / event / effect | `*UiState` / `*Event` / `*Effect` | `NoteListUiState` |
| Use case | `*UseCase` | `GetNotesUseCase` |
| Repository interface / impl | `*Repository` / `*RepositoryImpl` | `NoteRepository` |
| Service interface / impl | `*Service` / `*ServiceImpl` | `NoteService` |
| Retrofit API | `*Api` | `NoteApi` |
| Stateful / stateless screen | `*Screen` / `*Content` | `NoteListScreen` |

## core-ui reuse rules

Put a composable in `:core-ui` when 2+ features use it. Feature-specific layouts stay in the feature.

## Key files

| What | Where |
|------|-------|
| Base URL | `local.properties` → `core/build.gradle.kts` → `BuildConfig.API_BASE_URL` |
| Network DI | `core/.../di/CoreModule.kt` |
| Response envelope | `core/.../base/BaseResponse.kt` |
| Async state | `core/.../data/entity/DataState.kt`, `core/.../presentation/UiState.kt` |
| Retrofit to Flow | `core/.../network/RemoteDataSource.kt` |
| Typed destinations | `core/.../navigation/AppDestinations.kt` |
| Theme | `core-ui/.../theme/Theme.kt` |
| Preview annotations | `core-ui/.../preview/AppPreviews.kt` |
| Screen shell | `core-ui/.../components/screen/DataStateScreenShell.kt` |
| App entry point | `app/.../app/StarterApplication.kt` |
| Navigation graph | `app/.../ui/navigation/MainGraph.kt` |

## Hard rules

- Never commit `local.properties`, keystores, tokens or credentials.
- Never put a feature-to-feature dependency in a `build.gradle.kts`.
- Never change the JSON field names of a model the API owns.
- Minimal diffs; match the surrounding style.
- Do not create git commits or push unless the user explicitly asks.
