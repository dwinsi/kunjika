---
layout: default
---

# Kunjika

### Local-first password manager and authenticator for Android

[![Version](https://img.shields.io/badge/version-1.8.0-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B)](./RELEASE_NOTES.html)
[![Platform](https://img.shields.io/badge/platform-Android%208%2B%20(API%2026)-818CF8?style=flat-square&labelColor=1E1B4B&color=818CF8)](https://developer.android.com)
[![License](https://img.shields.io/badge/license-MIT-FBBF24?style=flat-square&labelColor=07090E&color=FBBF24)](./LICENSE)

**Local-first vault. No Kunjika account or cloud sync required.** Kunjika stores an encrypted vault on your Android device. You choose when to copy, export, or transfer selected information.

[Install from Google Play](https://play.google.com/store/apps/details?id=com.kunjika.app) · [Read the user guide](./docs/user-guide.html) · [Privacy policy](./PRIVACY_POLICY.html)

## What Kunjika does

- Store account logins in an encrypted local database.
- Generate passwords, multi-word passphrases, numeric PINs, and TOTP setup secrets.
- Use Android Autofill with supported apps and websites.
- Save TOTP secrets with vault entries and generate rotating verification codes.
- Organize entries by category, mark favorites, search, and add notes.
- Review password strength, reuse, age, and missing TOTP coverage.
- Create passphrase-protected encrypted backups.
- Transfer a selected credential between Android devices using encrypted QR.
- Use Web Drop to send a selected credential over Bluetooth to a browser after pairing-code comparison and phone approval.
- Set a master PIN, optionally use biometric unlock, and configure a duress PIN that opens a decoy view.
- View selected security checks and a local signed audit chain.

## Local-first by design

Kunjika does not provide accounts or cloud synchronization. The Android app manifest removes the `INTERNET` permission. The app also offers an optional Google Play Integrity token request through Google Play services; it has no backend to validate that token. Selected data can still be shared through Android Autofill, clipboard, exports, QR, and Bluetooth.

The vault uses SQLCipher with a random database passphrase wrapped by Android Keystore. Password and TOTP-secret fields also use AES-GCM. Android Keystore hardware backing depends on the device. The master PIN is an app access gate, not the database key; a rooted or otherwise compromised device can bypass app-level controls.

## Web Drop

Web Drop uses Bluetooth Low Energy for the transfer after the companion page is loaded. It uses ephemeral P-256 ECDH, HKDF-SHA-256, AES-GCM, and a six-digit comparison code shown on both devices. The user compares the codes, confirms in the browser, and approves sending on the phone.

The hosted companion page is mutable, and the browser must decrypt the credential to display it. Copying is user initiated; clipboard managers can retain copied values. Closing the transfer view clears visible fields and drops references on a best-effort basis, but JavaScript cannot guarantee memory erasure. Use a trusted computer, browser, and companion page.

## Data protection and recovery

Encrypted vault exports use a passphrase and can be imported on another device. Keep the export and its passphrase separately. The local database recovery PDF contains the database passphrase; it is unencrypted and does not restore entries or Keystore field keys to a new device.

Kunjika's security checks are advisory. Root and emulator heuristics can miss compromised environments. The local signed audit chain can help detect some edits but is not immutable and cannot guarantee detection of deletion, rollback, or app compromise. `FLAG_SECURE` blocks ordinary screenshots and hides the recents preview, but cannot stop every capture method.

## Project resources

- [User guide](./docs/user-guide.html)
- [Feature guide](./docs/features.html)
- [Security design and limitations](./docs/security.html)
- [Architecture](./docs/architecture.html)
- [Release notes](./RELEASE_NOTES.html)
- [Web Drop companion](./web-companion/)
- [Project website](https://dwinsi.github.io/kunjika/)
- [Report a bug or request a feature](https://github.com/dwinsi/kunjika/issues)

Kunjika is open-source software licensed under MIT. See [LICENSE](./LICENSE).
