# Architecture Documentation

KeyFortress follows a modern Android architecture based on **Clean Architecture** and **MVVM (Model-View-ViewModel)** patterns, optimized for offline-first security.

## Core Components

The app is divided into three main layers:

1.  **UI Layer**: Jetpack Compose screens and ViewModels.
2.  **Domain/Repository Layer**: Repositories that orchestrate data flow between local storage and business logic.
3.  **Data Layer**: Room database (SQLCipher), DataStore (Preferences), and core security managers (Android KeyStore).

## System Architecture Diagram

```mermaid
graph TD
    subgraph UI ["UI Layer (Jetpack Compose)"]
        A[MainActivity] --> B[MainNavigation]
        B --> C[GeneratorScreen]
        B --> D[VaultScreen]
        B --> E[SecurityAuditScreen]
        B --> F[SettingsScreen]
        
        C --> VM1[GeneratorViewModel]
        D --> VM2[VaultViewModel]
        E --> VM2
        F --> VM3[SettingsViewModel]
    end

    subgraph Domain ["Repository Layer"]
        VM1 --> R1[HistoryRepository]
        VM2 --> R2[PasswordRepository]
        VM3 --> R2
        VM3 --> P[UserPreferences]
        VM1 --> G[Generator Core]
    end

    subgraph Data ["Data Layer (Secure Storage)"]
        R1 --> DB1[(SQLCipher History DB)]
        R2 --> DB2[(SQLCipher Vault DB)]
        P --> DS[(Encrypted DataStore)]
        DB1 & DB2 --> K[Android KeyStore]
    end

    subgraph Services ["Background Services"]
        S1[AutofillService] --> R2
    end
```

## Data Flow

1.  **Generation**: `GeneratorViewModel` calls the `PasswordGenerator` core. Once generated, the password is encrypted via `KeystoreManager` and saved to `HistoryRepository`.
2.  **Vault Management**: `VaultViewModel` interacts with `PasswordRepository`. Passwords are encrypted *before* hitting the database using a hardware-backed key.
3.  **Security Audit**: `VaultViewModel` performs a background scan of all decrypted credentials to calculate entropy and detect reuse without ever exposing raw data to the system.
4.  **Autofill**: The `KeyFortressAutofillService` runs in a separate system process, querying the `PasswordRepository` securely to suggest credentials in other apps.
