# Security

Learn Portuguese / Nenoling should remain a small, low-risk Android learning app.

## Principles

- Keep the app offline-first.
- Avoid unnecessary Android permissions.
- Do not store secrets in the repository.
- Do not embed API keys in Android source code or JSON lesson data.
- Keep lesson data as static content unless a future feature requires otherwise.

## Current Expected Risk

The current app direction uses local lesson content and Android TextToSpeech. It should not require authentication, OAuth, a database, or remote API secrets.

## Future Features

If future versions add cloud sync, accounts, analytics, payments, AI calls, or a backend database, add a security review before release.

Review:

- Authentication and authorization
- Secret management
- Data storage
- Network transport security
- Logging of user input
- Third-party SDKs

## Release Checklist

- No secrets committed to the repository.
- No unnecessary permissions in the Android manifest.
- No debug-only endpoints or keys in release builds.
- Privacy documentation matches actual app behavior.
