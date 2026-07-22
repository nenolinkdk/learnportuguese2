# Changelog

## Unreleased

### Fixed

- Corrected dialog Previous/Next behavior so crossing dialog boundaries stays deterministic: Next opens the first phrase of the next dialog and Previous opens the last phrase of the previous dialog.
- Removed repeated feedback/helper phrases from children dialog navigation content.
- Normalized children dialogs to the same JSON lesson/dialog/phrase architecture as adult levels.
- Removed silent placeholder/fallback phrase paths for invalid dialog content.
- Standardized visible terminology from "Level" to "Niveau" in app-facing text and user documentation.
- Standardized lesson status lines as `gennemført · klar`, where `klar` means available quiz questions for the lesson.
- Moved dialog breadcrumb/status text to the bottom of dialog and story screens.
- Removed repeated children objective text from dialog screens.

### Added

- Added bundled offline Nenoling user guide for users and parents.
- Added a main-menu book icon that opens the user guide without internet access.
- Added validation script for dialog navigation/content checks.
- Extended validation to cover required lesson/dialog/phrase fields, unique dialog IDs, shared schema assumptions, and 20-step Next/Previous children navigation.
- Expanded children dialogs with age-appropriate dialog content stored in JSON.
- Added discreet main-menu release metadata from Gradle: `Version 0.2.0 · Build 12 · 2026-07-12`.
- Added configurable Nenolink browser link via generated Android resource.
- Added children safety phrases for ordinary police help and emergency 112 use in Portugal.
- Added release-process documentation.
- Added Learn Portuguese 3 as a new JSON-only Nenoling content package under `app/src/main/assets/levels/level4` because `level3` is already used for Children.
- Added 10 lower-intermediate Learn Portuguese 3 lessons with 10 dialogs per lesson, reading texts of approximately 20 lines, expanded vocabulary, grammar explanations, conjugation tables, examples, notes and common mistakes.
- Extended the shared grammar formatter to display JSON `commonMistakes` without adding a separate grammar engine.
- Extended validation to check Learn Portuguese 3 lesson count, dialog count, story length, grammar/conjugation/common-mistake structure, quiz presence and deterministic navigation.

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
