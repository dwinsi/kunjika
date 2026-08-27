# Package Renaming Plan: KeyFortress to kunjika

This plan outlines the steps to complete the package renaming from `com.keyfortress.app` to `com.kunjika.app`.

## User Review Required

> [!IMPORTANT]
> The directory structure for the source code has already been partially updated to `com/kunjika/app`, but many files still declare `package com.keyfortress.app` and use imports from that namespace. This plan will synchronize all files to use `com.kunjika.app`.

## Proposed Changes

### [app]

Summary of changes across the `:app` module.

#### [MODIFY] [build.gradle.kts](file:///Users/ashwinsingh/PasswordGenerator/app/build.gradle.kts)
- (Already updated, but will double-check)

#### [MODIFY] [settings.gradle.kts](file:///Users/ashwinsingh/PasswordGenerator/settings.gradle.kts)
- Update `rootProject.name` to `"kunjika"` (or "Kunjika").

#### [MODIFY] [AndroidManifest.xml](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/AndroidManifest.xml)
- (Already updated, but will double-check for any absolute package references)

#### [MODIFY] [Kotlin Files](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/java/com/kunjika/app/)
Bulk update all Kotlin files in `app/src/main/java/com/kunjika/app/` and `app/src/test/java/com/kunjika/app/`:
- Replace `package com.keyfortress.app` with `package com.kunjika.app`.
- Replace `import com.keyfortress.app` with `import com.kunjika.app`.

#### [MODIFY] [Resource Files](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/res/)
- `themes.xml`: Rename `Theme.KeyFortress` to `Theme.Kunjika`.
- `autofill_service_config.xml`: Update `settingsActivity` if it uses the old package.

#### [MODIFY] [SettingsViewModel.kt](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/java/com/kunjika/app/ui/viewmodel/SettingsViewModel.kt)
- Update hardcoded strings: `"KeyFortressMasterSecretKey"` -> `"KunjikaMasterSecretKey"`, `"KeyFortress_Recovery_Kit.pdf"` -> `"Kunjika_Recovery_Kit.pdf"`.

## Verification Plan

### Automated Tests
- Run all unit tests to ensure imports are correct and logic is intact.
- Run `gradle build` to verify the project compiles with the new package name.

### Manual Verification
- Deploy the app to a device/emulator.
- Verify that the app launches and functions correctly.
- Check if the app name on the launcher is correct.
- Verify that the autofill service and shortcuts still work.
