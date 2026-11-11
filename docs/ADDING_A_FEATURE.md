# Adding a feature, step by step

This walkthrough adds a feature called `task`. Replace the name with yours. Every step points at
the `note` sample so you can copy the shape.

## 1. Create the package

```
features/src/main/java/com/kshrd/features/task/
  data/api/  data/service/  data/repository/
  domain/model/  domain/repository/  domain/service/  domain/usecase/
  presentation/model/  presentation/components/  presentation/screen/  presentation/viewmodel/
```

## 2. Domain model

`domain/model/Task.kt`: a data class with `@SerializedName` on every field, matching the API
contract exactly. Model: `note/domain/model/Note.kt`.

## 3. API interface

`data/api/TaskApi.kt`: a Retrofit interface. Every call returns `Response<BaseResponse<T>>`.
Model: `note/data/api/NoteApi.kt`.

While there is no backend, add `data/api/InMemoryTaskApi.kt` implementing the interface with a
list and a `delay`. Model: `note/data/api/InMemoryNoteApi.kt`.

## 4. Service

- `domain/service/TaskService.kt`: interface returning `Flow<DataState<T>>`.
- `data/service/TaskServiceImpl.kt`: wraps each API call in `RemoteDataSource`.

Model: `note/domain/service/NoteService.kt`, `note/data/service/NoteServiceImpl.kt`.

## 5. Repository

- `domain/repository/TaskRepository.kt`: interface.
- `data/repository/TaskRepositoryImpl.kt`: delegates to the service.

Model: `note/domain/repository/NoteRepository.kt`, `note/data/repository/NoteRepositoryImpl.kt`.

## 6. Use cases

One class per user intention, with `@Inject constructor` and a single `operator fun invoke`.
Return `repository.xxx().asUseCaseFlow()`. Put domain validation here, not in the ViewModel.
Model: `note/domain/usecase/GetNotesUseCase.kt`, `AddNoteUseCase.kt`.

Write a unit test with a fake repository. Model: `features/src/test/.../GetNotesUseCaseTest.kt`.

## 7. Wire DI

Edit the three modules in `features/di/`:

```kotlin
// FeatureApiModule
@Provides @Singleton fun provideTaskApi(inMemory: InMemoryTaskApi): TaskApi = inMemory
// later, with a backend: fun provideTaskApi(retrofit: Retrofit): TaskApi = retrofit.create(TaskApi::class.java)

// FeatureServiceModule
@Binds @Singleton abstract fun bindTaskService(impl: TaskServiceImpl): TaskService

// FeatureRepositoryModule
@Binds @Singleton abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
```

Use cases need nothing here.

## 8. Screen contract

`presentation/model/TaskListUiContract.kt` with `TaskListUiState`, `TaskListEvent`,
`TaskListEffect`. Model: `note/presentation/model/NoteListUiContract.kt`.

## 9. ViewModel

`presentation/viewmodel/TaskListViewModel.kt`: `@HiltViewModel`, one `StateFlow<TaskListUiState>`,
one `onEvent`, effects through `OneShotEvents`. Model: `note/presentation/viewmodel/NoteListViewModel.kt`.

For a screen with an argument, read it with `savedStateHandle.toRoute<TaskDetail>()`.
Model: `NoteDetailViewModel.kt`.

## 10. Composables

- `presentation/components/`: stateless pieces (`TaskRow`, `TaskListSkeleton`). Each with
  `@ThemePreviews` inside `PreviewSurface`.
- `presentation/screen/TaskListScreen.kt`: `TaskListScreen` (stateful) + `TaskListContent`
  (stateless, previewed). Use `DataStateScreenShell` for the loading/error/empty/success branches.
- Strings in `features/src/main/res/values/strings.xml`.

Model: `note/presentation/components/*`, `note/presentation/screen/NoteListScreen.kt`.

## 11. Navigation

- Add the destination to `core/src/main/java/com/kshrd/core/navigation/AppDestinations.kt`:

```kotlin
@Serializable data object TaskList : AppDestination
@Serializable data class TaskDetail(val taskId: Long) : AppDestination
```

- Register it in `app/src/main/java/com/kshrd/starter/ui/navigation/MainGraph.kt`:

```kotlin
composable<TaskList> { TaskListScreen(onOpenTask = { navController.navigateTo(TaskDetail(it)) }) }
composable<TaskDetail> { TaskDetailScreen(onBack = { navController.popBackStack() }) }
```

## 12. Verify

```bash
./gradlew assembleDebug test checkCode checkKtlint
```

Open the previews in Android Studio (light, dark, 150% font) and run the app on an emulator.

## Checklist before you submit

- [ ] Every folder in the template is used, or deliberately empty with a reason.
- [ ] No feature imports another feature's package.
- [ ] ViewModel calls use cases only; no `Context`.
- [ ] All four `DataState` branches handled; no `TODO()`.
- [ ] Every composable has a preview inside `PreviewSurface`.
- [ ] No hardcoded user-facing strings or raw colours.
- [ ] At least one unit test per use case.
- [ ] `checkCode` passes.
