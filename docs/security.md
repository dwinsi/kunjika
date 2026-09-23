# 🛡️ Security Deep-Dive

[![Version](https://img.shields.io/badge/version-1.1.2-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B)](../RELEASE_NOTES.md)
[![Theme](https://img.shields.io/badge/theme-Obsidian%20Gold%20%26%20Midnight%20Indigo-F59E0B?style=flat-square&labelColor=1E1B4B&color=F59E0B)](features.md)
[![Encryption](https://img.shields.io/badge/encryption-Double--Lock%20AES--256--GCM-F59E0B?style=flat-square&labelColor=1E1B4B&color=F59E0B)](security.md)
[![Network](https://img.shields.io/badge/network-100%25%20Air--Gapped-34D399?style=flat-square&labelColor=064E3B&color=34D399)](security.md)
[![Audit](https://img.shields.io/badge/tamper%20defense-Blockchain%20Ledger-818CF8?style=flat-square&labelColor=1E1B4B&color=818CF8)](security.md)

Kunjika is engineered to exceed military-grade standards for local data protection. Operating on a **Zero-Network, Zero-Trust** model, it guarantees that credentials never touch the internet and are protected by hardware-backed cryptographic primitives.

---

## 🗝️ Encryption Strategy: "Double-Lock" Architecture

Kunjika does not simply encrypt the database file; it encrypts every sensitive field *before* it is written to an encrypted database.

```mermaid
%%{init: {
  'theme': 'base',
  'themeVariables': {
    'darkMode': true,
    'background': '#07090E',
    'mainBkg': '#0F141C',
    'primaryColor': '#131A26',
    'primaryTextColor': '#F8FAFC',
    'primaryBorderColor': '#F59E0B',
    'lineColor': '#818CF8',
    'clusterBkg': '#0F141C',
    'clusterBorder': '#2B374A',
    'edgeLabelBackground': '#18202E'
  }
}}%%
graph LR
    P[Plaintext Data] --> E1[AES-256-GCM]
    subgraph KS ["Android KeyStore (Hardware TEE/StrongBox)"]
        K1[Hardware Master Key]
    end
    K1 --> E1
    E1 --> C[Field Ciphertext]
    C --> E2[SQLCipher DB]
    subgraph RP ["Passphrase Derivation"]
        PIN[Master PIN] --> PBKDF2[PBKDF2 100,000 iter]
        PBKDF2 --> K2[Database Key]
    end
    K2 --> E2
    E2 --> D[(Encrypted SQLite File on Disk)]

    classDef gold fill:#131A26,stroke:#F59E0B,stroke-width:1.5px,color:#FDE68A;
    classDef indigo fill:#1E1B4B,stroke:#818CF8,stroke-width:1.5px,color:#E0E7FF;
    classDef emerald fill:#064E3B,stroke:#34D399,stroke-width:1.5px,color:#A7F3D0;
    classDef storage fill:#18202E,stroke:#FBBF24,stroke-width:2px,color:#FDE68A;

    class P,C,E1,E2 gold;
    class K1 emerald;
    class PIN,PBKDF2,K2 indigo;
    class D storage;

    style KS fill:#052e25,stroke:#34D399,stroke-width:2px,color:#A7F3D0
    style RP fill:#151238,stroke:#818CF8,stroke-width:2px,color:#C7D2FE
```

> [!NOTE]
> **Double-Lock Protection**: Even if an attacker gains raw root access to the device and dumps the SQLite database file, the contents remain encrypted at the field level with a hardware-backed key that physically cannot be exported from the device's TEE or StrongBox.

### 1. Hardware Security Module (Android KeyStore)
All cryptographic keys are generated inside the device's **Trusted Execution Environment (TEE)** or dedicated **StrongBox Keymaster**:
- **Master Encryption Key**: AES-256-GCM key used for field-level credential encryption.
- **Identity Key**: Elliptic curve (`secp256r1`) key used for signing blockchain ledger blocks.
- **Biometric Key**: AES-256 key initialized with `setUserAuthenticationRequired(true)`.

### 2. Database Protection (SQLCipher)
- **PIN-Derived Database Passphrase**: The database encryption key is derived from the user's Master PIN combined with a per-device salt using **PBKDF2WithHmacSHA256** (100,000 iterations).
- **No Static Keys**: There are no hardcoded or static decryption keys anywhere in the application code.

---

## 🔐 Hardware-Bound Biometric PIN Storage

To provide frictionless biometric unlock without weakening the Master PIN or persisting plaintext credentials in storage or RAM:

1. **Hardware-Guarded Cipher**: When biometrics are enabled, an AES-256-GCM key is generated in the Android KeyStore requiring biometric authentication for each use (`setUserAuthenticationRequired(true)`).
2. **Cryptographic Wrapping**: Upon successful initial PIN entry and biometric verification, the Master PIN is encrypted using the authenticated `BiometricPrompt.CryptoObject` cipher.
3. **Persisting Encrypted Payload**: The resulting ciphertext and initialization vector (IV) are persisted in `UserPreferences`. Plaintext is cleared from memory.
4. **Biometric Unwrapping**: On subsequent app launches, a biometric prompt authorizes the KeyStore cipher to decrypt the stored PIN directly into volatile memory for database unlocking. If biometrics are modified or removed at the OS level, the KeyStore key is automatically invalidated by Android.

---

## 🧱 Local Blockchain Audit Log

To prevent offline modification attacks—where an attacker with physical or root access alters entries directly in the SQLite database file—Kunjika maintains a cryptographically signed blockchain ledger.

```mermaid
%%{init: {
  'theme': 'base',
  'themeVariables': {
    'darkMode': true,
    'background': '#07090E',
    'mainBkg': '#0F141C',
    'primaryColor': '#131A26',
    'primaryTextColor': '#F8FAFC',
    'primaryBorderColor': '#F59E0B',
    'lineColor': '#818CF8',
    'clusterBkg': '#0F141C',
    'clusterBorder': '#2B374A',
    'edgeLabelBackground': '#18202E'
  }
}}%%
graph RL
    B1[Block 0: Genesis]
    B2[Block 1: Created Entry]
    B3[Block 2: Updated Entry]
    B4[Block 3: Web Drop Export]
    
    B2 -- "prevHash" --> B1
    B3 -- "prevHash" --> B2
    B4 -- "prevHash" --> B3

    subgraph Block ["Block Structure"]
        H[Content Hash SHA-256]
        S[ECDSA Hardware Signature]
        T[Timestamp]
        P[Previous Block Hash]
    end
    
    classDef goldBlock fill:#131A26,stroke:#F59E0B,stroke-width:1.5px,color:#FDE68A;
    classDef blockDetail fill:#1E1B4B,stroke:#818CF8,stroke-width:1.5px,color:#E0E7FF;
    
    class B1,B2,B3,B4 goldBlock;
    class H,S,T,P blockDetail;
    
    style Block fill:#151238,stroke:#818CF8,stroke-width:2px,color:#C7D2FE
```

- **Content Hash**: `SHA-256` of the serialized transaction data.
- **Hardware Signature**: Signed with the device's private EC key (`secp256r1`) residing in the hardware TEE.
- **Cryptographic Chain**: Each block points to the previous block's SHA-256 hash. Any manual alteration of past database records breaks the cryptographic chain and triggers an immediate audit alert.

---

## 🛡️ Environmental Integrity & Tamper Protection

Kunjika incorporates multi-layered runtime environmental checks to detect compromised execution environments:

### 1. Real-Time Root Detection
- **Build Tags Analysis**: Scans `Build.TAGS` for `test-keys` indicative of custom, rooted ROMs.
- **System Binary Detection**: Probes filesystem paths for presence of the `su` binary across `/sbin/su`, `/system/bin/su`, `/system/xbin/su`, `/data/local/xbin/su`, `/system/sd/xbin/su`, and `/system/app/Superuser.apk`.
- **Runtime Execution Probe**: Spawns an isolated sub-process executing `which su` to catch stealth root managers that hide binaries from standard directory listings.

### 2. Emulator Environment Detection
Heuristic scanning flags emulated environments to prevent dynamic analysis in automated sandboxes:
- Checks build attributes for `generic`, `unknown`, `google_sdk`, `Android SDK built for x86`, and `vbox86p`.
- Inspects hardware configuration strings for `goldfish` and `ranchu` emulation architectures.
- Identifies virtualized manufacturers such as `Genymotion`.

### 3. KeyStore Hardware Attestation
- Uses `KeyInfo.isInsideSecureHardware` via `SecretKeyFactory` to verify whether the active encryption keys are physically backed by hardware (TEE/StrongBox) rather than a software fallback.

### 4. Google Play Integrity API
- Verifies app binary integrity and authentic signing certificates to prevent distribution of repackaged or trojanized APKs.

---

## 📂 Air-Gapped Web Drop Cryptographic Protocol

Web Drop enables wireless credential transfer to desktop/laptop browsers without internet access, third-party relays, or accounts.

```mermaid
%%{init: {
  'theme': 'base',
  'themeVariables': {
    'darkMode': true,
    'background': '#07090E',
    'mainBkg': '#0F141C',
    'primaryColor': '#131A26',
    'primaryTextColor': '#F8FAFC',
    'primaryBorderColor': '#F59E0B',
    'lineColor': '#818CF8',
    'clusterBkg': '#0F141C',
    'clusterBorder': '#2B374A',
    'edgeLabelBackground': '#18202E'
  }
}}%%
graph TD
    subgraph Browser ["💻 Web Companion (RAM Only)"]
        POW[Compute SHA-256 PoW] --> B_KP[Generate Ephemeral P-256 Keypair]
        B_KP --> QR[Display Dynamic QR Code]
        B_SAS[Compute 6-Digit Visual SAS]
        B_DEC[AES-256-GCM Decrypt in RAM]
        WIPE[30s Clipboard & Memory Auto-Wipe]
    end

    subgraph Phone ["📱 Kunjika Android App"]
        CAM[Scan QR Code via CameraX] --> P_POW[Verify PoW & Extract Public Key]
        P_POW --> P_KP[Generate Ephemeral P-256 Keypair]
        P_KP --> ECDH[Derive Shared Secret via HKDF]
        ECDH --> P_SAS[Compute 6-Digit Visual SAS]
        P_SAS --> BIO{Biometric Authorization}
        BIO -->|Success| P_ENC[AES-256-GCM Encrypt Payload]
        P_ENC --> BLE_TX[BLE GATT Server Stream with Retries]
        P_ENC --> LEDGER[Record EXPORT_BLE in Blockchain]
    end

    QR -.->|Visual Scan| CAM
    P_SAS <===>|User Confirms Visual Parity| B_SAS
    BLE_TX ==>|Web Bluetooth GATT Chunks| B_DEC
    B_DEC --> WIPE

    classDef browserNode fill:#1E1B4B,stroke:#818CF8,stroke-width:1.5px,color:#E0E7FF;
    classDef phoneNode fill:#131A26,stroke:#F59E0B,stroke-width:1.5px,color:#FDE68A;
    classDef greenNode fill:#064E3B,stroke:#34D399,stroke-width:1.5px,color:#A7F3D0;

    class POW,B_KP,QR,B_SAS,B_DEC,WIPE browserNode;
    class CAM,P_POW,P_KP,ECDH,P_SAS,BIO,P_ENC,BLE_TX,LEDGER phoneNode;
    class BIO greenNode;

    style Browser fill:#151238,stroke:#818CF8,stroke-width:2px,color:#C7D2FE
    style Phone fill:#0F141C,stroke:#F59E0B,stroke-width:2px,color:#FDE68A
```

### Cryptographic Guarantees:
1. **Client-Side Proof of Work (PoW)**:
   - Browser computes `SHA-256(SessionID + Nonce) < 0x0800...` before displaying the session QR code.
   - Sessions expire after 60 seconds, eliminating replay attacks.
2. **Ephemeral ECDH P-256 Key Agreement**:
   - Both devices generate single-use EC P-256 keypairs.
   - Android parses the browser's 65-byte uncompressed public key (`0x04 || X || Y`) and computes the Diffie-Hellman shared secret.
   - Keys are derived via **HKDF-SHA256** with domain separation context `"kunjika-web-drop-v1"`.
3. **Visual Short Authentication String (SAS)**:
   - To defeat Man-in-the-Middle (MitM) attacks over Bluetooth, both devices derive a 6-digit TOTP code from the shared secret.
   - User visually verifies code parity across both screens before touching the biometric sensor.
4. **Hardware Biometric Authorization**:
   - Android `BiometricPrompt` (`BIOMETRIC_STRONG`) must succeed before BLE advertising begins.
5. **BLE GATT Transfer with Reliable Chunking**:
   - Phone acts as a GATT peripheral server advertising Service `e9a30001-c852-4e08-9bfa-87bb0f592658`.
   - Transmits AES-256-GCM ciphertext in sequence-numbered chunks with notification retries for error-free delivery.
6. **RAM-Only Auto-Wipe**:
   - The Web Companion operates strictly in browser memory (no `localStorage`, cookies, or `IndexedDB`).
   - A 30-second countdown wipes decrypted credentials from RAM and shreds the OS clipboard.
7. **Blockchain Audit Trail**:
   - Every Web Drop transaction commits an `EXPORT_BLE` block into the local ledger.

---

## 📑 Backup Security & Cache Purging
- **Encrypted Vault Backups**: Password-protected vault exports encrypted with AES-256-GCM using PBKDF2 key derivation.
- **Cache Shredding**: Generated PDF Recovery Kits are automatically overwritten and purged from device cache immediately after sharing to leave zero filesystem footprint.
