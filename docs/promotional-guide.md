# 🏰 Kunjika: Sovereign Password Vault & Authenticator
## Official Promotional, Positioning & Educational Guide

> **"Your Passwords Belong to You. Not to Cloud Servers."**  
> *A local-first Credential Vault & Authenticator for Android.*

---

## 📌 Executive Summary & The Core Hook

Every day, hundreds of millions of people entrust their most sensitive secrets—bank logins, email credentials, social media accounts, and crypto recovery phrases—to centralized cloud password managers. 

Over the past few years, the tech world has learned a harsh lesson: **centralized cloud vaults are prime targets for sophisticated threat actors.** From high-profile cloud breaches exposing millions of encrypted vault blobs and unencrypted vault metadata to rising $36–$60/year recurring subscription models, users are paying companies to store their digital keys on computers they do not control.

Meanwhile, traditional offline password managers (like KeePass) suffer from outdated user interfaces, painful synchronization workflows, and one glaring daily annoyance: **manually typing 30-character high-entropy passwords (`k8#Q!v9$L2z*M...`) from a smartphone screen onto a computer keyboard.**

**Kunjika addresses these needs with local encrypted storage and user-controlled sharing.**

Kunjika is an Android password manager and TOTP authenticator with local encrypted storage. The Android app removes the `INTERNET` permission and has no cloud sync service. Android Keystore backing depends on the device. Bluetooth, QR, and clipboard are data-sharing paths. Web Drop sends a selected credential to a browser over Bluetooth after biometric authorization and a user-compared pairing code; use only a trusted computer and browser.

---

## 🚨 Why Users Need Kunjika: The 4 Big Problems We Solve

### 1. The Cloud Vulnerability Crisis
- **The Problem:** Cloud password managers advertise "zero-knowledge encryption," yet their servers hold your encrypted vault files, email addresses, IP access logs, and website URLs. If their cloud infrastructure is compromised, attackers can copy your entire vault offline and subject it to specialized GPU brute-force clusters.
- **The Kunjika Solution:** Kunjika has no cloud accounts or sync service and removes Android's `INTERNET` permission. It can share data locally through Bluetooth and QR when you initiate a transfer.

### 2. The Subscription Tax on Security
- **The Problem:** Leading commercial password managers charge $3 to $5 every single month just to autofill passwords across devices or store two-factor authentication codes. If you cancel your subscription, your access is throttled or locked down.
- **The Kunjika Solution:** Kunjika is **100% Free and Open Source (MIT License)**. You own your software, you own your keys, and you never pay a dime to protect your digital sovereignty.

### 3. The Cross-Device Typing Nightmare
- **The Problem:** With offline password managers, logging into a website on your desktop or work laptop requires manually reading a long, complex password from your phone screen and typing each symbol, number, and uppercase letter one by one on your physical keyboard. Users get frustrated and downgrade to weak passwords like `Summer2024!`.
- **The Kunjika Solution:** **Kunjika Web Drop** transfers a selected password over Bluetooth. Compare the pairing codes before approving. The browser does not copy automatically; clipboard history may retain any value you copy, and browser memory cleanup is best-effort.

### 4. App Fragmentation (Passwords vs 2FA Authenticator)
- **The Problem:** Users juggle one app for passwords (e.g., Chrome or 1Password) and a second separate app for 2FA verification codes (e.g., Google Authenticator). If either is lost or de-synced, logins break.
- **The Kunjika Solution:** Kunjika integrates a full **RFC 6238-compliant TOTP Authenticator** directly inside each vault entry, complete with animated 30-second countdown rings. One tap copies your password; one tap copies your 2FA code.

---

## 🎯 What End Users Can Achieve with Kunjika

