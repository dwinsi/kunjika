#  Kunjika Documentation Index

[![Google Play](https://img.shields.io/badge/Google_Play-Install%20App-34D399?style=flat-square&labelColor=064E3B&color=34D399&logo=googleplay&logoColor=white)](https://play.google.com/store/apps/details?id=com.kunjika.app)
[![Project Website](https://img.shields.io/badge/Project%20Website-GitHub%20Pages-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B&logo=githubpages&logoColor=white)](https://dwinsi.github.io/kunjika/)
[![Version](https://img.shields.io/badge/version-1.9.0-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B)](../RELEASE_NOTES.md)
[![Theme](https://img.shields.io/badge/theme-Obsidian%20Gold%20%26%20Midnight%20Indigo-F59E0B?style=flat-square&labelColor=1E1B4B&color=F59E0B)](features.md)
[![Architecture](https://img.shields.io/badge/architecture-Security--First%20Clean-818CF8?style=flat-square&labelColor=1E1B4B&color=818CF8)](architecture.md)
[![Security](https://img.shields.io/badge/security-local--first-34D399?style=flat-square&labelColor=064E3B&color=34D399)](security.md)

Welcome to the documentation for **Kunjika source version 1.9.0** (version code 19). The Play Store rollout can differ from the current source version; check the release track for the installed version. These guides cover the app's features, limitations, privacy, and implementation.

---

##  Table of Contents

0. **[ Interactive Project Website & Online Guide](https://dwinsi.github.io/kunjika/)**
   - Detailed feature overview, real-time in-browser password generator, screenshots tour, plain-language privacy explanation, setup instructions, FAQs, and support links.
1. **[ User Guide & Quickstart](user-guide.md)**
   - Simple, step-by-step beginner guide with screenshots, common use cases (generating passwords, vault search, 2FA codes, Web Drop to PC, phone-to-phone transfer), and FAQs.
2. **[ हिंदी यूजर गाइड (Hindi User Guide)](user-guide-hindi.md)** / **[ हिंदी वेब पेज (Web Page)](https://dwinsi.github.io/kunjika/hindi.html)**
   - सरल और आसान हिंदी गाइड: यह ऐप क्यों इस्तेमाल करें, कैसे इस्तेमाल करें, स्क्रीनशॉट्स और जरूरी सवाल-जवाब।
3. **[ Architecture & Data Flow](architecture.md)**
   - System design, MVVM architecture, Android Keystore, Web Bluetooth GATT peripheral, and current Android dependency stack.
4. **[ Security & Hardening Deep-Dive](security.md)**
   - SQLCipher and Android Keystore data protection, biometric app access and its current limitation, local signed audit chain, advisory root/emulator checks, and the Direct Web Drop protocol.
5. **[ Feature Guide](features.md)**
   - Password and passphrase generation, numeric PINs, TOTP, vault organization, backups, encrypted QR transfer, Web Drop, and security-check limitations.
6. **[ Product Positioning & Promotion Guide](promotional-guide.md)**
   - Current product positioning, accurate feature descriptions, organic promotion ideas, and copy that avoids unsupported security comparisons.
7. **[ Release Notes](../RELEASE_NOTES.md)**
   - Version history. The current source version is 1.9.0 (version code 19); verify the Play Store track for live rollout details.
8. **[ Web Companion](https://dwinsi.github.io/kunjika/web-companion/)**
   - Browser companion for direct Bluetooth transfer. It does not auto-copy passwords; cleanup is best-effort and the receiving computer must be trusted.
9. **[ Privacy Policy](../PRIVACY_POLICY.md)**
   - Local data handling and permissions disclosure.
10. **[ Play Store Data Safety Checklist](../PLAY_STORE_DATA_SAFETY.md)**
   - Code-based checklist; verify the exact release and current Play Console questionnaire before submitting.

---

##  Visualizing the Logic
All architecture and protocol diagrams throughout this documentation are rendered in standard **Mermaid.js** format for clarity and version-controlled review.

> [!TIP]
> Mermaid diagrams render on GitHub. Preview support varies in other Markdown viewers.

---

##  Security Boundaries
The Android app manifest removes `android.permission.INTERNET`; Kunjika provides no account or cloud-sync service. The app can request a Play Integrity token through Google Play services and can share selected data through Bluetooth, QR, clipboard, Autofill, and exports. Web Drop's hosted companion is mutable, and the receiving computer must be trusted.
