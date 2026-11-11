---
name: verify
description: Build, unit-test and lint the Android project with compact output. Use after code changes, before saying work is done, or when the user asks to build/test/check the code.
---

# Verify

Always use the quiet wrapper — never raw `./gradlew` (its output wastes thousands of tokens).

Pick the narrowest scope that covers the change:

| Changed | Run |
|---|---|
| only `features/` | `bash .claude/scripts/gradle.sh :features:compileDebugKotlin :features:testDebugUnitTest` |
| only `core/` or `core-ui/` | same with `:core` / `:core-ui` |
| Gradle files, DI, navigation, `app/` | `bash .claude/scripts/gradle.sh assembleDebug` |
| before a PR / "full check" | `bash .claude/scripts/gradle.sh assembleDebug test checkCode` |

Formatting: if ktlint complains, run `bash .claude/scripts/gradle.sh ktlintFormat` instead of
fixing whitespace by hand.

On failure: fix from the summary lines. Open `build/claude-gradle.log` only with Grep or a
small offset/limit, never in full. Report pass/fail honestly with the failing line.
