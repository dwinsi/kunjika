# 🏰 Kunjika: Sovereign Password Vault & Authenticator
## Official Promotional, Positioning & Educational Guide

> **"Your Passwords Belong to You. Not to Cloud Servers."**  
> *A 100% Offline, Hardware-Hardened Credential Vault & Cross-Device Authenticator for Android 15+ (API 35).*

---

## 📌 Executive Summary & The Core Hook

Every day, hundreds of millions of people entrust their most sensitive secrets—bank logins, email credentials, social media accounts, and crypto recovery phrases—to centralized cloud password managers. 

Over the past few years, the tech world has learned a harsh lesson: **centralized cloud vaults are prime targets for sophisticated threat actors.** From high-profile cloud breaches exposing millions of encrypted vault blobs and unencrypted vault metadata to rising $36–$60/year recurring subscription models, users are paying companies to store their digital keys on computers they do not control.

Meanwhile, traditional offline password managers (like KeePass) suffer from outdated user interfaces, painful synchronization workflows, and one glaring daily annoyance: **manually typing 30-character high-entropy passwords (`k8#Q!v9$L2z*M...`) from a smartphone screen onto a computer keyboard.**

**Kunjika solves both dilemmas once and for all.**

Kunjika is a 100% offline, sovereign Android password manager and 2FA authenticator that locks your secrets inside your phone’s physical silicon chips (**Hardware TEE / StrongBox**) with **Double-Lock Encryption**. It requests **ZERO network permissions** (`android.permission.INTERNET` is physically omitted from code), ensuring the app has zero network access and never transmits your data to remote servers. And with its revolutionary **Kunjika Web Drop**, it lets you beam credentials to your laptop or desktop browser in 2 seconds via **Web Bluetooth**—with zero internet, zero cloud servers, and zero software installation required on your computer.

---

## 🚨 Why Users Need Kunjika: The 4 Big Problems We Solve

### 1. The Cloud Vulnerability Crisis
- **The Problem:** Cloud password managers advertise "zero-knowledge encryption," yet their servers hold your encrypted vault files, email addresses, IP access logs, and website URLs. If their cloud infrastructure is compromised, attackers can copy your entire vault offline and subject it to specialized GPU brute-force clusters.
- **The Kunjika Solution:** Kunjika operates completely offline. It contains no tracking SDKs, no analytics, no account registration, and zero network code. Your data physically cannot leave your phone over the web (local device-to-device transfers occur strictly under your biometric control via Bluetooth Web Drop or Encrypted QR).

### 2. The Subscription Tax on Security
- **The Problem:** Leading commercial password managers charge $3 to $5 every single month just to autofill passwords across devices or store two-factor authentication codes. If you cancel your subscription, your access is throttled or locked down.
- **The Kunjika Solution:** Kunjika is **100% Free and Open Source (MIT License)**. You own your software, you own your keys, and you never pay a dime to protect your digital sovereignty.

### 3. The Cross-Device Typing Nightmare
- **The Problem:** With offline password managers, logging into a website on your desktop or work laptop requires manually reading a long, complex password from your phone screen and typing each symbol, number, and uppercase letter one by one on your physical keyboard. Users get frustrated and downgrade to weak passwords like `Summer2024!`.
- **The Kunjika Solution:** **Kunjika Web Drop**. Open the free companion web page in Chrome, Edge, Brave, or Opera, scan the pairing QR code with your phone camera, tap your fingerprint, and your password is instantly decrypted into your computer's RAM and copied to your clipboard—with an automatic 30-second memory and clipboard wipe.

### 4. App Fragmentation (Passwords vs 2FA Authenticator)
- **The Problem:** Users juggle one app for passwords (e.g., Chrome or 1Password) and a second separate app for 2FA verification codes (e.g., Google Authenticator). If either is lost or de-synced, logins break.
- **The Kunjika Solution:** Kunjika integrates a full **RFC 6238-compliant TOTP Authenticator** directly inside each vault entry, complete with animated 30-second countdown rings. One tap copies your password; one tap copies your 2FA code.

---

## 🎯 What End Users Can Achieve with Kunjika

