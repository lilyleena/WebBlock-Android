# Architecture

This project follows **Clean Architecture + MVVM** in a multi-module Gradle build. The shape is
the same one used by the KSHRD production apps, reduced to one sample feature so you can see every
rule in a small amount of code.

## Module graph

```
:app       → shell: MainActivity, StarterApplication, the navigation graph (MainGraph.kt)
:features  → feature slices (sample: note). Each slice is data → domain → presentation
:core-ui   → Compose design system: AppTheme, previews, motion, shared components
:core      → contracts shared by every feature: DataState/UiState, RemoteDataSource,
             BaseResponse, OneShotEvents, typed navigation destinations, network DI
```

Dependencies only point downward:

```
:app ──► :features ──► :core-ui ──► :core
  │                                   ▲
  └───────────────────────────────────┘
```

**Rules**

- Feature modules never depend on each other. Anything two features share moves to `:core`
  (logic) or `:core-ui` (UI).
- `:core-ui` depends on `:core`, never the reverse.
- `:app` wires graphs and app-wide startup only. No business logic.
- Package names are lowercase with no underscores: `com.kshrd.core`, `com.kshrd.coreui`,
  `com.kshrd.features`, `com.kshrd.starter`.

## Per-feature package template

```
com.kshrd.features.<feature>/
  data/
    api/            Retrofit interface (+ a fake implementation while there is no backend)
    service/        *ServiceImpl: wraps API calls in RemoteDataSource
    repository/     *RepositoryImpl: delegates to the service, adds caching if needed
  domain/
    model/          data classes the API returns and the UI shows
    repository/     *Repository interface
    service/        *Service interface
    usecase/        one class per user intention, single `invoke`
  presentation/
    model/          *UiState, *Event, *Effect (the screen contract)
    components/     stateless composables, each with a preview
    screen/         *Screen (stateful entry) + *Content (stateless, previewable)
    viewmodel/      *ViewModel
```

The sample feature `note` fills every folder. Copy it when you add a feature
(see `ADDING_A_FEATURE.md`).

## Data flow

```
Composable ──event──► ViewModel ──► UseCase ──► Repository ──► Service ──► Api (Retrofit)
    ▲                    │
    └──── StateFlow ◄────┘        every layer below the ViewModel returns Flow<DataState<T>>
```

- `RemoteDataSource` (in `:core`) converts a Retrofit `Response<BaseResponse<T>>` into a
  `Flow<DataState<T>>`. It is the single place that handles HTTP errors and connection failures.
- `asUseCaseFlow()` gives every use case the same shape: Loading first, then Success or Error.
- The ViewModel collects use-case flows and reduces them into one `UiState`.

## DataState

```kotlin
sealed interface DataState<out T> {
    data object Loading
    data class Success<T>(val data: T)
    data class Error(val code: Int?, val message: String?, val throwable: Throwable?)
    data class MessageSuccess(val message: String)   // 2xx with no payload
}
typealias UiState<T> = DataState<T>                   // presentation-layer name
```

Every `when (state)` in UI handles all four branches. `DataStateScreenShell` in `:core-ui` does
this for you with one slot per branch.

## Naming

| Kind | Suffix | Example |
|------|--------|---------|
| ViewModel | `*ViewModel` | `NoteListViewModel` |
| Screen state | `*UiState` | `NoteListUiState` |
| User action | `*Event` | `NoteListEvent` |
| One-shot effect | `*Effect` | `NoteListEffect` |
| Use case | `*UseCase` | `GetNotesUseCase` |
| Repository interface | `*Repository` | `NoteRepository` |
| Repository impl | `*RepositoryImpl` | `NoteRepositoryImpl` |
| Service interface | `*Service` | `NoteService` |
| Service impl | `*ServiceImpl` | `NoteServiceImpl` |
| Retrofit API | `*Api` | `NoteApi` |
| Stateful screen | `*Screen` | `NoteListScreen` |
| Stateless screen body | `*Content` | `NoteListContent` |

## Dependency injection (Hilt)

- `@InstallIn(SingletonComponent::class)` for app-wide bindings.
- `features/di/` has one module per layer: `FeatureApiModule` (`@Provides`),
  `FeatureServiceModule` and `FeatureRepositoryModule` (`@Binds`).
- A use case with `@Inject constructor` needs no module entry.
- Never inject `Context` into a ViewModel. Use `@ApplicationContext` in a repository or service.
- ViewModels are `@HiltViewModel` and obtained in Compose with `hiltViewModel()`.

## Navigation

- Destinations are `@Serializable` types in `core/navigation/AppDestinations.kt`.
- Navigate with `navController.navigateTo(destination)`.
- A screen with arguments is a `data class`; its ViewModel reads them with
  `savedStateHandle.toRoute<NoteDetail>()`.
- Only `app/ui/navigation/MainGraph.kt` maps destinations to screens. Feature screens take
  navigation as callbacks (`onOpenNote`, `onBack`).

## What goes in `:core-ui` vs a feature

| Put in `:core-ui` | Keep in the feature |
|-------------------|---------------------|
| Theme, typography, shapes, motion | Feature-only assets |
| `DataStateScreenShell`, `EmptyStateView` | Screen composition |
| `SkeletonBox`, `ShimmerBrush` | `NoteListSkeleton` (the feature's layout) |
| Any composable used by 2+ features | A composable used by one feature |

## Code quality

- Max ~250 lines per composable; split into `components/`.
- Max ~300 lines per ViewModel; extract use cases.
- `./gradlew checkCode` (detekt) must pass before a pull request.
- `./gradlew checkKtlint` reports formatting; `./gradlew ktlintFormat` fixes it.
- No global `object` holding Compose `mutableStateOf`. State lives in a ViewModel or `remember`.
