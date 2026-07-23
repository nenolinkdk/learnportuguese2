# Test Log

## 2026-07-23 - Level 3 Library Content Feedback

Learn Portuguese 3 library lesson correction after device review.

### Scope

- Rewrote the Level 3 library lesson from Danish workflow sentences first, then short Portuguese variants.
- Removed Danish words from Portuguese text, lower-case dialog starts and generic "ordne/tjekke" style wording from the library lesson.
- Made library phrases active and concrete: applying for a library card, borrowing a novel, returning late, reserving, using the catalogue, using a computer and asking about reading-room rules.
- Moved vocabulary above grammar on dialog screens and added clearer grammar sections with `verbDa` support.
- Documented a reusable Level 3+ content quality checklist.

### Result

Passed `tools/validate_navigation_content.ps1`, bundled JSON parse validation, targeted Level 3 text-quality scan, `git diff --check` and `gradlew.bat assembleDebug`.

The updated APK was copied to `C:\Users\henri\Dropbox\Privat\Nenolink\github\learnportuguese2\app\LearnPortuguese2Test.apk`.

## 2026-07-22 - Learn Portuguese 3 Content Revision

Learn Portuguese 3 content quality and validation pass.

### Scope

- Rewrote Learn Portuguese 3 lessons to use the requested theme set while keeping all content in `levels/level4` JSON.
- Checked that each Learn Portuguese 3 lesson has 10 dialogs and each dialog has exactly 10 phrase entries.
- Added validation for duplicate Portuguese/Danish dialog phrases, repeated fallback/helper phrases, approximately 20-line story texts, grammar/conjugation/common-mistake structure and cautious pharmacy wording.
- Confirmed that Learn Portuguese 3 still uses the shared JSON schema and the shared navigation engine; no separate Level 3 lesson/dialog/grammar engine was introduced.
- Adjusted main menu ordering so Children appears after the adult levels, quiz, numbers and documentation.

### Result

Passed `tools/validate_navigation_content.ps1`, full bundled JSON parse validation, encoding/mojibake scan for the revised Learn Portuguese 3 files and documentation, `gradlew.bat tasks` and `gradlew.bat assembleDebug`.

The debug build produced the normal Android Studio debug APK and the manual test copy at `app/build/outputs/manual-debug/LearnPortuguese2Test.apk`.

## 2026-07-22 - Learn Portuguese 3 JSON Package

Learn Portuguese 3 content package.

### Scope

- Added Learn Portuguese 3 as a JSON-only package in `levels/level4`.
- Validated that the package uses the same lesson, dialog, phrase, vocabulary, grammar, quiz and story schema as existing content.
- Added validator checks for 10 lessons, 10 dialogs per lesson, approximately 20 story lines, grammar explanations, conjugation tables, examples, notes, common mistakes and quiz presence.
- Confirmed deterministic navigation validation across Learn Portuguese 3 dialogs.

### Result

Passed local JSON, navigation and Gradle debug build validation. Android Studio/device smoke test is still recommended before release.

## 2026-07-12 - Terminology, Status, Release Metadata

Consistency and documentation correction.

### Scope

- Replaced visible app/user-guide terminology from "Level" to "Niveau" where applicable.
- Added main-menu release metadata from Gradle values: version name, version code, and release date.
- Added shared lesson status formatter: `gennemført · klar`.
- Added shared dialog/story breadcrumb builder.
- Added configurable Nenolink URL resource and browser intent from the user guide.
- Added children safety phrases for ordinary police contact and emergency 112 use in Portugal.
- Added release-process documentation.

### Validation

- `tools/validate_navigation_content.ps1` now checks schema, children safety phrases, breadcrumb/status helpers, version/build/date metadata, and Nenolink link wiring.
- Emergency-number wording was checked against the European Commission page "112 - the EU's emergency phone number": `https://digital-strategy.ec.europa.eu/en/policies/112`.

### Result

Pending final Android Studio build/device smoke test.

## 2026-07-12

Children level schema and navigation validation.

### Scope

- Normalized children dialogs so they use the same lesson/dialog/phrase/vocabulary JSON schema as adult levels.
- Removed generated children filler phrases that made unrelated text appear after opening a new dialog.
- Removed silent dialog placeholder/fallback phrase paths from the app UI.
- Strengthened `tools/validate_navigation_content.ps1`.

### Validation

- `tools/validate_navigation_content.ps1` checks lesson IDs/titles, dialog IDs/titles, unique dialog IDs, phrase `textPt`/`textDa`, children vocabulary, and shared JSON schema assumptions.
- The script simulates at least 20 consecutive Next presses and 20 consecutive Previous presses across children dialogs.
- All bundled JSON files parse as valid JSON.

### Result

Passed local content/navigation validation. Ready for Android Studio build and device testing.

## 2026-07-11

Dialog navigation and children-content correction.

### Scope

- Fixed deterministic Previous/Next navigation across dialog boundaries.
- Removed repeated helper phrases from children dialog navigation content.
- Added offline Nenoling user guide asset and main-menu guide entry.
- Added validation script: `tools/validate_navigation_content.ps1`.

### Validation

- `tools/validate_navigation_content.ps1` verifies children dialogs have phrase content and do not contain helper phrases such as repeated feedback lines.
- The validation also checks that next-dialog navigation lands on a non-empty first phrase and previous-dialog navigation lands on a non-empty last phrase.

### Result

Ready for Android Studio build and device testing.

## 2026-07-09

Documentation-only update.

### Scope

- Added Learn Portuguese 2 requirements documentation.
- Added Codex implementation notes.
- Added roadmap, privacy, security, and test log documents.
- Updated README links.

### Validation

- No app logic was changed.
- No Android code was changed.
- No JSON lesson data was changed.
- No OAuth configuration was changed.
- No database schema was changed.
- No build settings were changed.
- Android build was not run because this change only touches Markdown documentation.

### Result

Pending pull request review.
