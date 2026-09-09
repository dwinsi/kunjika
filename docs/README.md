# 📚 Kunjika Documentation Index

Welcome to the technical documentation for **Kunjika**. This guide provides a deep dive into the security architecture and features of the application.

## 📖 Table of Contents

1.  **[🏗️ Architecture & Data Flow](architecture.md)**
    *   System design, MVVM implementation, Core Security layer, Web Bluetooth peripheral, and modern Android stack.
2.  **[🛡️ Security & Hardening](security.md)**
    *   Details on Double Encryption, Hardware-Backed KeyStore, PBKDF2, Local Blockchain Audit Log, and the Web Drop Cryptographic Protocol (Proof of Work + Ephemeral ECDH + SAS).
3.  **[✨ Feature Guide](features.md)**
    *   Deep dive into the Smart Generator, Encrypted Vault, TOTP support, Phone-to-Phone QR Sync, and Air-Gapped Web Drop (PC Sync).
4.  **[🌐 Web Companion](https://dwinsi.github.io/kunjika/web-companion/)**
    *   Zero-install, client-side web companion for receiving credentials over Web Bluetooth with automatic memory wipe.

---

## 📊 Visualizing the Logic
All diagrams in this documentation are written in **Mermaid.js** format. 

> [!TIP]
> If you are viewing this in Android Studio, ensure the **Mermaid** plugin is installed to see the interactive diagrams.

---

## 🔒 Security Promise
Kunjika is designed to be **100% offline**. We guarantee that no sensitive data ever leaves your device's secure hardware.
