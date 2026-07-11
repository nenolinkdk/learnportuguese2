# Changelog

## Unreleased

### Fixed

- Corrected dialog Previous/Next behavior so crossing dialog boundaries stays deterministic: Next opens the first phrase of the next dialog and Previous opens the last phrase of the previous dialog.
- Removed repeated feedback/helper phrases from children dialog navigation content.

### Added

- Added bundled offline Nenoling user guide for users and parents.
- Added a main-menu book icon that opens the user guide without internet access.
- Added validation script for dialog navigation/content checks.
- Expanded children dialogs with age-appropriate mini-dialog content stored in JSON.

## 1.0 Local Release Candidate

This version is a local APK release candidate for testing and private distribution.

### Added

- 10 offline lessons.
- 10 dialogue entries per lesson.
- Danish to European Portuguese learning direction.
- Local JSON lesson data.
- Android Text-to-Speech for Portuguese pronunciation where available.
- Local progress tracking with latest lesson, latest dialogue and completed dialogues.
- Simple quiz flow with result screen.
- 5 quiz questions per lesson.
- Local quiz score storage.
- Progress overview screen.
- Settings screen with local progress reset.
- Android Studio test checklist.
- Local APK installation guide.

### Notes

- The app is offline-first and has no login, cloud sync, advertising or payment features.
- Lesson 1 still uses the original longer beginner phrase deck and is split into display dialogues in the UI.
- Portuguese Text-to-Speech depends on installed Android voice data.
- Wider distribution still needs final device testing, app icon review and release notes.