| What You Can Achieve | How Kunjika Delivers It | Everyday User Impact |
| :--- | :--- | :--- |
| **Complete Digital Sovereignty** | Hardware-backed KeyStore TEE + SQLCipher double encryption with PBKDF2 (100,000 iterations). | You hold the only key in existence. Even if someone physically steals your phone, your database cannot be decrypted without your Master PIN. |
| **Zero Cloud Exposure & No Telemetry** | No `android.permission.INTERNET` declared in `AndroidManifest.xml`. | Complete immunity from cloud database breaches, data brokers, telemetry harvesting, or server outages. |
| **Instant PC Logins in 2 Seconds** | Web Bluetooth GATT transmission protected by ephemeral ECDH P-256 key exchange & client-side Proof-of-Work. | Log into your computer or work laptop with high-entropy passwords effortlessly without typing a single character. |
| **Consolidated 2FA & Password Management** | Built-in TOTP authenticator engine generating standard 6-digit rolling codes. | Ditch standalone authenticator apps; all your credentials and secondary factors live securely in one sovereign place. |
| **Peer-to-Peer Device Migrations** | Encrypted QR Code phone-to-phone data transfer with single-use 6-digit PIN. | Switch to a new phone or share a home Wi-Fi password with a family member without using cloud sync, email, or messaging apps. |
| **Provable Tamper Resistance** | On-device, hardware-signed (`secp256r1` ECDSA) local blockchain audit log. | Any unauthorized modification, file corruption, or database tampering is immediately caught and alerted. |
| **Effortless Mobile Experience** | Native Android Autofill service integrated with biometric fingerprint / face unwrap. | Log into apps like banking, social media, and shopping with a single tap of your finger. |
| **Disaster Recovery Preparedness** | One-click encrypted, printable Emergency Recovery Kit with automatic cache shredding. | Peace of mind that your family or trusted contacts can recover critical accounts in emergencies via a secure physical safe copy. |

---

## ⚡ Spotlight Feature: Kunjika Web Drop (Offline Direct PC Sync)

### How It Works Without the Internet

```
[ Laptop Browser (Chrome/Edge/Brave) ]                     [ Kunjika Mobile App ]
                 │                                                    │
    1. Generates Ephemeral ECDH Key (P-256)                           │
    2. Computes Client-Side Proof of Work                             │
    3. Displays Dynamic Pairing QR Code                               │
                 │                                                    │
                 │ <─────────── 4. Scan QR Code with Phone ───────────┤
                 │                                                    │
                 │                          5. Verifies PoW Challenge │
                 │                          6. Derives HKDF Shared Key│
                 │                          7. Displays 6-Digit Code  │
                 │                          8. Advertises via BLE GATT│
                 │                                                    │
    9. Visual Confirmation:                                           │
       Both screens show matching code (e.g., "544 655")              │
                 │                                                    │
                 │                          10. User Touches Fingerprint
                 │                          11. AES-256-GCM Encrypts  │
                 │                          12. Hardware signs block  │
                 │                                                    │
                 │ <──────── 13. Transmits BLE Data Chunks ───────────┤
                 │                                                    │
   14. Decrypts payload directly into RAM                             │
   15. Copies password to computer clipboard                          │
   16. Auto-shreds browser memory & clipboard in 30 seconds!          │
```

### Why Users Love It:
1. **Zero software to install** on your laptop—it runs in standard modern web browsers.
2. **Works completely offline**—your phone and computer can both be in Airplane mode.
3. **No clipboard lingering**—the 30-second auto-wipe protects against unauthorized snooping on shared or work computers.

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
- **Why Kunjika:** Open-source (MIT), zero internet permissions, hardware-backed Android KeyStore TEE, local blockchain audit log, and root/emulator tamper defenses.

### 4. The Crypto Trader & Web3 Enthusiast
- **Goal:** Needs a dedicated, offline repository for seed phrases, private notes, exchange credentials, and 2FA keys.
- **Why Kunjika:** Completely offline, protected against screenshots by Android `FLAG_SECURE`, double-lock AES-256 encryption, and printable emergency recovery kits.

---

## 📊 Feature Comparison: Kunjika vs. The Competition

