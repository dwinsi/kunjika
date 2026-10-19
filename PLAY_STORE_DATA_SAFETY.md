#  Google Play Data Safety Information

[![Version](https://img.shields.io/badge/version-1.8.0-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B)](RELEASE_NOTES.md)
[![Theme](https://img.shields.io/badge/theme-Obsidian%20Gold%20%26%20Midnight%20Indigo-F59E0B?style=flat-square&labelColor=1E1B4B&color=F59E0B)](docs/features.md)
[![Data Safety](https://img.shields.io/badge/data%20safety-review%20before%20submission-34D399?style=flat-square&labelColor=064E3B&color=34D399)](PLAY_STORE_DATA_SAFETY.md)
[![Collection](https://img.shields.io/badge/collection-review%20release%20behavior-818CF8?style=flat-square&labelColor=1E1B4B&color=818CF8)](PLAY_STORE_DATA_SAFETY.md)

This is a code-based checklist for source version 1.8.0 (version code 18). Verify every answer against the exact release artifact, current Play Console questionnaire, and any third-party SDK behavior before submitting; this file is not a certification.

---

### 1. Data Collection and Security
- **Does your app collect or share any of the required user data types?** -> Review the current Play Integrity integration, installed SDKs, and app behavior in the release artifact before answering. Vault records are local by default, but users can intentionally share selected data via system integrations and transfers.
- **Is all user data encrypted in transit?** -> Do not answer yes solely based on QR and Web Drop. Those payloads use authenticated encryption, but other user-initiated routes such as clipboard, Autofill, and exports have different protections. Review the exact question and release behavior in Play Console.
- **Do you provide a way for users to request that their data is deleted?** -> Answer based on the current app controls and Play Console definition. Clearing app data or uninstalling removes local app data; previously exported files or copied data are outside the app's control.
- **Does the app connect to the internet?** -> The manifest removes `android.permission.INTERNET` from the app. The app can request a Play Integrity token through Google Play services; validate how this is treated in the current disclosure form. The app also supports user-initiated Bluetooth and QR transfers.

---

### 2. Data Types
The app stores vault information locally by default. The exact Data Safety declaration must account for user-initiated sharing and the Play Integrity request through Google Play services. Review the final manifest, SDKs, and network/service behavior of the release artifact before submission.
- **Personal Info**: Usernames, passwords, notes, and URLs in the local vault; selected information can be copied, exported, or transferred by the user.
- **App Activity**: Selected vault events in a local signed audit chain.

---

### 3. Data Usage & Permissions
All permissions requested by Kunjika are used strictly for **App Functionality** and **Local Device Security**:

| Permission | Category | Specific Purpose in Kunjika |
| :--- | :--- | :--- |
| `android.permission.USE_BIOMETRIC` | Authentication | Biometric approval for vault access and Android Keystore `CryptoObject` operations; hardware backing varies by device. |
| `android.permission.CAMERA` | Hardware | Scanning dynamic pairing and transfer QR codes for Offline Sync and Web Drop. |
| `android.permission.BLUETOOTH_ADVERTISE` | Device Connection | Broadcasting local GATT peripheral advertisements exclusively during an active, user-initiated Web Drop transfer to a nearby desktop browser. Never used for beaconing, background telemetry, or location tracking. |
| `android.permission.BLUETOOTH_CONNECT` | Device Connection | Streaming encrypted credential chunks to the paired browser during an active Web Drop session. |
| `android.permission.VIBRATE` | System Utilities | Haptic confirmation for biometric authentication and PIN entry. |

---

### 4. Integrity & Anti-Tamper Checks
- **Google Play Integrity API**: The app can request a Play Integrity token through Google Play services. There is no backend in this project to verify the token, so do not describe this as a server-validated app-integrity verdict. Review Google's disclosures and the current Play Console form before making Data Safety declarations.

---

### 5. Privacy Policy URL
Public URL: `https://dwinsi.github.io/kunjika/PRIVACY_POLICY.html` (or `PRIVACY_POLICY.md` in repository).
