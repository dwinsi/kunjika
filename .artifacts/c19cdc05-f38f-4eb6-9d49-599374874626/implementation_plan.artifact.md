# Manual Generation and History Saving

Currently, the Generator screen auto-generates passwords/passphrases/PINs whenever any configuration change occurs (e.g., slider movement, toggling character types). This also auto-saves every intermediate generated string to the history, which clutters the history and makes it less useful.

This plan moves the generation and history-saving logic to a manual action triggered by "Generate" or "Regenerate" buttons.

## User Review Required

> [!NOTE]
> The "Generated Output" box will now be empty when first entering a mode until the user clicks "Generate".
> Configuration changes will no longer immediately update the generated string. The user must click "Regenerate" to see the effect of their changes.

## Proposed Changes

### [Generator UI & Logic]

#### [MODIFY] [GeneratorViewModel.kt](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/java/com/kunjika/app/ui/viewmodel/GeneratorViewModel.kt)
- Remove the `generate()` call from the `init` block.
- Remove `generate()` calls from all configuration setters (`setLength`, `setPinLength`, `setWordCount`, `setSeparator`, and all toggle functions).
- Update `setMode` to clear the `generatedPassword` and `strengthResult` when switching modes, instead of auto-generating.
- Update configuration setters to update `isConfigValid` immediately so the UI can show validation errors without needing to click "Generate".

#### [MODIFY] [GeneratorScreen.kt](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/java/com/kunjika/app/ui/screens/generator/GeneratorScreen.kt)
- Update the "Generated Output" box to show a placeholder text (e.g., "Tap Generate to start") when `generatedPassword` is empty.
- Change the "New" button to:
    - "Generate" (and use a filled `Button` for prominence) when no password has been generated yet.
    - "Regenerate" when a password already exists.
- Ensure the "Copy" and "Save" buttons remain disabled until a password is generated.

## Verification Plan

### Automated Tests
- I will check if there are existing tests for `GeneratorViewModel` and update them or add new ones to verify that configuration changes do not trigger history saving.

### Manual Verification
1.  Open the Generator screen.
2.  Verify that no password is auto-generated initially.
3.  Change settings (e.g., adjust length, toggle symbols). Verify no password is generated and history is not updated.
4.  Click "Generate". Verify a password is created and added to the History tab.
5.  Switch between Password, Passphrase, and PIN modes. Verify that the output is cleared each time.
6.  Click "Regenerate" and verify a new item is added to history.
