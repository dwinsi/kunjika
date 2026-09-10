# Executive Security Evaluation: Kunjika Password Manager

**App Name:** Kunjika
**Package:** `com.kunjika.app`
**Architecture:** 100% Offline / Localized Zero-Knowledge Password Vault
**Evaluator:** Senior Android Security Expert

---

## Overall Security posture: 🟢 STRONG (8.5 / 10)

Kunjika demonstrates exceptional security architecture for an Android application, adhering strictly to zero-network principles, defense-in-depth cryptographic storage, hardware-backed Android KeyStore integration, and tamper-evident audit logging.

---

## 1. Architectural Highlights & Strengths

### 🛡️ Air-Gapped & Zero-Network Design
- **Transitive Permission Stripping:** `AndroidManifest.xml` explicitly strips `INTERNET` and `ACCESS_NETWORK_STATE` permissions using `tools:node="remove"`. Even if third-party libraries (e.g., ML Kit) request internet access, the Android OS blocks all network sockets.
- **Offline BLE WebDrop Sync:** WebDrop uses BLE advertising with ephemeral ECDH (P-256) key exchange and AES-256-GCM transport encryption for completely offline phone-to-browser transfers.

### 🔐 Screen Security & Leak Prevention
- **FLAG_SECURE Enforcement:** `MainActivity.kt` enforces `WindowManager.LayoutParams.FLAG_SECURE`, blocking screenshots, screen recordings, system display mirroring, and hiding vault previews in the Android Recent Apps switcher.

### 🔑 Cryptographic Storage & Database Security
- **SQLCipher At-Rest Encryption:** Room Database is encrypted using SQLCipher (`net.zetetic:android-database-sqlcipher`).
- **Application-Layer Field Encryption:** `PasswordEntity` fields are double-encrypted with `AES-256-GCM` via `KeystoreManager` backed by `AndroidKeyStore`.
- **Master Key Hardware Isolation:** Keys generated in `AndroidKeyStore` (`KunjikaMasterSecretKey`, `KunjikaBiometricKey`) utilize Android hardware-backed security modules (TEE / StrongBox).

### ⛓️ Tamper-Evident Blockchain Audit Ledger
- **Append-Only Signed Ledger:** Every vault operation (`CREATE`, `UPDATE`, `DELETE`) is recorded in `blockchain_ledger` and cryptographically signed using an EC key pair (`secp256r1`) via `SHA256withECDSA`.
- **Merkle Linkage:** Blocks store `previousHash` references, allowing immediate detection of database tampering or unauthorized row modification.

### 📋 Clipboard Security
- **Sensitive Clip Identification:** Sets `ClipDescription.EXTRA_IS_SENSITIVE = true` on API 33+ (Android 13+), preventing soft keyboards and clipboard managers from caching copied passwords.
- **Auto-Clearing Timer:** Automatically clears clipboard content after 30 seconds.

---

## 2. Security Vulnerabilities & Findings

| ID | Title | Severity | Location | Description |
|---|---|---|---|---|
| **FIND-01** | Database Key Derivation Bypasses Master PIN | 🔴 **High** | `UserPreferences.kt` | SQLCipher key is generated randomly and stored encrypted via `KeystoreManager`. Because `KeystoreManager` does not require PIN authentication, anyone with physical/root access can decrypt the database without entering the Master PIN. |
| **FIND-02** | Unbound Biometric Authentication Flow | 🟠 **Medium** | `AuthViewModel.kt` | Biometric authentication verifies identity via `BiometricPrompt`, but the resulting `CryptoObject` cipher is not used to unwrap or decrypt the vault master key; auth state is held solely as a `Boolean` in memory. |
| **FIND-03** | In-Memory Sensitive Data Leakage (`String` vs `CharArray`) | 🟠 **Medium** | `PasswordRepository.kt` | Passwords and TOTP secrets are stored and passed around as immutable `java.lang.String` objects, making them impossible to zeroize and leaving them in JVM Heap memory until GC collects them. |
| **FIND-04** | QR Transfer Code Vulnerable to Fast Brute-Force | 🟠 **Medium** | `QrEncryptionManager.kt` | QR transfer codes use a 6-digit numeric PIN with a static salt and 10,000 PBKDF2 iterations. 1,000,000 combinations can be brute-forced offline in < 1 second. |
| **FIND-05** | Cryptographic Fallback Leaks Encrypted Ciphertext | 🟡 **Low** | `PasswordRepository.kt` | In `toDecrypted()`, if TOTP secret decryption fails, the raw ciphertext is returned as the TOTP secret fallback instead of returning `null` or throwing an exception. |
| **FIND-06** | Root & Emulator Detection Unenforced | 🟡 **Low** | `SecurityManager.kt` | Root and emulator detection methods exist in `SecurityManager`, but they are purely informational and not hooked into vault lock/warning logic. |

---

## 3. Recommended Remediation & Action Plan

### 1. Bind SQLCipher Key to Master PIN / Key Derivation
- Derive the primary database key directly from `PBKDF2(Master PIN, Salt)` or store the database passphrase encrypted inside an `AndroidKeyStore` key with `setUserAuthenticationRequired(true)`.

### 2. Utilize CryptoObject in Biometric Unlock
- In `AuthViewModel.unlockWithBiometrics()`, use `result.cryptoObject.cipher` to decrypt an encrypted master token or seed value, ensuring hardware-verified decryption.

### 3. Upgrade QR Sync Entropy
- In `QrEncryptionManager`, include a dynamic 16-byte random salt in the encrypted payload and increase PBKDF2 iterations to at least 100,000 (or use Argon2id).

### 4. Zeroize Sensitive Strings
- Replace immutable `String` instances with `CharArray` or `ByteArray` for raw password handling during generation, encryption, and clipboard operations. Clean them with `Arrays.fill(arr, '\0')` immediately after use.

---

## 4. Summary Matrix

```
[System Security]      ██████████ 100% (FLAG_SECURE, No Internet)
[Data Storage]          ████████░░  80% (SQLCipher + AES-GCM)
[Key Management]        ███████░░░  70% (Keystore used, needs PIN binding)
[Authentication]        ████████░░  80% (PBKDF2 100k, Needs CryptoObject bind)
[Audit Integrity]       ██████████ 100% (ECDSA Blockchain Ledger)
```
