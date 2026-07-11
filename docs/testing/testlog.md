# Test Log

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
