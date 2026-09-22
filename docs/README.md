# 📚 Kunjika Documentation Index

Welcome to the technical documentation for **Kunjika** (v1.1.2). This documentation provides a comprehensive guide to Kunjika's security architecture, cryptographic protocols, user interface systems, and feature sets.

---

## 📖 Table of Contents

1. **[🏗️ Architecture & Data Flow](architecture.md)**
   - System design, MVVM architecture, Security Core layer, Biometric PIN unwrap pipeline, Web Bluetooth GATT peripheral, and modern Android 15+ dependency stack.
2. **[🛡️ Security & Hardening Deep-Dive](security.md)**
   - Double-Lock encryption (Android KeyStore TEE + SQLCipher), Biometric-backed PIN storage via `BiometricPrompt.CryptoObject`, Local Blockchain Audit Log, Environmental Tamper Defenses (Root/Emulator detection, Play Integrity API), and the Air-Gapped Web Drop Cryptographic Protocol.
3. **[✨ Feature Guide](features.md)**
   - Complete breakdown of the 4-in-1 Smart Generator (Passwords, Diceware, PINs, TOTP Secrets) with history, Encrypted Sovereign Vault, Phone-to-Phone QR Sync, Air-Gapped Web Drop, and Obsidian Gold & Platinum visual theming.
4. **[📋 Release Notes](../RELEASE_NOTES.md)**
   - Detailed version-by-version change log from v1.0.0 up to the current v1.1.2 release.
5. **[🌐 Web Companion](https://dwinsi.github.io/kunjika/web-companion/)**
   - Zero-install, client-side web companion for receiving credentials over Web Bluetooth with automatic memory and clipboard wipe.
6. **[🔒 Privacy Policy](../PRIVACY_POLICY.md)**
   - Sovereign zero-network privacy policy and offline data rights.
7. **[📊 Play Store Data Safety](../PLAY_STORE_DATA_SAFETY.md)**
   - Official Google Play data safety disclosures and permission specifications.

---

## 📊 Visualizing the Logic
All architecture and protocol diagrams throughout this documentation are rendered in standard **Mermaid.js** format for clarity and version-controlled review.

> [!TIP]
> When viewing documentation in Android Studio or GitHub, interactive Mermaid diagram previews are supported natively.

---

## 🔒 The Sovereign Security Promise
Kunjika is designed to be **100% offline**. We mathematically guarantee that no passwords, master keys, or personal metadata ever touch the network or leave your device's secure hardware.
