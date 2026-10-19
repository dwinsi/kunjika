#  Security Deep-Dive

[![Version](https://img.shields.io/badge/version-1.8.0-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B)](../RELEASE_NOTES.md)
[![Theme](https://img.shields.io/badge/theme-Obsidian%20Gold%20%26%20Midnight%20Indigo-F59E0B?style=flat-square&labelColor=1E1B4B&color=F59E0B)](features.md)
[![Encryption](https://img.shields.io/badge/encryption-SQLCipher%20%2B%20AES--GCM-F59E0B?style=flat-square&labelColor=1E1B4B&color=F59E0B)](security.md)
[![Network](https://img.shields.io/badge/design-local--first-34D399?style=flat-square&labelColor=064E3B&color=34D399)](security.md)
[![Audit](https://img.shields.io/badge/audit-local%20signed%20chain-818CF8?style=flat-square&labelColor=1E1B4B&color=818CF8)](security.md)

Kunjika is designed for local-first use. The Android manifest removes the app's `INTERNET` permission, but Bluetooth, operating-system services, clipboard integrations, backups, and a receiving computer remain part of the security boundary. Keystore hardware backing depends on the device and is not guaranteed everywhere.

---

##  Encryption Strategy: "Double-Lock" Architecture

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
    subgraph RP ["Database Key Protection"]
        RAND[Random 256-bit passphrase] --> WRAP[Keystore AES-GCM wrapping]
        K1 --> WRAP
        WRAP --> K2[SQLCipher passphrase]
    end
    K2 --> E2
    E2 --> D[(Encrypted SQLite File on Disk)]

    classDef gold fill:#131A26,stroke:#F59E0B,stroke-width:1.5px,color:#FDE68A;
    classDef indigo fill:#1E1B4B,stroke:#818CF8,stroke-width:1.5px,color:#E0E7FF;
    classDef emerald fill:#064E3B,stroke:#34D399,stroke-width:1.5px,color:#A7F3D0;
    classDef storage fill:#18202E,stroke:#FBBF24,stroke-width:2px,color:#FDE68A;

    class P,C,E1,E2 gold;
    class K1 emerald;
    class RAND,K2,WRAP indigo;
    class D storage;

    style KS fill:#052e25,stroke:#34D399,stroke-width:2px,color:#A7F3D0
    style RP fill:#151238,stroke:#818CF8,stroke-width:2px,color:#C7D2FE
```

> [!NOTE]
> **At-rest protection**: SQLCipher uses a random passphrase wrapped by Android Keystore, while selected fields use a Keystore key. Root access or code execution as the app can invoke these keys and bypass the app PIN; hardware backing varies by device.

### 1. Hardware Security Module (Android KeyStore)
Cryptographic keys are generated in Android Keystore. Hardware backing is device-dependent and is reported only when the platform confirms TEE or StrongBox:
- **Master Encryption Key**: AES-256-GCM key used for field-level credential encryption.
- **Identity Key**: Elliptic curve (`secp256r1`) key used to sign local audit-chain blocks.
- **Biometric Key**: AES-256 key initialized with `setUserAuthenticationRequired(true)` when biometric unlock is configured.

### 2. Database Protection (SQLCipher)
- **Random Database Passphrase**: SQLCipher uses a random 256-bit passphrase stored wrapped by Android Keystore. The Master PIN is an app access gate, not the database key.
- **PIN Verification**: Newly set PINs use PBKDF2-HMAC-SHA-256 at 600,000 iterations with exponential app-level delays after failures. Root access can bypass the app and reset app data; existing PINs shorter than six digits should be changed.
- **No Static Keys**: There are no hardcoded or static decryption keys anywhere in the application code.

---

##  Biometric PIN Unlock

Biometric unlock uses Android Keystore to protect a stored PIN for convenience. The PIN is not persisted as plaintext, but it is used in app memory during authentication; app code cannot guarantee secure erasure from memory.

1. **Authentication-Gated Cipher**: When biometrics are enabled, an AES-256-GCM key is generated in Android Keystore requiring biometric authorization for its use (`setUserAuthenticationRequired(true)`). Hardware backing depends on the device.
2. **Cryptographic Wrapping**: Upon successful initial PIN entry and biometric verification, the Master PIN is encrypted using the authenticated `BiometricPrompt.CryptoObject` cipher.
3. **Persisting Encrypted Payload**: The resulting ciphertext and initialization vector (IV) are persisted in `UserPreferences`. The PIN may exist temporarily in app memory during unlock; secure erasure cannot be guaranteed for Kotlin/Java strings.
4. **Biometric Unwrapping**: On subsequent app launches, a biometric prompt authorizes the KeyStore cipher to decrypt the stored PIN directly into volatile memory for database unlocking. If biometrics are modified or removed at the OS level, the KeyStore key is automatically invalidated by Android.

---

##  Local Signed Audit Chain

Kunjika maintains a locally signed hash chain intended to make some edits detectable. It is not an immutable blockchain and cannot reliably detect full ledger deletion, tail truncation, or an attacker controlling the app and its signing environment.

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
        S[ECDSA Android Keystore Signature]
        T[Timestamp]
        P[Previous Block Hash]
    end
    
    classDef goldBlock fill:#131A26,stroke:#F59E0B,stroke-width:1.5px,color:#FDE68A;
    classDef blockDetail fill:#1E1B4B,stroke:#818CF8,stroke-width:1.5px,color:#E0E7FF;
    
    class B1,B2,B3,B4 goldBlock;
    class H,S,T,P blockDetail;
    
    style Block fill:#151238,stroke:#818CF8,stroke-width:2px,color:#C7D2FE
```

- **Vault Record Digest**: HMAC-SHA-256 of serialized vault row data with a Keystore-held key to resist offline guessing from ledger-only copies. Other audit event digests are not password hashes.
- **Device Keystore Signature**: Signed with a private EC key (`secp256r1`) in Android Keystore; hardware backing varies by device.
- **Cryptographic Chain**: Each block points to the previous block's SHA-256 hash. This can detect some edits, but cannot reliably detect full ledger deletion, tail truncation, or rollback.

---

##  Environmental Integrity & Tamper Protection

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

### 3. KeyStore Security Level
- Reports Android's key security level (software, TEE, or StrongBox where available). This is device-reported information, not independent key attestation.

### 4. Google Play Integrity API
- The app can request an integrity token. Without backend verification, it does not provide the app with a trusted integrity verdict.

---

##  Direct Web Drop Cryptographic Protocol

Web Drop transfers a selected credential directly over Bluetooth without a cloud relay or account. The hosted companion page needs to be loaded in the browser first, and the receiving computer must be trusted.

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
    subgraph Browser [" Web Companion (best-effort memory cleanup)"]
        B_KP[Generate Ephemeral P-256 Keypair] --> QR[Display One-minute QR]
        B_SAS[Derive transcript comparison code]
        B_DEC[AES-256-GCM Decrypt]
        WIPE[Clear visible fields on close]
    end

    subgraph Phone [" Kunjika Android App"]
        CAM[Scan QR Code via CameraX] --> P_KP[Validate session & Generate P-256 Key]
        P_KP --> ECDH[Derive Shared Secret via HKDF]
        ECDH --> P_SAS[Compute 6-Digit Visual SAS]
        P_SAS --> BIO{Biometric Authorization}
        BIO -->|Success| P_ADV[Advertise session BLE service]
        P_ADV --> P_ENC[AES-256-GCM Encrypt after code confirmation]
        P_ENC --> BLE_TX[BLE GATT Server Stream with Retries]
        P_ENC --> LEDGER[Record EXPORT_BLE in local signed audit chain]
    end

    QR -.->|Visual Scan| CAM
    P_SAS <===>|Compare on both screens before send| B_SAS
    BLE_TX ==>|Web Bluetooth GATT Chunks| B_DEC
    B_DEC --> WIPE

    classDef browserNode fill:#1E1B4B,stroke:#818CF8,stroke-width:1.5px,color:#E0E7FF;
    classDef phoneNode fill:#131A26,stroke:#F59E0B,stroke-width:1.5px,color:#FDE68A;
    classDef greenNode fill:#064E3B,stroke:#34D399,stroke-width:1.5px,color:#A7F3D0;

    class B_KP,QR,B_SAS,B_DEC,WIPE browserNode;
    class CAM,P_KP,ECDH,P_SAS,BIO,P_ADV,P_ENC,BLE_TX,LEDGER phoneNode;
    class BIO greenNode;

    style Browser fill:#151238,stroke:#818CF8,stroke-width:2px,color:#C7D2FE
    style Phone fill:#0F141C,stroke:#F59E0B,stroke-width:2px,color:#FDE68A
```

### Cryptographic Guarantees:
1. **Short-lived QR sessions**:
   - The phone enforces a 60-second time window and remembers recently consumed session IDs while its process remains alive. This does not authenticate the companion page.
2. **Ephemeral ECDH P-256 Key Agreement**:
   - Both devices generate single-use EC P-256 keypairs.
   - Android parses the browser's 65-byte uncompressed public key (`0x04 || X || Y`) and computes the Diffie-Hellman shared secret.
   - HKDF-SHA-256 derives separate encryption and pairing-code keys, bound to the session ID and both public keys.
3. **Visual Short Authentication String (SAS)**:
   - Both devices derive a six-digit comparison code from the session transcript. After biometric authorization, the user compares both screens and confirms on the browser before approving the send on the phone.
4. **Hardware Biometric Authorization**:
   - Android `BiometricPrompt` (`BIOMETRIC_STRONG`) must succeed before BLE advertising begins.
5. **BLE GATT Transfer with Reliable Chunking**:
   - Phone advertises a per-session random GATT service UUID only after biometric approval.
   - Transmits AES-256-GCM ciphertext in sequence-numbered chunks with notification retries for error-free delivery.
6. **Best-effort cleanup**:
   - The browser does not automatically copy passwords. Closing the transfer view clears visible credential fields and drops references, but JavaScript cannot guarantee memory erasure or delete operating-system clipboard history.
7. **Local Audit Trail**:
   - Web Drop records an `EXPORT_BLE` event in the local signed audit chain. The chain can help detect some edits but is not immutable and cannot guarantee detection of deletion or rollback.

---

##  Backup Security & Cache Purging
- **Encrypted Vault Backups**: Password-protected vault exports encrypted with AES-256-GCM using PBKDF2 key derivation.
- **Recovery kit cleanup**: The generated PDF is deleted from app cache after a delay. Deletion does not guarantee physical erasure on flash storage; protect the PDF and delete exported copies yourself.
