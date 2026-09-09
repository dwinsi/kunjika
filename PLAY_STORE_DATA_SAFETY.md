# Google Play Data Safety Information

When publishing Kunjika, use the following information for the Data Safety form:

### 1. Data Collection and Security
- **Does your app collect or share any of the required user data types?** -> **No**. (Zero data collected, transmitted, or shared).
- **Is all of the user data encrypted in transit?** -> **Yes**. All local transmissions (both Phone-to-Phone QR Sync and Phone-to-PC Web Bluetooth Drop) are end-to-end encrypted using authenticated **AES-256-GCM** with ephemeral ECDH / PBKDF2 keys.
- **Do you provide a way for users to request that their data is deleted?** -> **Yes**. Deleting the app or using "Clear Data" permanently shreds all hardware keys and encrypted databases.
- **Does the app connect to the internet?** -> **No**. The app does not request or hold `android.permission.INTERNET`.

### 2. Data Types
Kunjika technically "handles" the following data locally on the device, but **none** is collected, uploaded, or shared:
- **Personal Info**: Usernames, passwords, and URLs stored exclusively in the encrypted local vault.
- **App Activity**: Local blockchain audit log stored on the device.

### 3. Data Usage & Permissions
- All data handled by the app is used strictly for **App Functionality** and **Account Management** purposes locally on the device.
- **Permissions**:
  - `BLUETOOTH_ADVERTISE` & `BLUETOOTH_CONNECT`: Used exclusively for local, air-gapped device-to-device credential transmission (Web Drop) to nearby desktop browsers. Never used for tracking, beacons, or analytics.
  - `CAMERA`: Used exclusively for scanning local pairing and session QR codes.
  - `USE_BIOMETRIC`: Used for local hardware-backed vault unlocking and Web Drop authorization.

### 4. Privacy Policy URL
Public URL: `https://dwinsi.github.io/kunjika/` (or `PRIVACY_POLICY.md` in repository).
