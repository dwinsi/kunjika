# Kunjika: Product Positioning & Promotion Guide

**Positioning:** Kunjika is a local-first Android password manager and authenticator for people who want an encrypted vault on their device and control over when they share credentials.

## Core message

**Local-first vault. No Kunjika account or cloud sync required.**

Kunjika combines an encrypted on-device vault, password and passphrase generation, Android Autofill, TOTP codes, encrypted backups, encrypted QR transfer, and Bluetooth Web Drop. The Android app manifest removes the `INTERNET` permission. The app can request a Play Integrity token through Google Play services, but this project has no backend to verify a trusted verdict.

Describe Kunjika as an alternative for users who prefer local storage and user-initiated transfers. Avoid implying that cloud password managers are inherently insecure or that Kunjika is more secure than named competitors. Different products make different tradeoffs around synchronization, recovery, convenience, and trust.

## What users can do

| User need | Kunjika feature |
| --- | --- |
| Create unique login credentials | Generate passwords with configurable character sets and length, multi-word passphrases, and numeric PINs. |
| Organize account access | Store logins in the encrypted local database; search, filter by category, mark favorites, and add notes. |
| Fill logins on Android | Enable Kunjika as an Android Autofill service for supported apps and websites. |
| Use authenticator codes | Store TOTP secrets with entries and generate rotating codes. Keeping both factors together is convenient, while separate apps provide more separation. |
| Transfer between Android phones | Use encrypted QR transfer with a random 16-digit code. Keep the code separate from the QR. |
| Transfer a selected login to a computer | Use Web Drop over Bluetooth after comparing the session codes and approving the send on the phone. |
| Back up or migrate a vault | Export an encrypted backup protected by a passphrase and import it on another device. The local database recovery PDF is not a portable vault backup. |
| Review password habits | Scan for weak, reused, aging, and entries without TOTP coverage. These are advisory checks. |

## Web Drop: explain the flow honestly

Web Drop helps avoid typing a long password on a computer. The user opens the hosted browser companion, scans its session QR code with Kunjika, authenticates on the phone, compares the six-digit code on both screens, confirms in the browser, and approves sending on the phone. The phone and browser exchange the selected credential directly over Bluetooth; there is no cloud relay in the transfer path.

The companion page is hosted remotely and is mutable. The browser must decrypt the credential to show it, so a compromised page, browser, computer, or extension can read it. Copying is user initiated. Clipboard history may retain copied values, and browser memory cleanup is best effort.

## Privacy and security wording

Use precise language:
- Say “local-first” and “no Kunjika account or cloud sync,” not “air-gapped” or “100% offline.”
- The app manifest removes Android's `INTERNET` permission. Selected data can still be copied, exported, autofilled, or transferred.
- SQLCipher encrypts the local database with a random passphrase wrapped by Android Keystore. Password and TOTP-secret fields also use AES-GCM; other metadata is protected by database encryption rather than individual field encryption.
- Android Keystore hardware backing varies by device. The master PIN is an app access gate, not the SQLCipher database key.
- Root and emulator checks are heuristics. The local signed audit chain can detect some edits, but it is not immutable and cannot guarantee detection of deletion or rollback.
- `FLAG_SECURE` blocks ordinary screenshots and recents previews, but cannot prevent every capture method or protect a compromised operating system.
- A Duress PIN opens a decoy view. Do not promise that it can defeat coercion or forensic access.

For current cryptographic parameters and implementation details, refer to [the security guide](security.md) and [the feature guide](features.md).

## Short promotional copy

### One-line description

A local-first Android password vault with Autofill, password tools, and TOTP codes.

### Short pitch

Kunjika keeps an encrypted password vault on your Android device, with no account or cloud sync required. Generate passwords and passphrases, use Autofill, manage TOTP codes, and make encrypted backups. When you choose to share a credential, use encrypted QR transfer or Bluetooth Web Drop.

### Social post

Meet Kunjika: a local-first password manager and authenticator for Android. Store logins in an encrypted on-device vault, generate passwords and passphrases, use Autofill, and manage TOTP codes. No Kunjika account or cloud sync required. You choose when to export or transfer data. [Get it on Google Play](https://play.google.com/store/apps/details?id=com.kunjika.app)

## Organic promotion ideas

- Share short demonstrations of password generation, Autofill, encrypted backup, and Web Drop.
- Explain local-first storage and its tradeoff: transfers and recovery are user-managed.
- Share the project site, screenshots, user guide, and privacy policy in relevant Android and privacy communities, following each community's self-promotion rules.
- Invite honest feedback. Do not offer incentives for ratings or reviews, and never ask users to leave only positive reviews.
- Review Play Console search terms and store-listing conversion data before changing keywords or screenshots.

## References

- [Google Play listing](https://play.google.com/store/apps/details?id=com.kunjika.app)
- [Project website](https://dwinsi.github.io/kunjika/)
- [User guide](user-guide.md)
- [Security guide](security.md)
- [Privacy policy](../PRIVACY_POLICY.md)
