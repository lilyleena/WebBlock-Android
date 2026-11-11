---
paths:
  - "**/*.gradle.kts"
  - "gradle/libs.versions.toml"
---

# Gradle reminders

- Every version goes in `gradle/libs.versions.toml`; modules reference `libs.*` only.
- Ask the user before adding a new library or plugin.
- Compose modules apply `implementation(platform(libs.androidx.compose.bom))`.
- After editing Gradle files, verify with `bash .claude/scripts/gradle.sh assembleDebug`.
