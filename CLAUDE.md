# CLAUDE.md

All instructions for this repository live in `AGENTS.md` (imported below — edit that file, not this one).
Architecture rules: `docs/ARCHITECTURE.md`. Screen checklist: `docs/presentation-conventions.md`.
Adding a feature: `docs/ADDING_A_FEATURE.md`. Read those docs only when the task needs them.

@AGENTS.md

## Claude Code specifics

- Build/test/lint through `bash .claude/scripts/gradle.sh <tasks>` (compact output), never raw `./gradlew`.
  Use the narrowest module task that covers the change (see the `verify` skill).
- Never read `build/`, `.gradle/`, `.idea/` or generated Hilt/KSP sources. Search with Grep + a glob
  (e.g. `features/**/*.kt`) and read files with offset/limit instead of whole large files.
- Skills: `new-feature`, `verify`, `run-app`. Reviewer subagent: `android-reviewer`.
