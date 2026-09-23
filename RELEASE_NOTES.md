# 📋 Kunjika Release Notes

[![Version](https://img.shields.io/badge/version-1.1.2-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B)](RELEASE_NOTES.md)
[![Theme](https://img.shields.io/badge/theme-Obsidian%20Gold%20%26%20Midnight%20Indigo-F59E0B?style=flat-square&labelColor=1E1B4B&color=F59E0B)](docs/features.md)
[![Platform](https://img.shields.io/badge/platform-Android%2015%2B%20(API%2035)-818CF8?style=flat-square&labelColor=1E1B4B&color=818CF8)](https://developer.android.com)
[![Build](https://img.shields.io/badge/build-Passing%20(Kotlin%202.2.10)-34D399?style=flat-square&labelColor=064E3B&color=34D399)](RELEASE_NOTES.md)

## v1.1.2 (Luxury UI Polish, TOTP Generator & Environmental Defense) - September 2026

### 🎨 Signature Aesthetics & Glossy UI
- **Dual-Theme Architecture**: Introduced the signature **Obsidian Gold & Midnight Indigo** dark theme (rich 24k gold accents `#F59E0B`/`#FBBF24`, cosmic indigo `#818CF8`, and pitch obsidian `#07090E`) alongside a high-contrast **Platinum Silver** light theme.
- **Glassmorphic Glossy Components**: Added a custom `GlossyComponents` design system featuring `GlossyCard`, `GlossyButton`, `GlossyBadge`, and `GlossyTextField` with specular top-shine gradients, frosted borders, and dynamic ambient glows.
- **Revamped Status Badges & Motion**: Redesigned security and status badges with spring transition animations, tactile feedback, and high-visibility status indicators across all screens.
- **Web Companion Dual Theme**: Extended dual-theme toggle (Obsidian Gold & Platinum Silver) to the Web Drop Companion with matching glossy badges.

### 🎯 Smart Generator & TOTP Authentication Mode
- **Built-in TOTP Secret Generator**: Added dedicated TOTP generation mode producing RFC 6238-compliant 160-bit Base32 secrets with quick copy and instant vault bookmarking.
- **Persistent Generation History**: Added full history logging for generated credentials, passphrases, and tokens with timestamped entries and one-tap re-use.
- **Controlled Generation Model**: Manual generation workflow ensures tokens and passwords are only generated and saved on explicit user action.

### 🛡️ Enhanced Security Audit & Environmental Hardening
- **Biometric-Backed PIN Storage**: Hardware-bound encryption for the Master PIN using Android KeyStore TEE ciphers (`BiometricPrompt.CryptoObject`), enabling instant cryptographic unlock without exposing plaintext in RAM or storage.
- **Real-Time Root Detection**: Deep runtime checks detecting `su` binaries, test-keys builds, Superuser/Magisk package paths, and command execution probes.
- **Emulator Environment Detection**: Heuristic detection of emulated runtime environments (QEMU/goldfish hardware, generic device properties, build fingerprints).
- **KeyStore Hardware Attestation**: Active verification of whether cryptographic keys are housed inside secure hardware (TEE/StrongBox).
- **Google Play Integrity Integration**: Interactive device integrity tester in the Security Audit screen leveraging the Google Play Integrity API (`playIntegrity = 1.6.0`).

### ⚡ Web Drop BLE Transfer Reliability
- **Robust Chunk Reassembly**: Implemented sequence-tagged GATT chunking with notification retry handling and checksum verification for zero data loss during phone-to-PC transfers.
- **P-256 Public Key Transmission**: Ephemeral ECDH key agreement streaming uncompressed 65-byte EC public keys for rapid pairing.
- **Automated Companion CI/CD**: Added GitHub Actions workflow for zero-downtime deployment of the Web Companion to GitHub Pages.

### 📦 Dependency & Build Modernizations
- Upgraded to **Kotlin 2.2.10**, **AGP 9.4.0**, **KSP 2.3.6**, **Room 2.8.4**, and **Compose BOM 2025.02.00**.
- Updated release Proguard/R8 obfuscation mappings for production hardening.
- App Version: `v1.1.2` (Version Code `8`).

---

## v1.1.1 (Google Play Integrity & App Attestation) - March 2026

- **Play Integrity Integration**: Integrated Google Play Integrity API and Automatic Integrity Protection to safeguard against APK tampering and repackaging.
- **Security Health Attestation**: Added an interactive Play Integrity attestation tester in the Security Audit screen for real-time device health verification.
- **Stability & Performance**: General performance optimizations and underlying dependency updates.

---

## v1.1.0 (Hardware Cryptographic Hardening & Air-Gapped Sync) - March 2026

### 🛡️ Core Security & Cryptographic Hardening
- **Hardware-Bound Biometric Unwrapping**: Biometric unlock now requires cryptographic unwrapping of the Master PIN via Android TEE/StrongBox `BiometricPrompt.CryptoObject` ciphers.
- **PIN-Bound Database Protection**: SQLCipher database passphrase is encrypted with a PBKDF2-derived key (100,000 iterations) directly bound to the Master PIN.
- **Hardened QR Payload Sync**: QR code synchronization now generates a dynamic 16-byte random salt per transaction and utilizes 100,000 PBKDF2 iterations to eliminate offline dictionary brute-force attacks.
- **Flexible Master PIN Length**: Removed arbitrary length limits during setup and PIN changes. Users can now create high-entropy Master PINs or numeric passcodes of any length (min 4 digits).
- **Environment & Root Security Health**: Added real-time root access detection, runtime sandbox checks, and KeyStore hardware TEE/StrongBox verification to the Security Health screen and Auth Screen.
- **Ciphertext Leak Prevention**: Cryptographic fallbacks in repository layers now return `null` on decryption errors, preventing raw ciphertext from leaking into the UI.

### 🚀 Air-Gapped Web Drop & Cross-Device Sync
- **Zero-Network Desktop Transfer**: Send 32+ character high-entropy credentials directly from your phone to any modern PC/Mac desktop browser without internet access, accounts, or cloud relays.
- **Web Bluetooth (BLE GATT)**: Phone operates as a BLE GATT peripheral server (`e9a30001-c852-4e08-9bfa-87bb0f592658`), streaming chunked AES-256-GCM ciphertext directly to the browser.
- **Client-Side Proof of Work (PoW)**: Dynamic QR session challenge generated in browser RAM using SHA-256 (`< 0x0800...`), auto-refreshing every 60 seconds to eliminate replay attacks.
- **Ephemeral ECDH P-256 Key Agreement**: In-memory key generation and Diffie-Hellman scalar multiplication with HKDF-SHA256 (`"kunjika-web-drop-v1"`).
- **Visual Short Authentication String (SAS)**: 6-digit TOTP verification code calculated independently on both devices; user visually verifies parity to prevent Man-in-the-Middle (MitM) attacks.
- **Tamper-Evident Audit Logging**: Automatically appends an `EXPORT_BLE` block to the local hardware-signed blockchain ledger for complete traceability.
- **Zero-Install Web Companion**: Available at [https://dwinsi.github.io/kunjika/web-companion/](https://dwinsi.github.io/kunjika/web-companion/) with RAM-only decryption and automatic 30-second clipboard wipe.

---

## v1.0.1 (UI Refinement & Stability) - August 28, 2026

### 🛠️ Improvements
- **Manual Password Generation**: The password generator no longer auto-generates strings on every setting change. This prevents history pollution and ensures only user-requested passwords are saved.
- **Enhanced UI Logic**: 
    - Added "Generate" and "Regenerate" actions for clearer interaction.
    - Updated copy/save actions to remain disabled until a string is generated.
    - Added placeholder guidance in the generator output box.
- **Internal Optimization**: Refined viewmodel logic to reduce unnecessary background operations.

---

## v1.0.0 (First Official Release) - August 26, 2026

We are proud to introduce **Kunjika**, a zero-network password manager that prioritizes user sovereignty and hardware-backed security above all else.

### 🛡️ Security First
- **100% Offline**: No internet permission requested. Your data never leaves your device.
- **Double Encryption**: Every entry is encrypted twice using SQLCipher and hardware-backed AES-256-GCM (Android KeyStore).
- **Blockchain Audit Log**: A cryptographically signed ledger tracks all changes to your vault, preventing unauthorized database tampering.
- **PBKDF2 PIN Hashing**: Your Master PIN is protected by 100,000 iterations of PBKDF2 with a unique per-device salt.
- **Biometric Binding**: Authentication is hardware-verified and cryptographically bound to your encryption keys.

---
*Kunjika is developed by Ashwin Singh.*
