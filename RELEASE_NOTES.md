#  Kunjika Release Notes

[![Version](https://img.shields.io/badge/version-1.9.0-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B)](RELEASE_NOTES.md)
[![Theme](https://img.shields.io/badge/theme-Obsidian%20Gold%20%26%20Midnight%20Indigo-F59E0B?style=flat-square&labelColor=1E1B4B&color=F59E0B)](docs/features.md)
[![Target SDK](https://img.shields.io/badge/target%20SDK-36-818CF8?style=flat-square&labelColor=1E1B4B&color=818CF8)](https://developer.android.com)
[![Platform](https://img.shields.io/badge/min%20Android-8.0%20(API%2026)-34D399?style=flat-square&labelColor=064E3B&color=34D399)](README.md)

> This changelog follows the repository source version. Play Store rollout availability may differ by track. See [security.md](docs/security.md) for current implementation details and limitations.

## v1.9.0 (Current Source Version) - October 19, 2026

### Web Drop Reliability
- Added the Web Companion URL to the phone's Web Drop scanner screen.
- Restricted the camera scanner to QR codes and made it handle one detected code per scan.
- Wait for Android to report successful GATT service registration before starting BLE advertising.
- Clarified that the user authorizes pairing on the phone, then starts the Bluetooth device picker from the computer browser.

### PIN and Vault Responsiveness
- Show progress during Master PIN setup and app unlock, and ignore duplicate submissions while verification is running.
- Do not wait for the first PIN-creation audit entry before opening the vault.
- Move vault list filtering and password-health analysis off the UI thread.

### Current Security Notes
- Newly set PINs use PBKDF2-HMAC-SHA-256 with 600,000 iterations; older hashes are upgraded after successful verification.
- Phone-to-phone QR transfer uses a random 16-digit transfer code, PBKDF2-HMAC-SHA-256 (600,000 iterations), and AES-GCM. Protect the QR and code separately.
- Web Drop uses ephemeral P-256 ECDH, HKDF-SHA-256, AES-GCM, a user-compared code, and explicit phone approval. The hosted page and receiving computer remain part of the trust boundary.
- Browser clipboard copying is user-initiated. Kunjika does not automatically copy or clear the browser clipboard; clipboard managers may retain copies.
- SQLCipher uses a random passphrase wrapped by Android Keystore. The Master PIN gates app access; it is not the database key. Hardware backing varies by device.

## v1.8.0 (October 2026)

### Usability and Privacy
- Added an optional Duress PIN that opens a decoy vault view.
- Added guided tours for the Generator and Vault screens.
- Added visual privacy styling for sensitive credential displays. This is a visual deterrent and does not replace Android's `FLAG_SECURE` or protect against a compromised device.
- Added a delay and cancellation step before exporting a vault backup.

### Performance and Reliability
- Moved database initialization, PIN hashing, backup cryptography, and QR generation off the main thread.
- Reduced repeated QR bitmap pixel operations and removed continuously animated background drawing.
- Moved autofill credential lookup to asynchronous background work.
- Added a startup loading and retry state for vault initialization failures.
- Added a timeout to a root-detection process probe.

### Security Notes at v1.8.0
- Newly set PINs use PBKDF2-HMAC-SHA-256 with 600,000 iterations. Older hashes are upgraded after successful verification.
- Phone-to-phone QR transfer uses a random 16-digit transfer code, PBKDF2-HMAC-SHA-256 (600,000 iterations), and AES-GCM. Protect the QR and code separately.
- Web Drop uses ephemeral P-256 ECDH, HKDF-SHA-256, AES-GCM, a user-compared code, and explicit phone approval. The hosted page and receiving computer remain part of the trust boundary.
- Browser clipboard copying is user-initiated. Kunjika does not automatically copy or clear the browser clipboard; clipboard managers may retain copies.
- SQLCipher uses a random passphrase wrapped by Android Keystore. The Master PIN gates app access; it is not the database key. Hardware backing varies by device.

---

## Historical Release Notes

The entries below record older releases and may describe behavior or parameters that have since changed. Use the current security and feature guides for the current source version.

## v1.1.2 (Luxury UI Polish, TOTP Generator & Environmental Defense) - September 2026

###  Signature Aesthetics & Glossy UI
- **Dual-Theme Architecture**: Introduced the signature **Obsidian Gold & Midnight Indigo** dark theme (rich 24k gold accents `#F59E0B`/`#FBBF24`, cosmic indigo `#818CF8`, and pitch obsidian `#07090E`) alongside a high-contrast **Platinum Silver** light theme.
- **Glassmorphic Glossy Components**: Added a custom `GlossyComponents` design system featuring `GlossyCard`, `GlossyButton`, `GlossyBadge`, and `GlossyTextField` with specular top-shine gradients, frosted borders, and dynamic ambient glows.
- **Revamped Status Badges & Motion**: Redesigned security and status badges with spring transition animations, tactile feedback, and high-visibility status indicators across all screens.
- **Web Companion Dual Theme**: Extended dual-theme toggle (Obsidian Gold & Platinum Silver) to the Web Drop Companion with matching glossy badges.

###  Smart Generator & TOTP Authentication Mode
- **Built-in TOTP Secret Generator**: Added dedicated TOTP generation mode producing RFC 6238-compliant 160-bit Base32 secrets with quick copy and instant vault bookmarking.
- **Persistent Generation History**: Added full history logging for generated credentials, passphrases, and tokens with timestamped entries and one-tap re-use.
- **Controlled Generation Model**: Manual generation workflow ensures tokens and passwords are only generated and saved on explicit user action.

###  Enhanced Security Audit & Environmental Hardening
- **Biometric Unlock**: Added Android Keystore cipher authorization through `BiometricPrompt.CryptoObject`. Hardware backing varies by device, and the PIN may exist temporarily in app memory during use.
- **Real-Time Root Detection**: Deep runtime checks detecting `su` binaries, test-keys builds, Superuser/Magisk package paths, and command execution probes.
- **Emulator Environment Detection**: Heuristic detection of emulated runtime environments (QEMU/goldfish hardware, generic device properties, build fingerprints).
- **Keystore Security-Level Check**: Reports the device-provided key security level where available; this is not independent hardware attestation.
- **Google Play Integrity Integration**: Interactive device integrity tester in the Security Audit screen leveraging the Google Play Integrity API (`playIntegrity = 1.6.0`).

###  Web Drop BLE Transfer Reliability
- **Robust Chunk Reassembly**: Implemented sequence-tagged GATT chunking with notification retry handling and checksum verification for zero data loss during phone-to-PC transfers.
- **P-256 Public Key Transmission**: Ephemeral ECDH key agreement streaming uncompressed 65-byte EC public keys for rapid pairing.
- **Automated Companion CI/CD**: Added GitHub Actions workflow for zero-downtime deployment of the Web Companion to GitHub Pages.

###  Dependency & Build Modernizations
- Upgraded to **Kotlin 2.2.10**, **AGP 9.4.0**, **KSP 2.3.6**, **Room 2.8.4**, and **Compose BOM 2025.02.00**.
- Updated release Proguard/R8 obfuscation mappings for production hardening.
- App Version: `v1.1.2` (Version Code `8`).

---

## v1.1.1 (Google Play Integrity Integration) - March 2026

- **Play Integrity Integration**: Integrated Google Play Integrity API and Automatic Integrity Protection to safeguard against APK tampering and repackaging.
- **Integrity Tester**: Added an interactive Play Integrity token request in the Security Audit screen. This historical feature does not provide a server-validated verdict in the current project because it has no backend verifier.
- **Stability & Performance**: General performance optimizations and underlying dependency updates.

---

## v1.1.0 (Hardware Cryptographic Hardening & Direct Offline Sync) - March 2026

###  Core Security & Cryptographic Hardening
- **Hardware-Bound Biometric Unwrapping**: Biometric unlock now requires cryptographic unwrapping of the Master PIN via Android TEE/StrongBox `BiometricPrompt.CryptoObject` ciphers.
- **Master PIN Verification**: PIN verification uses a salted PBKDF2 hash. The PIN is an app access gate, not the SQLCipher database key; the current database passphrase is randomly generated and wrapped by Android Keystore.
- **Encrypted QR Transfer**: QR payloads use AES-GCM and a PBKDF2-derived key. In the current source, the format uses a random 16-digit transfer code and 600,000 PBKDF2-HMAC-SHA-256 iterations. Older short-code formats were vulnerable to offline guessing.
- **Master PIN Length**: PIN setup now requires at least six digits; use a longer, unique PIN where practical.
- **Environment & Root Security Health**: Added real-time root access detection, runtime sandbox checks, and KeyStore hardware TEE/StrongBox verification to the Security Health screen and Auth Screen.
- **Ciphertext Leak Prevention**: Cryptographic fallbacks in repository layers now return `null` on decryption errors, preventing raw ciphertext from leaking into the UI.

###  Direct Web Drop & Cross-Device Sync
- **Direct Desktop Transfer**: After the hosted companion page is loaded, send a selected credential directly to a compatible browser over Bluetooth, without an account or cloud relay in the transfer path.
- **Web Bluetooth (BLE GATT)**: Phone operates as a BLE GATT peripheral server (`e9a30001-c852-4e08-9bfa-87bb0f592658`), streaming chunked AES-256-GCM ciphertext directly to the browser.
- **Short-Lived Web Drop Sessions**: The companion generates an expiring QR challenge. Proof of work is a lightweight work factor, not authentication; session expiry and consumed-session checks address replay within their stated limits.
- **Ephemeral ECDH P-256 Key Agreement**: In-memory key generation and Diffie-Hellman scalar multiplication with HKDF-SHA256 (`"kunjika-web-drop-v1"`).
- **Pairing-Code Comparison**: Both devices display a transcript-derived six-digit code. Comparing it can help detect session substitution, but does not authenticate the hosted companion page.
- **Local Audit Logging**: Web Drop records an `EXPORT_BLE` event in the local signed audit chain. It is not immutable and cannot guarantee detection of deletion or rollback.
- **Browser Companion**: Available at [https://dwinsi.github.io/kunjika/web-companion/](https://dwinsi.github.io/kunjika/web-companion/). The browser displays the decrypted credential and copies it only after a user action; memory cleanup is best-effort and clipboard contents are not automatically cleared.

---

## v1.0.1 (UI Refinement & Stability) - August 28, 2026

###  Improvements
- **Manual Password Generation**: The password generator no longer auto-generates strings on every setting change. This prevents history pollution and ensures only user-requested passwords are saved.
- **Enhanced UI Logic**: 
    - Added "Generate" and "Regenerate" actions for clearer interaction.
    - Updated copy/save actions to remain disabled until a string is generated.
    - Added placeholder guidance in the generator output box.
- **Internal Optimization**: Refined viewmodel logic to reduce unnecessary background operations.

---

## v1.0.0 (First Official Release) - August 26, 2026

The first official release introduced **Kunjika**, a local-first password manager. The security and connectivity details below describe that historical release and have since changed; see the current sections above for present behavior.

###  Security First
- **Local-First Storage**: The manifest removed the Android `INTERNET` permission. Users could still share or export selected data through app features and Android integrations.
- **Layered Encryption**: SQLCipher protected the local database; selected fields also used Android Keystore AES-GCM. Hardware backing varied by device.
- **Local Signed Audit Chain**: The app recorded selected vault events in a local chain intended to help detect some edits; it could not prevent tampering or guarantee detection of deletion or rollback.
- **PBKDF2 PIN Hashing**: Your Master PIN is protected by 100,000 iterations of PBKDF2 with a unique per-device salt.
- **Biometric Unlock**: Biometric approval used Android platform authentication and Keystore operations where configured; hardware backing varied by device.

---
*Kunjika is developed by Ashwin Singh.*