| What You Can Achieve | How Kunjika Delivers It | Everyday User Impact |
| :--- | :--- | :--- |
| **Local Data Storage** | SQLCipher with a random passphrase wrapped by Android Keystore; hardware backing varies by device. | No Kunjika cloud vault or sync service. A compromised or rooted device remains a risk. |
| **No Android Internet Permission** | The app manifest removes `android.permission.INTERNET`; Bluetooth and QR remain supported. | No app internet socket access; local transfers still share data with nearby devices. |
| **Web Drop** | BLE payload uses ephemeral ECDH, transcript-bound HKDF, and AES-GCM. | Transfer only to a trusted computer; the browser must see the decrypted password. |
| **Consolidated 2FA & Password Management** | Built-in TOTP authenticator engine generating standard 6-digit rolling codes. | Ditch standalone authenticator apps; all your credentials and secondary factors live securely in one sovereign place. |
| **Phone-to-Phone Transfer** | AES-GCM encrypted QR payload protected by a random 16-digit code and PBKDF2. | Keep the QR and code separate; do not share both in a photo. |
| **Local Audit Chain** | A Keystore-signed hash chain records events. | Detects some edits; it is not an immutable blockchain and cannot guarantee detection of deletion or app compromise. |
| **Effortless Mobile Experience** | Native Android Autofill service integrated with biometric fingerprint / face unwrap. | Log into apps like banking, social media, and shopping with a single tap of your finger. |
| **Backup and Recovery** | Encrypted vault exports use AES-GCM and PBKDF2. The local database passphrase PDF is unencrypted and is not a cross-device restore. | Keep encrypted exports and a strong, unique passphrase in a separate safe place. |

---

## ⚡ Spotlight Feature: Kunjika Web Drop (Offline Direct PC Sync)

### How It Works Without the Internet

```
[ Laptop Browser (Chrome/Edge/Brave) ]                     [ Kunjika Mobile App ]
                 │                                                    │
    1. Generates Ephemeral ECDH Key (P-256)                           │
    2. Creates one-time session and random BLE service                │
    3. Displays Dynamic Pairing QR Code                               │
                 │                                                    │
                 │ <─────────── 4. Scan QR Code with Phone ───────────┤
                 │                                                    │
                 │                          5. Creates ephemeral session │
                 │                          6. Derives HKDF Shared Key│
                 │                          7. Displays 6-Digit Code  │
                 │                          8. Displays phone code    │
                 │                                                    │
    9. Visual Confirmation:                                           │
       Both screens show matching code (e.g., "544 655")              │
                 │                                                    │
                 │                          10. Biometric authorizes BLE
                 │                          11. User compares code    │
                 │                          12. AES-GCM encrypts data │
                 │                                                    │
                 │ <──────── 13. Transmits BLE Data Chunks ───────────┤
                 │                                                    │
   14. Decrypts payload directly into RAM                             │
   15. Shows masked password; copy is user initiated                   │
   16. Closing clears visible fields best-effort                       │
```

### Why Users Love It:
1. **Zero software to install** on your laptop—it runs in standard modern web browsers.
2. **Local transfer**—Bluetooth transfers data directly between the phone and browser; the web page itself must first be available on the laptop.
3. **Clipboard caution**—the companion does not auto-copy or clear clipboard content. Clipboard history and software on the receiving computer may retain copied credentials.

---

## 👥 Who Is Kunjika For? (User Personas)

### 1. The Everyday Smartphone User
- **Goal:** Wants strong, unique passwords for Instagram, Netflix, banking, and shopping without having to memorize 50 different passwords or pay a monthly fee.
- **Why Kunjika:** Intuitive 4-in-1 generator with Diceware passphrases (e.g. `correct-horse-battery-staple`), fingerprint unlock, and Android native autofill.

### 2. The Remote Worker & Professional
- **Goal:** Uses a mobile phone and a laptop all day. Needs to quickly access complex work credentials on their laptop without typing them manually or copying them through insecure messaging apps like Slack or WhatsApp.
- **Why Kunjika:** Kunjika Web Drop beams high-entropy passwords directly into the laptop clipboard over Bluetooth in 2 seconds, then wipes the clipboard clean.

### 3. The Privacy & Security Advocate
- **Goal:** Adheres to zero-trust principles. Dislikes big-tech telemetry, closed-source security claims, and cloud sync risks.
- **Why Kunjika:** Open-source (MIT), no Android internet permission, Android Keystore protection (hardware backing varies), and local audit checks. Root checks are best-effort and do not stop a compromised OS.

### 4. The Crypto Trader & Web3 Enthusiast
- **Goal:** Needs a dedicated, offline repository for seed phrases, private notes, exchange credentials, and 2FA keys.
- **Why Kunjika:** Local encrypted storage, Android `FLAG_SECURE` screenshot protection, and encrypted portable exports. The printed database passphrase is unencrypted and is not a cross-device restore.

---

## 📊 Feature Comparison: Kunjika vs. The Competition

