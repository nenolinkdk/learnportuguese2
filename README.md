learn portuguese 2

This repository contains the Android implementation for Learn Portuguese 2 / Nenoling Portuguese 2.

The app reuses the existing Learn Portuguese Java/Android structure and keeps Niveau 1 available while adding Niveau 2 as a separate offline JSON lesson set.

## Android app

- Existing lesson/dialog navigation is reused.
- Portuguese TextToSpeech behavior is preserved.
- Niveau 1 content lives in `app/src/main/assets/levels/level1`.
- Niveau 2 content lives in `app/src/main/assets/levels/level2`.
- Børneniveauet ligger i `app/src/main/assets/levels/level3`.
- The bundled Nenoling user guide lives in `app/src/main/assets/docs/user_guide.json`.
- The app defaults to Niveau 2 and lets the user choose Niveau 1, Niveau 2, børneniveau, quizzes, numbers and the offline guide.
- Current release metadata is maintained in `app/build.gradle`: `versionName 0.2.0`, `versionCode 12`, and release date `2026-07-12`.
- The configurable Nenolink website URL is generated from `app/build.gradle` as `https://www.nenolink.dk`.
- The manual debug APK copy remains `app/build/outputs/manual-debug/LearnPortuguese2Test.apk`.

## Validation

- `tools/validate_navigation_content.ps1` validates deterministic dialog navigation assumptions and checks that helper/feedback phrases are not stored as children dialog content.
- The same script checks shared lesson status formatting, dialog breadcrumb metadata, release metadata, Nenolink link wiring, and children safety phrases.

## Documentation

- [Learn Portuguese 2 requirements](docs/requirements/learn-portuguese-2-requirements.md)
- [Learn Portuguese 2 Codex notes](docs/requirements/learn-portuguese-2-codex-notes.md)
- [JSON content structure](docs/requirements/json-content-structure.md)
- [Architecture](docs/architecture.md)
- [Release process](docs/release-process.md)
- [Test log](docs/testing/testlog.md)
- [Roadmap](docs/roadmap.md)
- [Privacy](docs/privacy.md)
- [Security](docs/security.md)
