# Release Process

This project keeps release metadata in one place: `app/build.gradle`.

## Version Fields

- `versionName` is changed in `app/build.gradle` under `android.defaultConfig`.
- `versionCode` is changed in `app/build.gradle` under `android.defaultConfig`.
- `releaseDate` is changed in `app/build.gradle` under the top-level `ext` block.
- The app displays these values on the main menu as:

```text
Version 0.2.1 - Build 13 - 2026-08-25
```

The app reads the installed APK's `versionName` and `versionCode` from Android package metadata. Those values are still defined in Gradle. The release date is exposed to Android through the generated `R.string.release_date` resource.

## APK Filename

The normal debug build produces Android's default debug APK for Android Studio Run:

```text
app/build/outputs/apk/debug/app-debug.apk
```

After a successful debug build, the `copyDebugApkForManualTesting` Gradle task copies that APK to a manual testing filename without replacing the standard Android Studio output:

```text
app/build/outputs/apk/debug/LearnPortuguese2Test.apk
```

## Clean Android Build And APK Verification

Run from the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\validate_navigation_content.ps1
.\gradlew.bat clean
.\gradlew.bat tasks
.\gradlew.bat assembleDebug
```

If local Gradle cannot find the Android SDK, build from Android Studio or set `ANDROID_HOME` / `ANDROID_SDK_ROOT` for the current shell.

Verify that the rebuilt APK is not an old renamed artifact:

```powershell
Get-ChildItem app/build/outputs/apk/debug/LearnPortuguese2Test.apk
jar tf app/build/outputs/apk/debug/LearnPortuguese2Test.apk | Select-String "AndroidManifest.xml|classes.dex|resources.arsc|assets/levels/level1|assets/levels/level2|assets/levels/level3|assets/levels/level4"
```

If Android SDK build-tools are available, also check the debug signature and launcher package:

```powershell
$env:ANDROID_HOME="$env:LOCALAPPDATA\Android\Sdk"
& "$env:ANDROID_HOME\build-tools\<version>\apksigner.bat" verify --print-certs app/build/outputs/apk/debug/LearnPortuguese2Test.apk
& "$env:ANDROID_HOME\build-tools\<version>\aapt.exe" dump badging app/build/outputs/apk/debug/LearnPortuguese2Test.apk
```

Install manually with adb when an emulator or phone is connected:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app/build/outputs/apk/debug/LearnPortuguese2Test.apk
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell cmd package resolve-activity --brief dk.nenolink.learnportuguese2
```

The app should appear in the launcher as `LearnPortuguese2` / `Learn Portuguese 2` and start from `dk.nenolink.learnportuguese2/.MainActivity`.

## Validation

`tools/validate_navigation_content.ps1` checks the shared lesson schema, dialog navigation assumptions, release metadata, Nenolink link wiring, and children safety phrases.

## GitHub Release

1. Commit and push the release branch.
2. Merge the pull request into `main`.
3. Build the APK from the merged `main`.
4. Confirm `LearnPortuguese2Test.apk` exists in `app/build/outputs/apk/debug/`.
5. In GitHub, open **Releases**.
6. Choose **Draft a new release**.
7. Create a tag matching the app version, for example `v0.2.1`.
8. Use release notes from `CHANGELOG.md`.
9. Attach `LearnPortuguese2Test.apk`.
10. Publish the release when the APK has been installed and smoke-tested on a device or emulator.

## Future Release Builds

- Keep release metadata synchronized with `CHANGELOG.md` and `README.md`.
- Do not rename the package or launcher configuration during release prep unless the Play Store target explicitly requires it.
- Keep Android Studio Run on the standard `app-debug.apk` output.
- Use the copied `LearnPortuguese2Test.apk` only for manual debug testing.
- A Play Store release must use the normal signed release artifact process, not the debug APK.
