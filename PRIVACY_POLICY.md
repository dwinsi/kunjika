# 🔒 Privacy Policy for Kunjika

[![Version](https://img.shields.io/badge/version-1.1.2-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B)](RELEASE_NOTES.md)
[![Theme](https://img.shields.io/badge/theme-Obsidian%20Gold%20%26%20Midnight%20Indigo-F59E0B?style=flat-square&labelColor=1E1B4B&color=F59E0B)](docs/features.md)
[![Privacy](https://img.shields.io/badge/privacy-Zero--Network%20%2F%20Zero--Trust-34D399?style=flat-square&labelColor=064E3B&color=34D399)](PRIVACY_POLICY.md)
[![Telemetry](https://img.shields.io/badge/telemetry-0%25%20Trackers-818CF8?style=flat-square&labelColor=1E1B4B&color=818CF8)](PRIVACY_POLICY.md)

**Effective Date: August 26, 2026**  
**Last Updated: September 17, 2026 (v1.1.2)**

Kunjika is a local-first password manager. The Android app removes the `INTERNET` permission and does not operate cloud accounts or sync servers. Bluetooth, QR transfers, operating-system integrations, and any receiving device are explicit local data-sharing paths.

---

### 1. No Data Collection or Telemetry
Kunjika **does not collect, transmit, log, or share** any personal information, passwords, financial data, or usage metrics.
- **No Android Internet Permission**: The application manifest removes `android.permission.INTERNET`. The app can still share data over Bluetooth, QR, clipboard, and Android intents; those paths require user care.
- **No Analytics or Trackers**: We include zero third-party analytics libraries, tracking SDKs, or crash reporting telemetry.
- **No Cloud Synchronization**: Kunjika does not operate cloud servers, synchronization relays, or user accounts. All vault data resides exclusively in local, hardware-encrypted storage on your physical device.

---

### 2. Cryptographic Protection of Your Data
All information handled by Kunjika is shielded by multi-layered, hardware-backed encryption:
- **At Rest**: Selected credential fields use AES-256-GCM with Android Keystore keys, and the database uses SQLCipher with a random passphrase wrapped by Android Keystore. Keystore hardware backing varies by device. The Master PIN is an app access gate, not the SQLCipher key.
- **Biometric Protection**: Biometric unlock uses hardware-level cryptographic key wrapping via `BiometricPrompt.CryptoObject`. Your Master PIN is encrypted directly by the KeyStore; plaintext is never persisted in storage.
- **In Transit (Offline Sync & Web Drop)**:
  - **Phone-to-Phone QR Sync**: Encrypted using AES-256-GCM with a PBKDF2-derived key and a random 16-digit transfer code. The app displays the QR and code in separate views. Protect both from photographs and observers.
  - **Phone-to-PC Web Drop**: AES-256-GCM keys are derived through transcript-bound HKDF-SHA-256 from ephemeral P-256 ECDH over BLE. The user must compare a six-digit code on both screens and approve the transfer. Use only a trusted browser and computer.

---

### 3. Device Permissions Used & Justification
Kunjika requests only the minimal local permissions required for security and core functionality:

- **Biometric (`USE_BIOMETRIC`)**: To authorize vault access and cryptographically unwrap the Master PIN using the device's secure hardware.
- **Camera (`CAMERA`)**: Used exclusively for scanning local pairing and session QR codes during Offline QR Sync and Web Drop. Camera frames are processed in real-time in memory via CameraX and ML Kit; no photos or videos are ever saved to disk.
- **Bluetooth (`BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`)**: Used exclusively to broadcast a local BLE GATT peripheral service and stream encrypted credentials directly to your nearby laptop/desktop browser during an active Web Drop session. Bluetooth is deactivated immediately after transfer and is never used for beaconing, location tracking, or telemetry.
- **Vibrate (`VIBRATE`)**: Provides tactile haptic confirmation during PIN input and biometric operations.

---

### 4. Integrity & Anti-Tamper Attestation
Kunjika includes integration with the **Google Play Integrity API** to offer an interactive security health check. This service verifies app authenticity and detects whether the binary has been modified or repackaged. Play Integrity attestation tokens are evaluated locally between Google Play Services and on-device OS primitives; no credentials, vault entries, or user data are included in integrity requests.

---

### 5. Web Companion Privacy (Web Drop)
The Kunjika Web Companion ([https://dwinsi.github.io/kunjika/web-companion/](https://dwinsi.github.io/kunjika/web-companion/)) is completely client-side:
- Executes 100% inside your local browser's volatile memory (RAM).
- Sets zero cookies, employs zero web tracking/telemetry, and writes zero data to persistent browser storage (`localStorage`, `sessionStorage`, or `IndexedDB`).
- Initiates an automatic 30-second visual countdown upon copying credentials, after which all decrypted values are wiped from browser memory and shredded from the operating system clipboard.

---

### 6. Changes to This Policy
Because Kunjika is an offline application, any policy updates will be distributed transparently within updated versions of the application and posted to the official repository.

---

### 7. Contact
If you have questions regarding this Privacy Policy or Kunjika's security architecture, please open an issue or discussion on the official GitHub repository: [https://github.com/dwinsi/kunjika](https://github.com/dwinsi/kunjika).
