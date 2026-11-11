---
name: run-app
description: Start the Pixel_8 emulator, install the debug build, launch the app, take a screenshot and read filtered logcat. Use when asked to run the app, see a screen, check a crash, or confirm a UI change on a device.
---

# Run the app on the emulator

SDK: `sdk.dir` in `local.properties` (currently `H:\android-sdk-components`). App id: `com.kshrd.starter`.
Use `$SDK/platform-tools/adb` and `$SDK/emulator/emulator` if they are not on PATH.

1. Device: `adb devices`. If none is listed, start one in the background:
   `"$SDK/emulator/emulator" -avd Pixel_8 -no-snapshot-save` (run_in_background), then
   `adb wait-for-device` and poll `adb shell getprop sys.boot_completed` until it prints `1`.
2. Install: `bash .claude/scripts/gradle.sh installDebug`.
3. Launch: `adb shell am start -n com.kshrd.starter/.MainActivity`
   (if that fails, `adb shell monkey -p com.kshrd.starter 1`).
4. Screenshot: `adb exec-out screencap -p > build/screen.png`, then Read `build/screen.png`.
   One screenshot per check — images are expensive.
5. Logs — never dump full logcat. Use:
   `adb logcat -d -t 300 --pid=$(adb shell pidof -s com.kshrd.starter) *:W`
   For a crash: `adb logcat -d -b crash -t 100`.

Interact with `adb shell input tap X Y` / `input text "..."` / `input keyevent 4` (back).
