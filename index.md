---
layout: default
---

# 🏰 Kunjika

### *Military-Grade, Offline-First Sovereign Password Vault & Generator for Android*

[![Version](https://img.shields.io/badge/version-1.1.2-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B)](./RELEASE_NOTES.html)
[![Platform](https://img.shields.io/badge/platform-Android%2015%2B%20(API%2035)-818CF8?style=flat-square&labelColor=1E1B4B&color=818CF8)](https://developer.android.com)
[![Security](https://img.shields.io/badge/network-100%25%20Air--Gapped-34D399?style=flat-square&labelColor=064E3B&color=34D399)](./docs/security.html)
[![License](https://img.shields.io/badge/license-MIT-FBBF24?style=flat-square&labelColor=07090E&color=FBBF24)](./LICENSE)

Kunjika is a zero-network, ultra-secure sovereign credential manager built for **Android 15+ (API 35)**. Engineered with a mathematical "Sovereign" security model, Kunjika ensures you retain 100% ownership of your data with cryptographic proofs of integrity and zero cloud dependencies.

<p align="center" style="margin: 28px 0;">
  <a href="./web-companion/" style="background: linear-gradient(180deg, #FDE68A 0%, #F59E0B 52%, #B45309 100%); color: #1a0f00; font-weight: 700; padding: 12px 22px; border-radius: 10px; text-decoration: none; margin-right: 12px; box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.85), 0 6px 20px rgba(245, 158, 11, 0.4); border: 1.5px solid transparent; display: inline-block;">⚡ Launch Web Drop</a>
  <a href="./docs/" style="background: linear-gradient(180deg, #1E1B4B 0%, #131138 100%); color: #E0E7FF; font-weight: 600; padding: 12px 20px; border-radius: 10px; text-decoration: none; margin-right: 12px; box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.3), 0 4px 14px rgba(0, 0, 0, 0.4); border: 1.5px solid #818CF8; display: inline-block;">📖 Documentation</a>
  <a href="./PRIVACY_POLICY.html" style="background: linear-gradient(180deg, #18202E 0%, #0F141C 100%); color: #F8FAFC; font-weight: 600; padding: 12px 20px; border-radius: 10px; text-decoration: none; box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.2), 0 4px 14px rgba(0, 0, 0, 0.4); border: 1.5px solid #2B374A; display: inline-block;">🔒 Privacy Policy</a>
</p>

---

## ⚡ Introducing Kunjika Web Drop (Air-Gapped PC Sync)

The greatest pain point with offline mobile password managers is manually typing 24+ character high-entropy passwords (`k8#Q!v9$L...`) from your phone screen onto your laptop keyboard.

**Kunjika Web Drop** eliminates cross-device typing friction **without cloud servers, without user accounts, and with ZERO internet permission**:

* **📡 100% Air-Gapped via Web Bluetooth (BLE)**: Direct radio communication between your phone and desktop browser. Data never touches Wi-Fi, routers, or the internet.
* **⚡ Client-Side Proof of Work (PoW)**: Browser calculates a dynamic SHA-256 PoW challenge (`< 0x0800...`) that auto-refreshes every 60 seconds to eliminate replay attacks.
* **🔑 Ephemeral ECDH P-256 Key Agreement**: In-memory key exchange deriving a 256-bit shared secret via HKDF-SHA256 (`"kunjika-web-drop-v1"`).
* **🔢 Visual SAS Confirmation (TOTP)**: Both screens independently calculate and display the identical 6-digit verification code (e.g. `544 655`) for visual confirmation before transmission.
* **🔐 Hardware-Signed Blockchain Audit**: Every Web Drop transfer is authorized via fingerprint biometrics and committed to the phone's hardware-signed blockchain ledger.
* **⏳ RAM-Only with 30s Auto-Wipe**: Credentials exist solely in desktop browser RAM, copyable to clipboard with an automatic 30-second memory and clipboard shredder.

👉 **[Open Kunjika Web Drop Companion](./web-companion/)** *(Compatible with Chrome, Edge, Brave, and Opera)*

---

## 🛡️ Sovereign Security Highlights

- **🚫 Zero Network Guarantee**: No `android.permission.INTERNET` declared in `AndroidManifest.xml`. Data is physically incapable of transmitting over any network.
- **🔐 Double-Lock Encryption**: Every credential is encrypted with **AES-256-GCM** inside Android KeyStore hardware (TEE/StrongBox), stored inside a **SQLCipher** database encrypted with a PBKDF2-derived key (100,000 iterations).
- **💎 Biometric-Backed PIN Storage**: Master PIN is encrypted using KeyStore TEE ciphers via `BiometricPrompt.CryptoObject`, enabling seamless biometric unwrap without exposing plaintext in storage or memory.
- **🧱 Local Blockchain Ledger**: Hardware-signed (`secp256r1` ECDSA) audit chain guaranteeing tamper-evidence for all create, update, delete, and export operations.
- **🛡️ Environmental Integrity Defenses**: Real-time Root detection (`su` binaries, test-keys builds, execution probes), Emulator detection (QEMU/goldfish hardware signatures), KeyStore hardware attestation, and Google Play Integrity API verification.
- **🛡️ Screen Protection**: App-wide `FLAG_SECURE` prevents screenshots, screen recordings, and OS task-switcher leaks.

---

## ✨ Features

- **🎨 Obsidian Gold & Platinum Dual Themes**: Designed with a luxury **Obsidian Gold & Midnight Indigo** dark palette and an ultra-clean **Platinum Silver** light palette, paired with custom **Glossy UI Components** (`GlossyCard`, `GlossyButton`, `GlossyBadge`).
- **🎯 4-in-1 Generator with History**: Generate cryptographically strong random passwords, memorable Diceware passphrases (10,000-word offline dictionary), Numeric PINs, and RFC 6238-compliant **TOTP Secrets** with persistent history logging.
- **🗳️ Encrypted Sovereign Vault**: Organize credentials with custom categories (Personal, Work, Finance, Social), favorite bookmarks, and encrypted secure notes.
- **⏰ Built-In TOTP Authenticator**: Hardware-protected 2FA code generation with animated circular countdown timers.
- **🔍 Security Health Audit**: Continuous audit of your vault highlighting weak, reused, or old credentials alongside live Root, Emulator, and Play Integrity checks.
- **🪄 Native Android Autofill**: Effortless one-tap autofill across apps and websites.
- **📑 Emergency Recovery Kit**: Generate an encrypted, printable PDF Emergency Kit with automated cache purging.

---

## 🏗️ How Web Drop Works

```
[ Laptop Browser (Web Drop) ]                         [ Kunjika Phone App ]
             │                                                 │
 1. Generates Ephemeral ECDH Key (P-256)                       │
 2. Solves Local Proof of Work                                 │
 3. Displays Dynamic Pairing QR Code                           │
             │                                                 │
             │ <────────── 4. Phone Scans Laptop QR ───────────┤
             │                                                 │
             │                           5. Verifies PoW & Time Window
             │                           6. Derives Shared Secret (HKDF)
             │                           7. Computes 6-Digit TOTP Code
             │                           8. Starts BLE GATT Advertising
             │
 9. Both screens show identical code:
    ┌──────────────────────────────────┐
    │  PAIRING CODE: 544 655           │  <── User visually confirms!
    └──────────────────────────────────┘
             │                                                 │
             │                           10. Touch Biometric Sensor
             │                           11. Encrypts payload (AES-256-GCM)
             │                           12. Signs block with Hardware Key
             │
             │ <─── 13. Transmits Chunks via Bluetooth (GATT) ──┤
             │
14. Decrypts payload into RAM
15. Copies password to clipboard
16. Auto-wipes memory & clipboard in 30s
17. Destroys ephemeral keys
```

---

## 📚 Resources & Links

- **[Web Drop Companion](./web-companion/)** - Live air-gapped web companion
- **[Architecture Deep-Dive](./docs/architecture.html)** - Technical and cryptographic architecture
- **[Security Overview](./docs/security.html)** - Threat model and defense layers
- **[Feature Guide](./docs/features.html)** - Complete feature documentation
- **[Release Notes](./RELEASE_NOTES.html)** - Complete version change log
- **[Privacy Policy](./PRIVACY_POLICY.html)** - Official zero-data collection policy
- **[Play Store Data Safety](./PLAY_STORE_DATA_SAFETY.html)** - Data safety declarations

---

## 👨‍💻 Author & License

Developed with ❤️ by **Ashwin Singh**. 
Reviewer - **Sachin Kumar**; **Vikash Kumar Pandey**
Licensed under the **MIT License**. See [LICENSE](./LICENSE) for details.