| Feature | 🏰 Kunjika | 🔴 LastPass | 🛡️ 1Password | 🟦 Bitwarden | 🌐 Chrome Autofill | 🗝️ KeePass |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Network Permission** | No Android `INTERNET` permission; local sharing still available | Varies | Varies | Varies | Varies | Varies |
| **Subscription Cost** | **Free ($0 Forever)** | $36/year | $36–$60/year | $10–$40/year | Free | Free |
| **Cross-Device PC Sync** | **⚡ Direct Offline Web Drop** | Cloud Server | Cloud Server | Cloud Server | Google Account | Manual USB/Sync |
| **Built-In 2FA Authenticator** | **✅ Yes (Included)** | Paid Only | Paid Only | Paid Only | ❌ No | Plugin Required |
| **Encryption Hardware Layer** | **Hardware TEE / StrongBox** | Software | Software | Software | OS Keychain | Software |
| **Audit Log Integrity** | **Hardware-Signed Blockchain** | None | None | None | None | None |
| **Screen Screenshot Blocking** | **✅ `FLAG_SECURE`** | Varies | Varies | Varies | ❌ No | Varies |
| **Open Source** | **✅ Yes (MIT)** | ❌ Closed | ❌ Closed | Partial | Partial | ✅ Yes |

---

## ❓ Frequently Asked Questions (FAQ)

#### Q1: If I lose or break my phone, how do I restore?
> **Answer:** Use an encrypted vault export created before the loss. Protect it with a strong, unique passphrase. The local database passphrase PDF alone cannot restore field-encrypted entries on a new device.

#### Q2: How does Web Drop work if there is no internet?
> **Answer:** Web Drop uses Bluetooth Low Energy between your phone and the browser. The Android app has no internet permission, but the hosted companion page is mutable and a compromised receiving computer can read the credential.

#### Q3: Is my Master PIN stored on the phone?
> **Answer:** The app stores a PBKDF2 hash of the Master PIN. When biometric unlock is enabled, a Keystore key protects a saved PIN for convenience. The Master PIN gates app access; it is not the database key. Hardware backing varies by device.

#### Q4: What makes Kunjika's generator different from basic generators?
> **Answer:** Kunjika gives you 4 distinct generation modes powered by cryptographically secure pseudo-random number generators (`SecureRandom`):
> - **Standard Passwords:** High-entropy character mixes.
> - **Diceware Passphrases:** Easy-to-remember multi-word phrases from an offline 10,000-word dictionary.
> - **Numeric PINs:** Custom-length security codes for bank cards or alarms.
> - **TOTP Secrets:** Cryptographic seeds for 2FA authenticators.  
> It also keeps an encrypted **Generation History** so you never lose a newly created password before saving it.

---

## 📢 Promotional Copy Snippets (Ready to Share)

### 🏷️ 1-Line Tagline
> *"Kunjika is a local-first Android vault with encrypted storage and direct Bluetooth transfer to a trusted computer."*

### 📱 100-Word Elevator Pitch
> *Kunjika is a local-first Android password vault and TOTP authenticator. The app removes Android's INTERNET permission and stores vault data in SQLCipher with Android Keystore protection. Web Drop can transfer a selected credential over Bluetooth after pairing-code comparison and phone approval. Use only with a trusted receiving computer; clipboard and memory cleanup are best-effort.*

### 🐦 Social Media Launch Thread (X / LinkedIn / Mastodon)
> **1/5** Cloud password managers keep getting breached, and they charge you $36/year for the privilege. We built a better way: Meet **Kunjika** 🏰  
> 
> **2/5** 🚫 Zero Network: `android.permission.INTERNET` is not even in the code. The app operates strictly offline, removing remote cloud attack surfaces entirely.  
> 
> **3/5** ⚡ Web Drop: Web Drop transfers a selected credential to a browser over Bluetooth after biometric approval and a user-compared pairing code. Use only a trusted computer; clipboard and browser memory cleanup are best-effort.
> 
> **4/5** 🔐 Encrypted Local Vault: SQLCipher and Android Keystore protect local data. Hardware backing varies by device; the local audit chain has limits and does not protect against a compromised device.
> 
> **5/5** 100% Free & Open Source (MIT). Join our 14-Day Play Store Closed Beta today: https://dwinsi.github.io/kunjika/
