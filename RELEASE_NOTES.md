# Kunjika Release Notes

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
- **Web Bluetooth (BLE GATT)**: Phone operates as a BLE GATT peripheral server (`e9a30001-...`), streaming chunked AES-256-GCM ciphertext directly to the browser.
- **Client-Side Proof of Work (PoW)**: Dynamic QR session challenge generated in browser RAM using SHA-256 (`< 0x0800...`), auto-refreshing every 60 seconds to eliminate replay attacks.
- **Ephemeral ECDH P-256 Key Agreement**: In-memory key generation and Diffie-Hellman scalar multiplication with HKDF-SHA256 (`"kunjika-web-drop-v1"`).
- **Visual Short Authentication String (SAS)**: 6-digit TOTP verification code calculated independently on both devices; user visually verifies parity to prevent Man-in-the-Middle (MitM) attacks.
- **Tamper-Evident Audit Logging**: Automatically appends an `EXPORT_BLE` block to the local hardware-signed blockchain ledger for complete traceability.
- **Zero-Install Web Companion**: Available at [https://dwinsi.github.io/kunjika/web-companion/](https://dwinsi.github.io/kunjika/web-companion/) with RAM-only decryption and automatic 30-second clipboard wipe.

## v1.0.1 (UI Refinement & Stability) - August 28, 2026

### 🛠️ Improvements
- **Manual Password Generation**: The password generator no longer auto-generates strings on every setting change. This prevents history pollution and ensures only user-requested passwords are saved.
- **Enhanced UI Logic**: 
    - Added "Generate" and "Regenerate" actions for clearer interaction.
    - Updated copy/save actions to remain disabled until a string is generated.
    - Added placeholder guidance in the generator output box.
- **Internal Optimization**: Refined viewmodel logic to reduce unnecessary background operations.

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
