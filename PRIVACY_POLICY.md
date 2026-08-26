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
- **In Transit (Local)**: When syncing data between devices via QR code, the data is encrypted with a session-specific AES key derived from a random 6-digit code.

### 3. Permissions Used
- **Biometric**: To allow you to unlock your vault securely.
- **Camera**: Only used for scanning encrypted QR codes during the Air-Gapped Sync process.
- **Vibrate**: To provide haptic feedback during authentication.

### 4. Third-Party Services
Kunjika is a standalone application and does not integrate with any third-party cloud services or APIs.

### 5. Changes to This Policy
We may update our Privacy Policy from time to time. Since the app is offline, any changes will be reflected in the updated version of the application and this document.

### 6. Contact Us
If you have any questions about this Privacy Policy, please contact the developer directly via the official repository.
