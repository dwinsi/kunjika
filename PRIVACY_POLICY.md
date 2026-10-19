#  Privacy Policy for Kunjika

[![Version](https://img.shields.io/badge/version-1.9.0-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B)](RELEASE_NOTES.md)
[![Theme](https://img.shields.io/badge/theme-Obsidian%20Gold%20%26%20Midnight%20Indigo-F59E0B?style=flat-square&labelColor=1E1B4B&color=F59E0B)](docs/features.md)
[![Privacy](https://img.shields.io/badge/privacy-Local--First-34D399?style=flat-square&labelColor=064E3B&color=34D399)](PRIVACY_POLICY.md)
[![Telemetry](https://img.shields.io/badge/telemetry-0%25%20Trackers-818CF8?style=flat-square&labelColor=1E1B4B&color=818CF8)](PRIVACY_POLICY.md)

**Effective Date: August 26, 2026**  
**Last Updated: October 19, 2026 (source version 1.9.0, version code 19)**

Kunjika is a local-first password manager. The Android app removes the `INTERNET` permission and does not operate cloud accounts or sync servers. Bluetooth, QR transfers, operating-system integrations, and any receiving device are explicit local data-sharing paths.

---

### 1. Accounts, Analytics, and Data Sharing
Kunjika does not operate an account, analytics, or cloud-sync service. Vault contents are stored locally unless you explicitly copy, export, or transfer selected data. Device-level services may process information according to their own privacy terms.
- **No Android Internet Permission**: The application manifest removes `android.permission.INTERNET`. The app can still share data over Bluetooth, QR, clipboard, and Android intents; those paths require user care.
- **No Analytics or Trackers**: We include zero third-party analytics libraries, tracking SDKs, or crash reporting telemetry.
- **No Cloud Synchronization**: Kunjika does not operate cloud servers, synchronization relays, or user accounts. Vault data is stored locally using SQLCipher and Android Keystore protection; hardware backing depends on the device.

---

### 2. Cryptographic Protection of Your Data
Vault protection uses SQLCipher and Android Keystore, with additional AES-GCM encryption for password and TOTP-secret fields. Hardware backing is device-dependent:
- **At Rest**: Password and TOTP-secret fields use AES-256-GCM with Android Keystore keys. The database uses SQLCipher with a random passphrase wrapped by Android Keystore. Other metadata is protected by database encryption, not separately field-encrypted. Hardware backing varies by device. The Master PIN is an app access gate, not the SQLCipher key.
- **Biometric App Access**: The app can request authentication through Android `BiometricPrompt`. If an encrypted PIN payload exists, the app attempts to unwrap and verify it. With no saved payload, the current UI can mark the app authenticated after the biometric prompt without verifying the Master PIN. This is an app access convenience, not a guarantee against device or app-process compromise.
- **In Transit (Offline Sync & Web Drop)**:
  - **Phone-to-Phone QR Sync**: Encrypted using AES-256-GCM with a PBKDF2-HMAC-SHA-256-derived key (600,000 iterations) and a random 16-digit transfer code. The app displays the QR and code in separate views. Protect both from photographs and observers.
  - **Phone-to-PC Web Drop**: AES-256-GCM keys are derived through transcript-bound HKDF-SHA-256 from ephemeral P-256 ECDH over BLE. The user must compare a six-digit code on both screens and approve the transfer. Use only a trusted browser and computer.

---

### 3. Device Permissions Used & Justification
Kunjika requests only the minimal local permissions required for security and core functionality:

- **Biometric (`USE_BIOMETRIC`, legacy `USE_FINGERPRINT`)**: To authorize optional biometric unlock and use the Android Keystore biometric cipher. Whether the key is backed by secure hardware depends on the device.
- **Camera (`CAMERA`)**: Used exclusively for scanning local pairing and session QR codes during Offline QR Sync and Web Drop. Camera frames are processed in real-time in memory via CameraX and ML Kit; no photos or videos are ever saved to disk.
- **Bluetooth (`BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`, and legacy `BLUETOOTH` / `BLUETOOTH_ADMIN` through Android 11)**: Used to advertise a local BLE GATT service and stream encrypted credentials directly to a nearby browser during a user-initiated Web Drop session. Advertising begins after phone authorization and ends when the transfer session ends.
- **Vibrate (`VIBRATE`)**: Provides tactile haptic confirmation during PIN input and biometric operations.

---

### 4. Device and App Integrity Checks
Kunjika includes root and emulator heuristics and can request a token through the **Google Play Integrity API**. The app has no backend to verify that token, so the feature does not provide a server-validated integrity verdict. These checks are advisory and cannot guarantee that a device or app process is uncompromised.

---

### 5. Web Companion Privacy (Web Drop)
The Kunjika Web Companion ([https://dwinsi.github.io/kunjika/web-companion/](https://dwinsi.github.io/kunjika/web-companion/)) runs its transfer code in the browser and uses Bluetooth to receive a selected credential. It stores the theme preference in browser `localStorage`. The credential is displayed after transfer and copied only after a user action. Closing/resetting the view clears visible fields and drops references on a best-effort basis; JavaScript cannot guarantee memory erasure or delete clipboard-manager history. The hosted page is mutable, so use a trusted page source, browser, and receiving computer.

---

### 6. Changes to This Policy
Because Kunjika is an offline application, any policy updates will be distributed transparently within updated versions of the application and posted to the official repository.

---

### 7. Contact
If you have questions regarding this Privacy Policy or Kunjika's security architecture, please open an issue or discussion on the official GitHub repository: [https://github.com/dwinsi/kunjika](https://github.com/dwinsi/kunjika).
