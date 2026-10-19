#  Kunjika

### *A Local-First Encrypted Password Vault & Generator for Android*

[![Google Play](https://img.shields.io/badge/Google_Play-Install%20App-34D399?style=flat-square&labelColor=064E3B&color=34D399&logo=googleplay&logoColor=white)](https://play.google.com/store/apps/details?id=com.kunjika.app)
[![Project Website](https://img.shields.io/badge/Project%20Website-GitHub%20Pages-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B&logo=githubpages&logoColor=white)](https://dwinsi.github.io/kunjika/)
[![Version](https://img.shields.io/badge/version-1.9.0-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B)](RELEASE_NOTES.md)
[![Platform](https://img.shields.io/badge/platform-Android%208%2B%20(API%2026)-818CF8?style=flat-square&labelColor=1E1B4B&color=818CF8)](https://developer.android.com)
[![Theme](https://img.shields.io/badge/theme-Obsidian%20Gold%20%26%20Midnight%20Indigo-F59E0B?style=flat-square&labelColor=1E1B4B&color=F59E0B)](docs/features.md)
[![Security](https://img.shields.io/badge/design-local--first-34D399?style=flat-square&labelColor=064E3B&color=34D399)](docs/security.md)
[![Web Companion](https://img.shields.io/badge/Web%20Drop-Bluetooth%20PC%20Transfer-818CF8?style=flat-square&labelColor=07090E&color=818CF8)](https://dwinsi.github.io/kunjika/web-companion/)
[![License](https://img.shields.io/badge/license-MIT-FBBF24?style=flat-square&labelColor=07090E&color=FBBF24)](LICENSE)

---

##  What is Kunjika?

Kunjika is a local-first password manager and authenticator for Android. Its encrypted vault is stored on your device, with no Kunjika account or cloud sync required.
- **No Kunjika account or cloud sync.** The Android app manifest removes the `INTERNET` permission. It can still share selected data through Bluetooth, QR, clipboard, Autofill, and exports; the optional Play Integrity request is made through Google Play services.
- **Local-first storage**: Vault data is encrypted on-device. Sharing or copying a credential moves it into the security boundary of the receiving device or app.

>  **Explore the interactive [Kunjika Project Website](https://dwinsi.github.io/kunjika/)** for a feature tour, screenshots, privacy information, setup guide, and community support.
>
>  **Install on Android:** [Get Kunjika on Google Play Store](https://play.google.com/store/apps/details?id=com.kunjika.app) (Requires Android 8.0 or later).
>
>  **Visual Walkthrough:** Check out our [User Guide & Step-by-Step Walkthrough](docs/user-guide.md) with screenshots and everyday use cases!

---

##  Key Features (In Plain English)

| Feature | What It Does | Why It Matters to You |
| :--- | :--- | :--- |
| ** No Android Internet Permission** | The app has no `INTERNET` permission. | Kunjika has no direct app internet access; Google Play services may handle the optional Play Integrity request. Local transfer and clipboard features still share data with other devices or apps. |
| ** Encrypted Local Vault** | SQLCipher uses a random passphrase wrapped by Android Keystore; hardware backing varies by device. | Protects copied database files. The Master PIN is an app access gate, not the database encryption key; root or app-process compromise can bypass it. |
| ** Password & PIN Generator** | Generates passwords, multi-word passphrases, and numeric PINs, plus TOTP setup secrets. | Choose the format for each account and revisit generated items in local history. |
| ** Built-In TOTP Authenticator** | Generates rotating six-digit codes for entries with a saved TOTP secret. | Keep a login and its authenticator code together, or use a separate authenticator if you prefer more separation. |
| **Web Drop (Send to PC)** | Transfer a selected credential from your phone to a compatible browser using QR pairing and Bluetooth. | No cable or cloud relay is used for the transfer; the browser and receiving computer must be trusted. |
| ** Phone-to-Phone QR Transfer** | Move credentials between two phones by scanning a secure, encrypted QR code. | Transfer passwords to a new phone without using cloud backups, cables, or Wi-Fi. |
| **Biometric App Access** | Use Android biometric authentication as an optional app access path. | The current UI does not verify the Master PIN on the no-saved-PIN biometric path; see the security guide. |
| ** Local Audit Chain** | A local HMAC-protected chain records vault changes. | Can help detect some edits; it is not immutable and cannot reliably detect full deletion or rollback. |
| ** Vault & Device Checks** | Reviews password strength/reuse and reports selected device environment signals. | Helps you spot issues; checks are advisory and cannot guarantee device security. |
| ** Obsidian Gold & Platinum Themes** | Choose between dark and light themes with custom Compose components. | Personalize the app's appearance. |

---

##  Handled Use Cases & Step-by-Step Flow Diagrams

Below are the primary use cases handled by Kunjika, illustrated with numbered step-by-step flows.

---

### Use Case 1: Generating & Saving a New Password or 2FA Code
*How Kunjika generates cryptographically secure passwords and stores them under double-lock encryption.*

```mermaid
%%{init: {
  'theme': 'base',
  'themeVariables': {
    'darkMode': true,
    'background': '#07090E',
    'actorBkg': '#0F141C',
    'actorBorder': '#F59E0B',
    'actorTextColor': '#FDE68A',
    'actorLineColor': '#818CF8',
    'signalColor': '#F59E0B',
    'signalTextColor': '#F8FAFC',
    'labelBoxBkgColor': '#0F141C',
    'labelBoxBorderColor': '#818CF8',
    'labelTextColor': '#F8FAFC',
    'loopTextColor': '#FDE68A',
    'noteBkgColor': '#1E1B4B',
    'noteBorderColor': '#818CF8',
    'noteTextColor': '#E0E7FF',
    'activationBorderColor': '#F59E0B',
    'activationBkgColor': '#131A26',
    'sequenceNumberColor': '#07090E'
  }
}}%%
sequenceDiagram
    autonumber
    actor User as  User
    participant UI as  Generator Screen
    participant Engine as  Crypto Engine
    participant Vault as  Security Vault Core
    participant DB as  SQLCipher DB (Encrypted)

    User->>UI: Select Type (Password, Passphrase, PIN, or TOTP)
    User->>UI: Tap "Generate"
    UI->>Engine: Request high-entropy random generation
    Engine-->>UI: Display generated credential + crack time
    UI->>UI: Save to Generator History (for quick recall)
    User->>UI: Tap "Save to Vault" (add title, username, category)
    UI->>Vault: Pass credential details
    Vault->>Vault: Encrypt sensitive fields with Hardware Key (AES-256-GCM)
    Vault->>Vault: Record selected event in local signed audit chain
    Vault->>DB: Store encrypted record in 256-bit database
    DB-->>UI: Confirmation success
    UI-->>User: Show item saved with strength rating
```

1. **Choose Type**: Pick between standard passwords, readable multi-word passphrases, numeric PINs, or 2FA authenticator keys.
2. **Generate**: The app uses Android's `SecureRandom` and shows an estimated strength indicator. Estimates are approximate and depend on assumptions about attacker capabilities.
3. **History Logging**: The item is logged in your local generation history so you don't lose it if you accidentally navigate away.
4. **Keystore Encryption**: When saved, sensitive fields are encrypted with a key protected by Android Keystore; hardware backing varies by device.
5. **Database Storage**: The encrypted record is committed to the encrypted database, and a digital signature is recorded in your audit log.

---

### Use Case 2: Sending a Password to Your Computer (Web Drop)
*How to log in to websites on your desktop or laptop without typing passwords or using the cloud.*

```mermaid
%%{init: {
  'theme': 'base',
  'themeVariables': {
    'darkMode': true,
    'background': '#07090E',
    'actorBkg': '#0F141C',
    'actorBorder': '#F59E0B',
    'actorTextColor': '#FDE68A',
    'actorLineColor': '#818CF8',
    'signalColor': '#F59E0B',
    'signalTextColor': '#F8FAFC',
    'labelBoxBkgColor': '#0F141C',
    'labelBoxBorderColor': '#818CF8',
    'labelTextColor': '#F8FAFC',
    'loopTextColor': '#FDE68A',
    'noteBkgColor': '#1E1B4B',
    'noteBorderColor': '#818CF8',
    'noteTextColor': '#E0E7FF',
    'activationBorderColor': '#F59E0B',
    'activationBkgColor': '#131A26',
    'sequenceNumberColor': '#07090E'
  }
}}%%
sequenceDiagram
    autonumber
    actor User as  User
    participant PC as  Web Companion (Browser)
    participant Phone as  Kunjika App
    participant BLE as  Web Bluetooth (Direct Link)

    User->>PC: Open Web Companion (Offline HTML)
    PC->>PC: Generate an ephemeral ECDH key and session ID
    PC->>PC: Display dynamic QR Code on computer screen
    User->>Phone: Open Credential -> Tap "Send to PC"
    Phone->>PC: Scan QR Code using phone camera (Reads PC's Public Key)
    User->>Phone: Approve Bluetooth advertising with biometrics
    Phone->>BLE: Register per-session GATT service, then advertise
    User->>PC: Click "Pair Bluetooth" and select the phone
    PC->>User: Display transcript-derived 6-digit comparison code
    User->>PC: Confirm matching code
    User->>Phone: Approve sending
    Phone->>BLE: Connect directly to PC over Bluetooth Low Energy
    Phone->>BLE: Stream encrypted password (AES-256-GCM)
    BLE->>PC: Deliver encrypted payload
    PC->>PC: Decrypt in browser runtime
    PC->>User: Display password for explicit reveal or copy
    Note over PC: Closing clears visible fields best-effort; JS memory erasure is not guaranteed
```

1. **Open Web Companion**: On your computer browser, open the [Web Companion](https://dwinsi.github.io/kunjika/web-companion/). It runs entirely in your browser's local memory.
2. **Scan the Screen**: On your phone, tap "Send to PC" and scan the QR code displayed on your monitor.
3. **Authorize on the phone**: Tap **Authorize Pairing** and approve with biometrics. The phone starts advertising only after approval and GATT service setup.
4. **Connect from the computer**: Click **Pair Bluetooth** in the companion page and choose the phone in the browser's device picker. The browser requires this user gesture.
5. **Visual Code Check**: Compare the six-digit codes on both screens, confirm in the browser, then approve sending on the phone. This can help detect session substitution but does not authenticate the hosted page itself.
6. **Direct Bluetooth Stream**: The selected credential is encrypted and sent directly over Bluetooth; no cloud relay is used during transfer.
7. **Copy only when needed**: The companion does not auto-copy. Clipboard history and host software can retain anything copied; closing the view only clears visible fields on a best-effort basis.

---

### Use Case 3: Transferring Passwords Between Two Phones (Encrypted QR Sync)
*How to migrate passwords to a new phone with complete privacy.*

```mermaid
%%{init: {
  'theme': 'base',
  'themeVariables': {
    'darkMode': true,
    'background': '#07090E',
    'actorBkg': '#0F141C',
    'actorBorder': '#F59E0B',
    'actorTextColor': '#FDE68A',
    'actorLineColor': '#818CF8',
    'signalColor': '#F59E0B',
    'signalTextColor': '#F8FAFC',
    'labelBoxBkgColor': '#0F141C',
    'labelBoxBorderColor': '#818CF8',
    'labelTextColor': '#F8FAFC',
    'loopTextColor': '#FDE68A',
    'noteBkgColor': '#1E1B4B',
    'noteBorderColor': '#818CF8',
    'noteTextColor': '#E0E7FF',
    'activationBorderColor': '#F59E0B',
    'activationBkgColor': '#131A26',
    'sequenceNumberColor': '#07090E'
  }
}}%%
sequenceDiagram
    autonumber
    actor User as  User
    participant OldPhone as  Sending Phone
    participant NewPhone as  Receiving Phone

    User->>OldPhone: Select Item -> Tap "Export / Sync via QR"
    OldPhone->>OldPhone: Generate random 16-digit transfer code
    OldPhone->>OldPhone: Encrypt item with PBKDF2 (600,000 passes) + AES-GCM
    OldPhone->>OldPhone: Display encrypted QR and code in separate views
    User->>NewPhone: Open Kunjika -> Tap "Scan QR Sync"
    NewPhone->>OldPhone: Point camera and scan QR Code
    NewPhone->>User: Prompt to enter the 16-digit transfer code
    User->>NewPhone: Enters matching 16-digit code
    NewPhone->>NewPhone: Decrypts payload, verifies data integrity
    NewPhone->>NewPhone: Saves item into new encrypted vault
    NewPhone-->>User: "Credential Successfully Imported!"
```

1. **Select Item**: Choose any credential to transfer on your current phone.
2. **Dynamic Encryption**: The phone encrypts the credential using a random 16-digit code and creates an encrypted QR pattern. The QR and code appear separately; do not capture both together.
3. **Scan with New Phone**: Open Kunjika on your new phone and point the camera at the first screen.
4. **Enter Code**: Type the 16-digit transfer code.
5. **Instant Import**: The new phone validates the integrity of the data and securely adds it to your new vault.

---

### Use Case 4: Vault Health & Phone Security Audit
*How Kunjika continuously protects you from compromised devices and weak passwords.*

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
    'secondaryColor': '#1E1B4B',
    'tertiaryColor': '#0F141C',
    'edgeLabelBackground': '#18202E'
  }
}}%%
graph TD
    Start([ Security Audit Triggered]) --> Check1[1. Password Health Scan]
    Start --> Check2[2. Device Environment Scan]

    Check1 --> W1[Flag Weak / Short Passwords]
    Check1 --> W2[Flag Reused Passwords Across Accounts]
    Check1 --> W3[Flag Expired / Old Credentials >90 days]
    Check1 --> W4[Flag Missing 2FA TOTP Protection]

    Check2 --> D1[Root Detection: Checks su binaries & test-keys]
    Check2 --> D2[Emulator Detection: Detects VM environments]
    Check2 --> D3[KeyStore Check: Verifies physical TEE / StrongBox]
    Check2 --> D4[Play Integrity: Requests a token; no backend verifies it]

    W1 & W2 & W3 & W4 & D1 & D2 & D3 & D4 --> Report[ Comprehensive Health Dashboard & Recommendations]

    classDef trigger fill:#1E1B4B,stroke:#818CF8,stroke-width:2px,color:#E0E7FF;
    classDef category fill:#131A26,stroke:#F59E0B,stroke-width:2px,color:#FDE68A;
    classDef checkItem fill:#0F141C,stroke:#475569,stroke-width:1px,color:#F8FAFC;
    classDef reportItem fill:#064E3B,stroke:#34D399,stroke-width:2px,color:#A7F3D0;

    class Start trigger;
    class Check1,Check2 category;
    class W1,W2,W3,W4,D1,D2,D3,D4 checkItem;
    class Report reportItem;
```

---

##  System Architecture & Component Breakdown

Kunjika follows **Security-First Clean Architecture**, separating user interface controls from core cryptographic operations.

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
    'lineColor': '#F59E0B',
    'clusterBkg': '#0F141C',
    'clusterBorder': '#2B374A',
    'edgeLabelBackground': '#18202E'
  }
}}%%
graph TB
    subgraph UI [" 1. Presentation Layer (Jetpack Compose)"]
        A[Auth & Lock Screen]
        B[Vault & Credential Details]
        C[4-in-1 Generator Engine]
        D[Security & Health Audit]
        E[Web Drop QR & BLE Dialog]
    end

    subgraph VM [" 2. Controller Layer (ViewModels)"]
        F[AuthViewModel]
        G[VaultViewModel]
        H[GeneratorViewModel]
        I[SettingsViewModel]
    end

    subgraph Core [" 3. Security & Cryptographic Core"]
        J[KeystoreManager<br/><b>Android Keystore; hardware varies</b>]
        K[BiometricKeyManager<br/><b>BiometricPrompt CryptoObject</b>]
        L[BlockchainManager<br/><b>Local Signed Audit Chain</b>]
        M[WebDropCrypto & BLE<br/><b>ECDH P-256 + GATT Server</b>]
        N[QrEncryptionManager<br/><b>PBKDF2 600k + AES-GCM</b>]
        O[Security & Integrity<br/><b>Root, Emulator & Play Integrity</b>]
    end

    subgraph Storage [" 4. Storage Layer"]
        P[(SQLCipher Database<br/><b>256-bit AES SQLite</b>)]
        Q[(DataStore Preferences)]
    end

    UI --> VM
    VM --> Core
    Core --> Storage

    classDef goldNode fill:#131A26,stroke:#F59E0B,stroke-width:1.5px,color:#F8FAFC;
    classDef indigoNode fill:#1E1B4B,stroke:#818CF8,stroke-width:1.5px,color:#E0E7FF;
    classDef emeraldNode fill:#064E3B,stroke:#34D399,stroke-width:1.5px,color:#A7F3D0;
    classDef storageNode fill:#18202E,stroke:#FBBF24,stroke-width:2px,color:#FDE68A;

    class A,B,C,D,E goldNode;
    class F,G,H,I indigoNode;
    class J,K,L,M,N,O emeraldNode;
    class P,Q storageNode;

    style UI fill:#0F141C,stroke:#F59E0B,stroke-width:2px,color:#FDE68A
    style VM fill:#151238,stroke:#818CF8,stroke-width:2px,color:#C7D2FE
    style Core fill:#052e25,stroke:#34D399,stroke-width:2px,color:#A7F3D0
    style Storage fill:#18202E,stroke:#FBBF24,stroke-width:2px,color:#FDE68A
```

---

### Detailed Component Explanations

| Layer | Component | What It Does in Simple Terms | Technical Backing |
| :--- | :--- | :--- | :--- |
| **1. Presentation** | **Compose UI & Glossy Theme** | Renders all screens, high-contrast dark/light themes, 3D specular shine reflections, and interactive dialogs. | Jetpack Compose, Material 3, custom glossy canvas modifiers. |
| **2. Controller** | **ViewModels** | Coordinates user actions, handles input validation, and ensures data is passed cleanly to the cryptographic engine. | Android Architecture Components, StateFlow, Coroutines. |
| **3. Security Core** | **`KeystoreManager`** | Generates and manages the Master Encryption Key in Android Keystore. | Android Keystore; hardware backing varies by device. |
| **3. Security Core** | **`BiometricKeyManager`** | Provides an authentication-gated Keystore cipher used by the biometric app access flow. Current UI behavior and limitations are documented in the security guide. | `BiometricPrompt.CryptoObject`, AES-256-GCM. |
| **3. Security Core** | **`BlockchainManager`** | Records vault modifications in a local signed audit chain with known deletion and rollback limitations. | Elliptic Curve (`secp256r1`) signatures and HMAC digests. |
| **3. Security Core** | **`WebDropCrypto` & `BLE`** | Enables direct, wireless password sharing to desktop browsers with zero cloud relays. | Web Bluetooth GATT server, ephemeral ECDH P-256, HKDF-SHA-256, AES-GCM. |
| **3. Security Core** | **`QrEncryptionManager`** | Encrypts phone-to-phone data transfer packages into scannable QR codes. | PBKDF2-HMAC-SHA-256 (600,000 iterations), AES-256-GCM, random 16-digit code. |
| **3. Security Core** | **`PlayIntegrity` & `Security`** | Offers user-triggered root/emulator heuristics and Play Integrity token requests. No backend validates a trusted verdict. | Best-effort local checks and Google Play Integrity API. |
| **4. Storage** | **SQLCipher Database** | Stores your encrypted credentials, URLs, and notes on flash memory with cryptographically hardened 256-bit encryption. | SQLCipher SQLite engine, AES-256-CBC with HMAC-SHA512. |
| **4. Storage** | **DataStore** | Stores application configuration, theme choices, PIN hashes, and wrapped values. | AndroidX DataStore; sensitive values are separately protected where implemented. |

---

##  Security & Privacy Comparison

| Security Feature | Standard Cloud Password Managers | Kunjika Sovereign Vault |
| :--- | :---: | :---: |
| **Network Exposure** | Cloud connectivity varies | Android app has no `INTERNET` permission; local sharing paths remain |
| **Data Storage Location** | Cloud or local, depending on product | Encrypted local database; Keystore hardware backing varies |
| **Phone-to-PC Sharing** | Product-dependent | Direct Bluetooth transfer after user confirmation; receiving computer is trusted |
| **Database Encryption** | Product-dependent | SQLCipher passphrase wrapped by Android Keystore; Master PIN gates the UI |
| **Tamper Detection** | Product-dependent | Local signed audit chain detects some edits; cannot assure deletion or rollback detection |
| **Account Creation** | Product-dependent | No Kunjika account required; vault is local by default |
| **Screen Capture Mitigation** | Product-dependent | `FLAG_SECURE` blocks ordinary screenshots and recents previews; it cannot block every capture method |

---

##  Technology Stack

- **Platform**: Android 8.0+ (min API 26; target API 36)
- **Language**: Kotlin 2.4.21
- **UI Framework**: Jetpack Compose (BOM 2026.09.00) with Material 3
- **Database**: Room 2.8.5 + SQLCipher 4.19.1
- **Biometrics**: AndroidX Biometric 1.1.0 (`CryptoObject`)
- **Integrity**: Google Play Integrity API 1.6.0; the app can request tokens, but no backend verifies a trusted verdict
- **Web Drop Companion**: Vanilla HTML5, CSS3, Web Bluetooth API, SubtleCrypto WebCrypto API

---

##  Further Documentation

- [ Interactive Project Site & Online Guide](https://dwinsi.github.io/kunjika/)
- [ User Guide & Quickstart (Screenshots & Use Cases)](docs/user-guide.md)
- [ हिंदी मार्गदर्शिका (Hindi Guide)](https://dwinsi.github.io/kunjika/hindi.html)
- [ Deep Architecture Overview](docs/architecture.md)
- [ Cryptography & Threat Model](docs/security.md)
- [ Complete Feature Guide](docs/features.md)
- [ Release Notes & Changelog](RELEASE_NOTES.md)
- [ Web Companion Page](https://dwinsi.github.io/kunjika/web-companion/)
- [ Privacy Policy](PRIVACY_POLICY.md)
- [ Play Store Data Safety](PLAY_STORE_DATA_SAFETY.md)

---

##  Getting Started

### For Users & Testers
- **Install from Google Play**: [Google Play Store Listing](https://play.google.com/store/apps/details?id=com.kunjika.app) (Android 8.0+)
- **Interactive Project Website**: [https://dwinsi.github.io/kunjika/](https://dwinsi.github.io/kunjika/)
- **Web Drop Companion (PC)**: [https://dwinsi.github.io/kunjika/web-companion/](https://dwinsi.github.io/kunjika/web-companion/)

### For Developers
1. Clone the repository:
   ```bash
   git clone https://github.com/dwinsi/kunjika.git
   ```
2. Open the project in **Android Studio Ladybug (2024.2.1+)** or newer.
3. Sync project with Gradle files.
4. Run on a physical Android 8.0+ device or a compatible emulator. Hardware-backed Keystore features vary by device.

---

##  Community, Support & Bug Reports

Kunjika is an open-source project dedicated to local-first security and digital sovereignty.
-  **Report a Bug**: [Open an issue on GitHub Issues](https://github.com/dwinsi/kunjika/issues) (include device model, Android version, and reproduction steps).
-  **Ask Questions & Ideas**: [Join GitHub Discussions](https://github.com/dwinsi/kunjika/discussions).
-  **Project Site**: [https://dwinsi.github.io/kunjika/](https://dwinsi.github.io/kunjika/)
-  **Developer Contact**: Ashwin Singh ([winsimash@gmail.com](mailto:winsimash@gmail.com) / [@dwinsi](https://github.com/dwinsi))

---

##  License

Kunjika is open-source software licensed under the [MIT License](LICENSE).

