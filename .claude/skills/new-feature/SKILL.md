---
name: new-feature
description: Scaffold a new feature slice (data → domain → presentation, DI, navigation) in :features following this repo's Clean Architecture + MVVM conventions. Use when the user asks to add a feature, screen flow, or module slice.
---

# New feature

Arguments: the feature name (e.g. `profile`) and what it should show/do.

1. Read `docs/ADDING_A_FEATURE.md` once. It is the step list — follow its 12 steps in order.
   Read `docs/presentation-conventions.md` only when you reach the screen steps.
2. Copy shape, not content, from the smallest existing slice: `features/src/main/java/com/kshrd/features/note/`.
   Grep it for the file you need instead of reading the whole tree.
3. Package: `com.kshrd.features.<feature>/{data,domain,presentation}`. Use an `InMemory*` fake
   data source while there is no backend.
4. Wire DI in `features/di/` (`@Binds` for repository/service). Use cases need no module entry.
5. Add a typed destination in `core/.../navigation/AppDestinations.kt` and wire it only in
   `app/.../ui/navigation/MainGraph.kt`. Screens take callbacks, never a NavController.
6. All UI text in `strings.xml`. Every component gets a `@ThemePreviews` preview of its `*Content`.
7. Add at least one unit test for the use case under `features/src/test/...`.
8. Finish with the `verify` skill.

Do not touch other features. Do not add dependencies without asking.
