learn portuguese 2

This repository contains the Android implementation for Learn Portuguese 2 / Nenoling Portuguese 2.

The app reuses the existing Learn Portuguese Java/Android structure and keeps Level 1 available while adding Level 2 as a separate offline JSON lesson set.

## Android app

- Existing lesson/dialog navigation is reused.
- Portuguese TextToSpeech behavior is preserved.
- Level 1 content lives in `app/src/main/assets/levels/level1`.
- Level 2 content lives in `app/src/main/assets/levels/level2`.
- The app defaults to Level 2 and lets the user switch back to Level 1.

## Documentation

- [Learn Portuguese 2 requirements](docs/requirements/learn-portuguese-2-requirements.md)
- [Learn Portuguese 2 Codex notes](docs/requirements/learn-portuguese-2-codex-notes.md)
- [JSON content structure](docs/requirements/json-content-structure.md)
- [Architecture](docs/architecture.md)
- [Test log](docs/testing/testlog.md)
- [Roadmap](docs/roadmap.md)
- [Privacy](docs/privacy.md)
- [Security](docs/security.md)
