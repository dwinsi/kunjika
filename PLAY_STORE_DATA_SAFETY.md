# Google Play Data Safety Information

When publishing Kunjika (v1.1.2), use the following information for the Google Play Data Safety form:

---

### 1. Data Collection and Security
- **Does your app collect or share any of the required user data types?** -> **No**. (Zero data collected, transmitted, or shared).
- **Is all user data encrypted in transit?** -> **Yes**. All local transmissions (both Phone-to-Phone QR Sync and Phone-to-PC Web Bluetooth Drop) are end-to-end encrypted using authenticated **AES-256-GCM** with ephemeral ECDH / PBKDF2 keys.
- **Do you provide a way for users to request that their data is deleted?** -> **Yes**. Uninstalling the app or using "Clear Data" permanently destroys all hardware keys and encrypted databases.
- **Does the app connect to the internet?** -> **No**. The app does not request or hold `android.permission.INTERNET`. Data physically cannot leave the device.

---

### 2. Data Types
Kunjika processes data strictly locally on the user's device. **None** is collected, uploaded to any server, or shared with third parties:
- **Personal Info**: Usernames, passwords, notes, and URLs stored exclusively in the encrypted local vault.
- **App Activity**: Local blockchain audit ledger stored exclusively on the device.

---

### 3. Data Usage & Permissions
All permissions requested by Kunjika are used strictly for **App Functionality** and **Local Device Security**:

| Permission | Category | Specific Purpose in Kunjika |
| :--- | :--- | :--- |
| `android.permission.USE_BIOMETRIC` | Authentication | Local hardware-backed vault unlocking and biometric wrapping of the Master PIN via KeyStore `CryptoObject`. |
| `android.permission.CAMERA` | Hardware | Scanning dynamic pairing and transfer QR codes for Air-Gapped Sync and Web Drop. |
| `android.permission.BLUETOOTH_ADVERTISE` | Device Connection | Broadcasting local GATT peripheral advertisements exclusively during an active, user-initiated Web Drop transfer to a nearby desktop browser. Never used for beaconing, background telemetry, or location tracking. |
| `android.permission.BLUETOOTH_CONNECT` | Device Connection | Streaming encrypted credential chunks to the paired browser during an active Web Drop session. |
| `android.permission.VIBRATE` | System Utilities | Haptic confirmation for biometric authentication and PIN entry. |

---

### 4. Integrity & Anti-Tamper Attestation
- **Google Play Integrity API**: Kunjika incorporates the Google Play Integrity API to allow users to verify application authenticity, detect APK repackaging, and test device environmental health. This attestation is performed between Google Play Services and on-device OS primitives; no credentials, vault items, or user data are included in integrity requests.

---

### 5. Privacy Policy URL
Public URL: `https://dwinsi.github.io/kunjika/PRIVACY_POLICY.html` (or `PRIVACY_POLICY.md` in repository).
