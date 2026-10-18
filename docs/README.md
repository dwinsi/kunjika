# 📚 Kunjika Documentation Index

[![Version](https://img.shields.io/badge/version-1.1.2-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B)](../RELEASE_NOTES.md)
[![Theme](https://img.shields.io/badge/theme-Obsidian%20Gold%20%26%20Midnight%20Indigo-F59E0B?style=flat-square&labelColor=1E1B4B&color=F59E0B)](features.md)
[![Architecture](https://img.shields.io/badge/architecture-Security--First%20Clean-818CF8?style=flat-square&labelColor=1E1B4B&color=818CF8)](architecture.md)
[![Security](https://img.shields.io/badge/security-local--first-34D399?style=flat-square&labelColor=064E3B&color=34D399)](security.md)

Welcome to the technical documentation for **Kunjika** (v1.1.2). This documentation provides a comprehensive guide to Kunjika's security architecture, cryptographic protocols, user interface systems, and feature sets styled in the signature **Obsidian Gold & Midnight Indigo** aesthetic.

---

## 📖 Table of Contents

1. **[📖 User Guide & Quickstart](user-guide.md)**
   - Simple, step-by-step beginner guide with screenshots, common use cases (generating passwords, vault search, 2FA codes, Web Drop to PC, phone-to-phone transfer), and FAQs.
2. **[🇮🇳 हिंदी यूजर गाइड (Hindi User Guide)](user-guide-hindi.md)** / **[🌐 हिंदी वेब पेज (Web Page)](https://dwinsi.github.io/kunjika/hindi.html)**
   - सरल और आसान हिंदी गाइड: यह ऐप क्यों इस्तेमाल करें, कैसे इस्तेमाल करें, स्क्रीनशॉट्स और जरूरी सवाल-जवाब।
3. **[🏗️ Architecture & Data Flow](architecture.md)**
   - System design, MVVM architecture, Security Core layer, Biometric PIN unwrap pipeline, Web Bluetooth GATT peripheral, and modern Android 15+ dependency stack.
3. **[🛡️ Security & Hardening Deep-Dive](security.md)**
   - Double-Lock encryption (Android KeyStore TEE + SQLCipher), Biometric-backed PIN storage via `BiometricPrompt.CryptoObject`, Local Blockchain Audit Log, Environmental Tamper Defenses (Root/Emulator detection, Play Integrity API), and the Direct Web Drop Cryptographic Protocol.
4. **[✨ Feature Guide](features.md)**
   - Complete breakdown of the 4-in-1 Smart Generator (Passwords, Diceware, PINs, TOTP Secrets) with history, Encrypted Sovereign Vault, Phone-to-Phone QR Sync, Direct Web Drop, and Obsidian Gold & Platinum visual theming.
5. **[📢 Promotional & Educational Guide](promotional-guide.md)**
   - Complete marketing copy, user education on cloud vulnerabilities vs offline sovereignty, what users can achieve, real-world personas, and launch materials.
6. **[📋 Release Notes](../RELEASE_NOTES.md)**
   - Detailed version-by-version change log from v1.0.0 up to the current v1.1.2 release.
7. **[🌐 Web Companion](https://dwinsi.github.io/kunjika/web-companion/)**
   - Browser companion for direct Bluetooth transfer. It does not auto-copy passwords; cleanup is best-effort and the receiving computer must be trusted.
8. **[🔒 Privacy Policy](../PRIVACY_POLICY.md)**
   - Local data handling and permissions disclosure.
9. **[📊 Play Store Data Safety](../PLAY_STORE_DATA_SAFETY.md)**
   - Official Google Play data safety disclosures and permission specifications.

---

## 📊 Visualizing the Logic
All architecture and protocol diagrams throughout this documentation are rendered in standard **Mermaid.js** format for clarity and version-controlled review.

> [!TIP]
> When viewing documentation in Android Studio or GitHub, interactive Mermaid diagram previews are supported natively.

---

## 🔒 Security Boundaries
The Android app does not declare `android.permission.INTERNET`, so it cannot make ordinary internet connections. It can still share selected data through Bluetooth, QR, clipboard, Autofill, and exports. Web Drop's hosted companion is mutable and the receiving computer must be trusted.
