# Google Play Data Safety Information

When publishing KeyFortress, use the following information for the Data Safety form:

### 1. Data Collection and Security
- **Does your app collect or share any of the required user data types?** -> No.
- **Is all of the user data collected by your app encrypted in transit?** -> Yes (Specifically for the local QR sync feature).
- **Do you provide a way for users to request that their data is deleted?** -> Yes (Deleting the app or using "Clear Data" permanently removes all data).

### 2. Data Types
KeyFortress technically "handles" the following data locally, but **none** is collected or shared:
- **Personal Info**: Usernames/Emails stored in the vault.
- **App Activity**: Local blockchain audit log of actions.

### 3. Data Usage
- All data handled by the app is used for **App Functionality** and **Account Management** purposes locally on the device.

### 4. Privacy Policy URL
You should host the `PRIVACY_POLICY.md` content on a public URL (e.g., GitHub Pages or a simple Gist) to provide the link to Google.