| Feature | 🏰 Kunjika | 🔴 LastPass | 🛡️ 1Password | 🟦 Bitwarden | 🌐 Chrome Autofill | 🗝️ KeePass |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Network Permission** | **🚫 0% (100% Offline)** | 100% Cloud | 100% Cloud | 100% Cloud | 100% Cloud | Offline |
| **Subscription Cost** | **Free ($0 Forever)** | $36/year | $36–$60/year | $10–$40/year | Free | Free |
| **Cross-Device PC Sync** | **⚡ Direct Offline Web Drop** | Cloud Server | Cloud Server | Cloud Server | Google Account | Manual USB/Sync |
| **Built-In 2FA Authenticator** | **✅ Yes (Included)** | Paid Only | Paid Only | Paid Only | ❌ No | Plugin Required |
| **Encryption Hardware Layer** | **Hardware TEE / StrongBox** | Software | Software | Software | OS Keychain | Software |
| **Audit Log Integrity** | **Hardware-Signed Blockchain** | None | None | None | None | None |
| **Screen Screenshot Blocking** | **✅ `FLAG_SECURE`** | Varies | Varies | Varies | ❌ No | Varies |
| **Open Source** | **✅ Yes (MIT)** | ❌ Closed | ❌ Closed | Partial | Partial | ✅ Yes |

---

## ❓ Frequently Asked Questions (FAQ)

#### Q1: If Kunjika is 100% offline, what happens if I lose or break my phone?
> **Answer:** Because Kunjika has no cloud servers, nobody else holds your passwords. That is why Kunjika includes two easy, sovereign backup tools:
> 1. **Emergency Recovery Kit:** Generates an encrypted, printable backup you can store in your safe or safety deposit box.
> 2. **Encrypted Vault Export:** Export an encrypted backup file to an external USB flash drive or physical storage.

#### Q2: How does Web Drop work if there is no internet?
> **Answer:** Web Drop uses **Web Bluetooth Low Energy (BLE)** directly between your phone and your computer browser. Bluetooth is a localized radio connection, not the internet. Data travels strictly through the air across a few feet directly between the two devices.

#### Q3: Is my Master PIN stored on the phone?
> **Answer:** No plaintext PIN is ever stored. Your Master PIN is encrypted inside the phone's physical security chip (Android KeyStore TEE/StrongBox). When you touch your fingerprint, the hardware chip safely unwraps the decryption key.

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
> *"Kunjika is the 100% offline, hardware-hardened Android vault that locks your passwords in physical silicon and beams them to your laptop in 2 seconds with zero internet."*

### 📱 100-Word Elevator Pitch
> *Tired of cloud password manager breaches and recurring subscription fees? Meet Kunjika, the sovereign Android password vault and 2FA authenticator with zero internet permissions. Engineered with Android 15+ hardware-backed KeyStore encryption and SQLCipher double-lock security, Kunjika keeps your credentials strictly under your control. Need to log in on your laptop? Kunjika Web Drop beams your password directly to your desktop browser via Web Bluetooth in 2 seconds—without Wi-Fi, without cloud servers, and with an automatic 30-second clipboard shredder. 100% free, private, and open source.*

### 🐦 Social Media Launch Thread (X / LinkedIn / Mastodon)
> **1/5** Cloud password managers keep getting breached, and they charge you $36/year for the privilege. We built a better way: Meet **Kunjika** 🏰  
> 
> **2/5** 🚫 Zero Network: `android.permission.INTERNET` is not even in the code. The app operates strictly offline, removing remote cloud attack surfaces entirely.  
> 
> **3/5** ⚡ Web Drop: The biggest complaint about offline vaults is typing 30-char passwords onto your laptop. Web Drop beams credentials to your PC browser over Bluetooth in 2s with zero internet and a 30s auto-wipe!  
> 
> **4/5** 🔐 Double-Lock Crypto: Encrypted by your phone’s physical security chip (TEE) + SQLCipher + hardware-signed local blockchain audit ledger.  
> 
> **5/5** 100% Free & Open Source (MIT). Join our 14-Day Play Store Closed Beta today: https://dwinsi.github.io/kunjika/
