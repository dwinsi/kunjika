---
layout: default
---

# 🏰 Kunjika

### *Military-Grade, Offline-First Password Vault & Generator for Android*

Kunjika is a zero-network, ultra-secure sovereign credential manager built for **Android 15+ (API 35)**. It provides a mathematical "Sovereign" security model where you retain 100% ownership of your data with cryptographic proofs of integrity.

<p align="center" style="margin: 24px 0;">
  <a href="./web-companion/" style="background: #00e676; color: #05100a; font-weight: 700; padding: 10px 20px; border-radius: 8px; text-decoration: none; margin-right: 10px;">⚡ Launch Web Drop</a>
  <a href="./docs/" style="background: rgba(255,255,255,0.1); color: #ffffff; padding: 10px 18px; border-radius: 8px; text-decoration: none; margin-right: 10px;">📖 Documentation</a>
  <a href="./PRIVACY_POLICY.html" style="background: rgba(255,255,255,0.1); color: #ffffff; padding: 10px 18px; border-radius: 8px; text-decoration: none;">🔒 Privacy Policy</a>
</p>

---

## ⚡ Introducing Kunjika Web Drop (Air-Gapped PC Sync)

The biggest challenge with offline mobile password managers is typing 24-character high-entropy passwords (`k8#Q!v9$L...`) from your phone screen onto your laptop keyboard.

**Kunjika Web Drop** solves cross-device typing friction **without cloud servers, without user accounts, and with ZERO internet permission**:

* **📡 100% Air-Gapped via Web Bluetooth (BLE)**: Your phone communicates directly with your laptop browser over direct local radio. Data never touches Wi-Fi, routers, or the internet.
* **⚡ Client-Side Proof of Work (PoW)**: Protects against stale or replayed QR codes by requiring a local cryptographic puzzle solved in milliseconds on the laptop CPU.
* **🔢 TOTP 6-Digit Visual Verification (SAS)**: Both screens calculate and display the identical 6-digit confirmation code (e.g. `544 655`) so you visually verify the connection before authorizing.
* **🔐 Hardware-Signed Blockchain Audit**: Every Web Drop transfer is authenticated with your fingerprint and permanently logged into the phone's hardware-backed blockchain ledger.
* **⏳ RAM-Only with 30s Auto-Wipe**: Credentials are decrypted strictly in laptop browser RAM, copied to your clipboard, and automatically wiped after 30 seconds.

👉 **[Open Kunjika Web Drop Companion](./web-companion/)** *(Works in Chrome, Edge, Brave, and Opera)*

---

## 🛡️ Security Highlights

- **🚫 Zero Network Guarantee**: No `android.permission.INTERNET` declared in `AndroidManifest.xml`. Data is physically incapable of leaving your device over the network.
- **🔐 Double Encryption**: Every credential is encrypted with **AES-256-GCM** backed by Android KeyStore hardware (TEE/StrongBox) + **SQLCipher** database encryption.
- **🧱 Local Blockchain Ledger**: Hardware-signed (`secp256r1` ECDSA) audit chain guaranteeing tamper-evidence for all vault create, update, delete, and export actions.
- **📡 Air-Gapped QR & BLE Sync**: Wirelessly export and import credentials without any cloud intermediary.
- **🛡️ PIN Hardening**: Salted PBKDF2 hashing for Master PIN (100,000 iterations).
- **🛡️ Screen Protection**: App-wide `FLAG_SECURE` prevents screenshots, screen recording, and task-switcher previews.

---

## ✨ Features

- **🎯 Intelligent Generator**: Generate cryptographically strong random passwords, memorable Diceware passphrases (10,000-word offline dictionary), and PINs with real-time bit entropy analysis and crack-time estimation.
- **🗳️ Encrypted Sovereign Vault**: Organize credentials with custom categories (Personal, Work, Finance, Social), favorite bookmarks, and encrypted secure notes.
- **⏰ Built-In TOTP Authenticator**: Replace Google Authenticator with hardware-protected 2FA code generation (RFC 6238) and circular countdown timers.
- **🔍 Security Health Audit**: Continuous audit of your vault highlighting weak passwords, reused credentials, old passwords (>90 days), and expiring accounts.
- **🪄 Native Android Autofill**: Seamless one-tap autofill in Android apps and mobile Chrome browsers.
- **📑 Emergency Recovery Kit**: Generate a secure, printable PDF Emergency Kit with automated cache purging.

---

## 🏗️ How Web Drop Works

```
[ Laptop Browser (Web Drop) ]                         [ Kunjika Phone App ]
             │                                                 │
 1. Generates Ephemeral ECDH Key (P-256)                       │
 2. Solves Local Proof of Work                                 │
 3. Displays Pairing QR Code                                   │
             │                                                 │
             │ <────────── 4. Phone Scans Laptop QR ───────────┤
             │                                                 │
             │                           5. Verifies PoW & Time Window
             │                           6. Derives Shared Secret (ECDH)
             │                           7. Computes 6-Digit TOTP Code
             │                           8. Starts BLE Advertising
             │
 9. Both screens show identical code:
    ┌──────────────────────────────────┐
    │  PAIRING CODE: 544 655           │  <── User visually confirms!
    └──────────────────────────────────┘
             │                                                 │
             │                           10. Touch Fingerprint Sensor
             │                           11. Encrypts payload (AES-GCM)
             │                           12. Signs block with Hardware Key
             │
             │ <─── 13. Transmits Ciphertext via Bluetooth ────┤
             │
14. Decrypts payload into RAM
15. Copies password to clipboard
16. Auto-wipes clipboard in 30s
17. Destroys all keys and memory
```

---

## 📚 Resources & Links

- **[Web Drop Companion](./web-companion/)** - Live air-gapped web companion
- **[Architecture Deep-Dive](./docs/architecture.md)** - Technical and cryptographic architecture
- **[Security Overview](./docs/security.md)** - Threat model and defense layers
- **[Feature Guide](./docs/features.md)** - Complete feature documentation
- **[Privacy Policy](./PRIVACY_POLICY.html)** - Official zero-data collection policy

---

## 👨‍💻 Author & License

Developed with ❤️ by **Ashwin Singh**.  
Licensed under the **MIT License**.
