# 🛡️ Security Deep-Dive

Kunjika is engineered to exceed military-grade standards for local data protection. It operates on a **Zero-Network, Zero-Trust** model.

## 🗝️ Encryption Strategy: "Double-Lock" Architecture

Kunjika doesn't just encrypt the database; it encrypts the data *inside* the encrypted database.

```mermaid
graph LR
    P[Plaintext Data] --> E1[AES-256-GCM]
    subgraph KS ["Android KeyStore (Hardware TEE/HSM)"]
        K1[Master Key]
    end
    K1 --> E1
    E1 --> C[Ciphertext]
    C --> E2[SQLCipher DB]
    subgraph RP ["Random Passphrase Storage"]
        K2[Encrypted Random Key]
    end
    K2 --> E2
    E2 --> D[(Encrypted Disk)]

    style KS fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px
    style RP fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    style D fill:#fff3e0,stroke:#e65100,stroke-width:2px
```

> [!NOTE]
> **Double-Lock Architecture Explanation**: This diagram visualizes our "Double-Lock" security model. Plaintext data is first encrypted using **AES-256-GCM** with a master key from the **Android KeyStore** (TEE/HSM). The resulting ciphertext is then stored in a **SQLCipher Database**, which is itself locked with a unique random passphrase, providing two independent layers of hardware-backed protection.


### 1. The Hardware Layer (KeyStore)
Sensitive keys are generated inside the device's **Trusted Execution Environment (TEE)** or **Secure Element (SE)**. The private/secret keys never leave this hardware.
- **Master Key**: AES-256 for vault data.
- **Identity Key**: EC (secp256r1) for Blockchain signatures.
- **Biometric Key**: Requires `setUserAuthenticationRequired(true)`.

### 2. The Database Layer (SQLCipher)
- **Random Passphrase**: On first run, a 256-bit random key is generated.
- **Key Storage**: This key is encrypted by the Master Key and stored in DataStore.
- **SQLCipher**: The database is unlocked using this unique, non-deterministic key.

## 🧱 Local Blockchain Audit Log

To prevent "Offline Modification Attacks" (where an attacker with root access modifies the SQLite file directly), Kunjika maintains a cryptographically signed ledger.

```mermaid
graph RL
    B1[Block 0: Genesis]
    B2[Block 1: Created Password]
    B3[Block 2: Updated Password]
    
    B2 -- "prevHash" --> B1
    B3 -- "prevHash" --> B2

    subgraph Block ["Block Structure"]
        H[Content Hash]
        S[ECDSA Signature]
        T[Timestamp]
        P[Previous Block Hash]
    end
    
    style Block fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
```

> [!NOTE]
> **Blockchain Ledger Explanation**: The blockchain audit log creates a cryptographically linked chain of vault actions. Each block contains a **Content Hash** of the action, an **ECDSA Signature** from the hardware-backed Identity Key, and a **Merkle Link** (hash) to the previous block. This ensures that any manual tampering with the database file can be immediately detected.


## 🔐 Authentication & Access
- **Master PIN**: Protected via **PBKDF2WithHmacSHA256** with **100,000 iterations** and a unique 16-byte salt.
- **Biometric Binding**: Authentication is cryptographically bound to a KeyStore key. A UI-bypass (like Frida) cannot unlock the encryption without a hardware-verified biometric success.
- **Auto-Lock**: Foreground/Background lifecycle observers trigger an immediate vault lock.

## 📂 Secure Export & Sync
- **QR Sync**: AES-GCM encryption with keys derived via PBKDF2 from a 6-digit Transfer Code.
- **Backup**: AES-256-GCM backups with user-provided passphrases.
- **Cache Purge**: Recovery Kits (PDFs) are automatically deleted from the cache after use to prevent data residue.
