#!/usr/bin/env bash
# Token-saving Gradle wrapper for Claude.
# Runs ./gradlew with the given tasks, keeps the full log on disk, and prints only
# the result plus the lines that matter (compiler errors, test failures, detekt findings).
#
#   bash .claude/scripts/gradle.sh assembleDebug
#   bash .claude/scripts/gradle.sh :features:test checkCode
set -u
cd "$(dirname "$0")/../.." || exit 1
mkdir -p build
log="build/claude-gradle.log"

./gradlew --console=plain --warning-mode=none "$@" >"$log" 2>&1
code=$?

if [ "$code" -eq 0 ]; then
  echo "OK: gradlew $*"
  # Surface Kotlin warnings, capped.
  grep -E '^w: ' "$log" | head -n 15
else
  echo "FAILED (exit $code): gradlew $*"
  grep -E '^e: |error:|FAILED|What went wrong|^> |Caused by|expected:|Exception|\.kt:[0-9]+:[0-9]+|^\s+at com\.kshrd' "$log" \
    | grep -vE '^> Task [^ ]+$|^> Task .* (UP-TO-DATE|NO-SOURCE|SKIPPED|FROM-CACHE)$|^> (Run with|Get more help)' \
    | awk '!seen[$0]++' | head -n 60
fi
echo "(full log: $log — read with offset/limit or grep only if the summary is not enough)"
exit "$code"
