# KeyFortress - 100% Offline Secure Password Generator & Vault

A military-grade, zero-network, ultra-secure Password Generator and Password Vault built for **Android 16 (API 36) and higher** written in **100% Kotlin** and **Jetpack Compose (Material 3)**.

---

## Security & Privacy Architecture

- **Strict Zero-Network Guarantee (Air-Gapped)**:
  `AndroidManifest.xml` does **not** declare `android.permission.INTERNET` or any network capabilities. No telemetry, no ads, no cloud sync, zero socket creation.
- **Hardware-Backed Encryption**:
  Master encryption key is generated and safely stored in the hardware-backed **Android KeyStore** using **AES-256-GCM**.
- **SQLCipher Encrypted Database**:
  All passwords, accounts, metadata, notes, and tags are stored in a local SQLite database encrypted with **SQLCipher** and **Room**.
- **Biometric & Master PIN Unlock**:
  Full `BiometricPrompt` integration with fallback Master PIN protection, customizable auto-lock timer, and automatic locking when the app is backgrounded.
- **Screen Protection (`FLAG_SECURE`)**:
  Protects all screens against screenshots, screen recording, and masks the preview in the Android App Switcher/Recents task list.
- **Concealed Clipboard (`EXTRA_IS_SENSITIVE`) & Auto-Purge**:
  Marks copied passwords as sensitive (Android 13+ standard) to prevent keyboard clipboard logging, paired with a 30-second automated clipboard memory wipe.
- **Encrypted Local Backup & Restore**:
  Export and import encrypted backups using AES-256-GCM with PBKDF2-HMAC-SHA256 key derivation.

---

## Features

1. **Password Generator**:
   - Customizable character sets (Uppercase, Lowercase, Numbers, Symbols, Exclude Ambiguous).
   - Password Length slider (8 to 64 characters).
   - **Passphrase Mode**: Diceware-style memorable multi-word passphrases with custom separators and word count.
   - **PIN Mode**: Numeric PIN generator (4 to 12 digits).
   - Live **Entropy Score & Crack Time Estimate**.
   - One-tap copy with toast timer and direct **Save to Vault** action.

2. **Encrypted Vault**:
   - Store title, username/email, password, website URL, category, notes, and favorite status.
   - Instant offline search across titles, usernames, URLs, and notes.
   - Category filtering (Personal, Work, Finance, Social, Streaming, Other).
   - Conceal / Reveal password toggle with quick copy actions.

3. **Security Health & Password Audit**:
   - 100% offline vulnerability scan.
   - Calculates overall Vault Security Score (0 - 100).
   - Detects weak passwords (< 50 bits of entropy).
   - Detects reused/duplicate passwords across accounts.
   - Flags stale passwords not updated in 90+ days.

4. **Settings & Customization**:
   - Toggle Biometric Unlock (Fingerprint / Face).
   - Configure Auto-Lock Timeout (Instant, 30s, 1m, 5m).
   - Change Master PIN.
   - Export & Import Encrypted Backups.

---

## Project Structure

```
PasswordGenerator/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│       └── gradle-wrapper.properties
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        └── main/
            ├── AndroidManifest.xml
            ├── java/com/keyfortress/app/
            │   ├── KeyFortressApp.kt
            │   ├── MainActivity.kt
            │   ├── core/
            │   │   ├── security/
            │   │   │   ├── KeystoreManager.kt
            │   │   │   ├── BiometricAuthManager.kt
            │   │   │   └── ClipboardHelper.kt
            │   │   ├── generator/
            │   │   │   ├── PasswordGenerator.kt
            │   │   │   ├── PassphraseGenerator.kt
            │   │   │   ├── WordList.kt
            │   │   │   └── PasswordStrengthEvaluator.kt
            │   │   └── backup/
            │   │       └── BackupManager.kt
            │   ├── data/
            │   │   ├── local/
            │   │   │   ├── AppDatabase.kt
            │   │   │   ├── PasswordDao.kt
            │   │   │   └── PasswordEntity.kt
            │   │   ├── repository/
            │   │   │   └── PasswordRepository.kt
            │   │   └── preferences/
            │   │       └── UserPreferences.kt
            │   └── ui/
            │       ├── theme/
            │       │   ├── Color.kt
            │       │   ├── Theme.kt
            │       │   └── Type.kt
            │       ├── components/
            │       │   ├── StrengthIndicator.kt
            │       │   ├── CategoryChip.kt
            │       │   └── CustomTextField.kt
            │       ├── screens/
            │       │   ├── auth/AuthScreen.kt
            │       │   ├── generator/GeneratorScreen.kt
            │       │   ├── vault/
            │       │   │   ├── VaultScreen.kt
            │       │   │   ├── AddEditPasswordDialog.kt
            │       │   │   └── PasswordDetailDialog.kt
            │       │   ├── health/SecurityAuditScreen.kt
            │       │   └── settings/SettingsScreen.kt
            │       ├── viewmodel/
            │       │   ├── AuthViewModel.kt
            │       │   ├── GeneratorViewModel.kt
            │       │   ├── VaultViewModel.kt
            │       │   └── SettingsViewModel.kt
            │       └── MainNavigation.kt
            └── res/
                ├── values/
                │   ├── strings.xml
                │   ├── colors.xml
                │   └── themes.xml
                ├── drawable/
                │   └── ic_launcher_foreground.xml
                └── mipmap-anydpi-v26/
                    ├── ic_launcher.xml
                    └── ic_launcher_round.xml
```

---

## How to Open and Run in Android Studio

1. Open **Android Studio**.
2. Click **Open** and select the folder:
   `/Users/ashwinsingh/Desktop/myAnotherwork/PasswordGenerator`
3. Allow Gradle to sync the dependencies.
4. Select an **Android 16 (API 36)** emulator or connected device.
5. Click **Run (Shift + F10)**.
