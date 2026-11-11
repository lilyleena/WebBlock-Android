# Presentation layer conventions

Use this as the **pull-request checklist** for every new or changed screen.

## Screen contract

Each screen has one file in `presentation/model/` with three types:

- **`*UiState`**: one `@Immutable` data class. The single source of truth for Compose.
- **`*Event`**: a sealed interface of everything the user can do. The ViewModel exposes one
  `fun onEvent(event: *Event)`.
- **`*Effect`**: a sealed interface of one-shot things (snackbar, navigation, dialog). Sent through
  `OneShotEvents<T>` and consumed once with `OnEachEvent`.

Reference: `features/note/presentation/model/NoteListUiContract.kt`.

## ViewModel

- `@HiltViewModel` + `@Inject constructor`.
- Expose an **immutable** `StateFlow` only: `private val _state = MutableStateFlow(...)` and
  `val state = _state.asStateFlow()`.
- Update with `_state.update { it.copy(...) }`.
- Call **use cases only**. Never a repository, service, API, or `Context`.
- One-shot effects go through `OneShotEvents`, never through state. A state is re-delivered on
  every recomposition; a snackbar stored in state shows twice.

## Composables

- Split every screen into a stateful `*Screen` (gets the ViewModel, collects state, consumes
  effects) and a stateless `*Content` (takes state and callbacks). Previews use `*Content`.
- Collect with `collectAsStateWithLifecycle()`, not `collectAsState()`.
- Get ViewModels with `hiltViewModel()`.
- **No business logic** in composables: no date parsing, list filtering, or API-shape mapping.
  Move it to a use case or a mapper.
- `LaunchedEffect` only for UI lifecycle work (scroll position). API calls live in the ViewModel.
- All user-facing text through `stringResource()`. No hardcoded literals.
- Max ~250 lines per composable. Split extras into `components/`.

## DataState exhaustiveness

- Handle all four branches: **Loading**, **Success**, **Error**, **MessageSuccess**.
- No `TODO()` in production branches. Use a skeleton, an empty view, or a message.
- Prefer `DataStateScreenShell` (in `:core-ui`) so the branching lives in one place:

```kotlin
DataStateScreenShell(
    state = state.notes,
    loading = { NoteListSkeleton() },
    error = { EmptyStateView(title = ..., actionLabel = "Retry", onAction = ...) },
    isEmpty = { it.isEmpty() },
    empty = { EmptyStateView(title = "No notes yet") },
    success = { notes -> NoteList(notes) },
)
```

## Previews

Every component gets a preview. Use the shared multipreview annotations from
`core-ui/preview/AppPreviews.kt`, never a bare `@Preview`:

```kotlin
@ThemePreviews          // light + dark: the default for every component
@FontScalePreviews      // 100% + 150% font scale: for text-heavy rows, cards, fields
@Composable
private fun NoteRowPreview() {
    PreviewSurface { NoteRow(note = previewNote, onClick = {}) }
}
```

- Always wrap in `PreviewSurface { }` so the preview uses `AppTheme`.
- Preview functions are `private fun *Preview()` at the bottom of the file.
- Screens are previewed through their `*Content` composable, never the ViewModel.

## Theme

- Colours from `MaterialTheme.colorScheme`, text styles from `MaterialTheme.typography`,
  corners from `MaterialTheme.shapes`. Never a raw `Color(0xFF...)` in a feature.
- Buttons use `PillShape`.

## Validation before a pull request

```bash
./gradlew assembleDebug
./gradlew test
./gradlew checkCode
./gradlew checkKtlint
```

Then smoke-test the touched flow on a device or emulator.
