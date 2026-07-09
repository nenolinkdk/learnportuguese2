# Learn Portuguese 2 Codex Notes

## Scope Guardrails

This documentation change should only touch Markdown documentation.

Do not change:

- App logic
- Android code
- JSON lesson data
- OAuth configuration
- Database schema
- Build settings

## Project Direction

Learn Portuguese / Nenoling should stay simple and beginner-friendly:

- One phrase at a time
- Portuguese phrase text
- Danish translation
- Beginner grammar note
- Small glossary
- Previous/Next navigation
- Pronunciation through Android TextToSpeech
- Offline-first content

## Future Implementation Notes

- Reuse Learn Portuguese 1 structure before adding new frameworks.
- Keep lesson content data-driven.
- Prefer JSON for lessons, dialogs, phrases, notes, and vocabulary.
- Initialize TTS only when the user presses the pronunciation button.
- Prefer `pt-PT` for Portuguese pronunciation and handle fallback gracefully.
- Keep Danish explanations short and practical.
- Do not claim a successful Android build unless Gradle, Android Studio, or CI has actually validated it.

## Documentation Map

- Requirements: `docs/requirements/learn-portuguese-2-requirements.md`
- Codex notes: `docs/requirements/learn-portuguese-2-codex-notes.md`
- Test log: `docs/testing/testlog.md`
- Roadmap: `docs/roadmap.md`
- Privacy: `docs/privacy.md`
- Security: `docs/security.md`
