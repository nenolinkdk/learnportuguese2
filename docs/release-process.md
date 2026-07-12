# Release Process

This project keeps release metadata in one place: `app/build.gradle`.

## Version Fields

- `versionName` is changed in `app/build.gradle` under `android.defaultConfig`.
- `versionCode` is changed in `app/build.gradle` under `android.defaultConfig`.
- `releaseDate` is changed in `app/build.gradle` under the top-level `ext` block.
- The app displays these values on the main menu as:

```text
Version 0.2.0 · Build 12 · 2026-07-12
```

The app reads the installed APK's `versionName` and `versionCode` from Android package metadata. Those values are still defined in Gradle. The release date is exposed to Android through the generated `R.string.release_date` resource.

## APK Filename

The normal debug build produces Android's default debug APK. The project also keeps a manual testing copy named:

```text
app/build/outputs/manual-debug/LearnPortuguese2Test.apk
```

The filename is configured by the `copyDebugApkForManualTesting` Gradle task in `app/build.gradle`. Verify it after a build with:

```powershell
Get-ChildItem app/build/outputs/manual-debug/LearnPortuguese2Test.apk
```

## Build

Run from the repository root:

```powershell
.\gradlew.bat tasks
.\gradlew.bat assembleDebug
```

If local Gradle cannot find the Android SDK, build from Android Studio or set `ANDROID_HOME` / `ANDROID_SDK_ROOT` for the current shell.

## Validation

Before publishing an APK, run:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\validate_navigation_content.ps1
```

This checks the shared lesson schema, dialog navigation assumptions, release metadata, Nenolink link wiring, and children safety phrases.

## GitHub Release

1. Commit and push the release branch.
2. Merge the pull request into `main`.
3. Build the APK from the merged `main`.
4. Confirm `LearnPortuguese2Test.apk` exists in `app/build/outputs/manual-debug/`.
5. In GitHub, open **Releases**.
6. Choose **Draft a new release**.
7. Create a tag matching the app version, for example `v0.2.0`.
8. Use release notes from `CHANGELOG.md`.
9. Attach `LearnPortuguese2Test.apk`.
10. Publish the release when the APK has been installed and smoke-tested on a device or emulator.

## Notes

- Keep release metadata synchronized with `CHANGELOG.md` and `README.md`.
- Do not rename the package or launcher configuration during release prep unless the Play Store target explicitly requires it.
- The debug APK name is for manual testing. A Play Store release will require the normal signed release artifact process.
