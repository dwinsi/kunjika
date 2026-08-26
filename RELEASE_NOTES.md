# KeyFortress Release Notes

## v1.0.0 (First Official Release) - August 26, 2026

We are proud to introduce **KeyFortress**, a zero-network password manager that prioritizes user sovereignty and hardware-backed security above all else.

### 🛡️ Security First
- **100% Offline**: No internet permission requested. Your data never leaves your device.
- **Double Encryption**: Every entry is encrypted twice using SQLCipher and hardware-backed AES-256-GCM (Android KeyStore).
- **Blockchain Audit Log**: A cryptographically signed ledger tracks all changes to your vault, preventing unauthorized database tampering.
- **PBKDF2 P[_config.yml](_config.yml)IN Hashing**: Your Master PIN is protected by 100,000 iterations of PBKDF2 with a unique per-device salt.
- **Biometric Binding**: Authentication is hardware-verified and cryptographically bound to your encryption keys.

### ✨ Key Features
- **Smart Generator**: Highly customizable passwords, passphrases, and numeric PINs with real-time entropy analysis.
- **Secure Vault**: Categorized storage for credentials, URLs, and notes.
- **Air-Gapped Sync**: Securely transfer entries between devices via encrypted QR codes (AES-GCM).
- **Native Autofill**: Seamlessly fill credentials in other apps and websites.
- **Built-in TOTP**: Integrated 2FA support with encrypted secrets.
- **Recovery Kit**: Generate a secure PDF emergency kit for peace of mind.

### 🎨 User Experience
- **Modern UI**: Built with Jetpack Compose and Material 3 (Material You support).
- **Accessibility**: Full TalkBack support and semantic labeling.
- **Privacy Protection**: Screenshot and screen recording prevention active throughout the app.

---
*KeyFortress is developed by Ashwin Singh.*
