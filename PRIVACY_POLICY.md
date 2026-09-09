# Privacy Policy for Kunjika

**Effective Date: August 26, 2026**

Kunjika is a 100% offline password manager. We believe that your data belongs solely to you, and our application is designed to ensure it never leaves your device.

### 1. No Data Collection
Kunjika **does not collect, transmit, or share** any personal information, passwords, or usage statistics.
- **Zero Network Permissions**: The application does not request the `INTERNET` permission.
- **No Analytics**: We do not use any third-party analytics or tracking libraries.
- **No Cloud Storage**: We do not provide cloud synchronization. Your data remains in the encrypted local database on your device.

### 2. Data Encryption
All data stored within the app is protected using military-grade encryption:
- **At Rest**: Data is stored in a SQLCipher-encrypted database, further protected by field-level AES-256-GCM encryption with hardware-backed keys (Android KeyStore).
- **In Transit (Local Air-Gapped)**:
  - **Phone-to-Phone QR Sync**: Encrypted using AES-256-GCM with keys derived via PBKDF2 from a one-time 6-digit code.
  - **Phone-to-PC Web Drop**: Encrypted using AES-256-GCM with keys derived from ephemeral ECDH P-256 key agreement over Web Bluetooth (BLE GATT), guarded by a visual 6-digit Short Authentication String (SAS) and biometric authorization.

### 3. Permissions Used
- **Biometric**: To allow you to unlock your vault and authorize Web Drop transfers securely.
- **Camera**: Only used for scanning encrypted QR codes during Air-Gapped Sync and Web Drop pairing.
- **Bluetooth (`BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`)**: Only used to broadcast and transmit encrypted credentials directly to your desktop/laptop browser during an active Web Drop session. Never used for beacon tracking, location discovery, or background telemetry.
- **Vibrate**: To provide haptic feedback during authentication.

### 4. Web Companion Privacy (Web Drop)
The Kunjika Web Companion ([https://dwinsi.github.io/kunjika/web-companion/](https://dwinsi.github.io/kunjika/web-companion/)) is completely client-side:
- Executes 100% in your local browser's volatile memory (RAM).
- Sets zero cookies, uses zero web tracking/analytics, and writes zero data to persistent browser storage (`localStorage`, `sessionStorage`, or `IndexedDB`).
- Automatically wipes received credentials from memory and clears the system clipboard after 30 seconds.

### 5. Third-Party Services
Kunjika is a standalone application and does not integrate with any third-party cloud services or APIs.

### 6. Changes to This Policy
We may update our Privacy Policy from time to time. Since the app is offline, any changes will be reflected in the updated version of the application and this document.

### 7. Contact Us
If you have any questions about this Privacy Policy, please contact the developer directly via the official repository.
