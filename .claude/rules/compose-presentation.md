---
paths:
  - "features/**/presentation/**/*.kt"
  - "core-ui/**/*.kt"
---

# Presentation-layer reminders

Full checklist: `docs/presentation-conventions.md` (read it before creating a new screen, not for small edits).

- Screen contract (`*UiState` / `*Event` / `*Effect`) lives in `presentation/model/*UiContract.kt`.
- Reuse before writing: check `core-ui/.../components/` and `core-ui/.../theme/` for an existing
  component, color, spacing or typography token. Never hardcode colors or dp values that the theme has.
- A composable used by 2+ features moves to `:core-ui`.
