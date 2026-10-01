# Implementation Plan: Interactive Guided Feature Tours (Spotlight Flyouts)

Add a custom, lightweight, theme-matched **Spotlight Tour** system to Kunjika that guides users through key features with highlighted target cutouts, glossy flyout cards, step indicators, and "Next / Skip" controls.

## User Review Required

> [!IMPORTANT]
> - **Default Tour Triggering**: Guided tours will run automatically once per screen on first visit (tracked via `UserPreferences`).
> - **Reset / Replay Option**: A "Replay Feature Tours" option will be added in **Settings** so users can restart the guided tour whenever they want.
> - **Theme Integration**: Spotlight tooltips will use Kunjika's signature `GlossyCard` and `glossyBorder` styling for a unified look.

## Proposed Changes

### Core Tour Architecture

#### [NEW] [SpotlightTour.kt](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/java/com/kunjika/app/ui/components/tour/SpotlightTour.kt)
- Create data models:
  - `TourStep(id: String, targetKey: String, title: String, description: String)`
- Create `SpotlightState`:
  - Maintains `currentStepIndex`, `isTourActive`, target bounding boxes (`Map<String, Rect>`), `nextStep()`, `previousStep()`, `skipTour()`.
- Create `Modifier.spotlightTarget(key: String, state: SpotlightState)`:
  - Uses `onGloballyPositioned` to measure the target's `boundsInRoot()`.
- Create `SpotlightOverlay` composable:
  - Draws full-screen dimmed background canvas with `BlendMode.Clear` cutout around active target (with padding and rounded corners).
  - Renders glossy flyout tooltip card anchored above/below target with:
    - Step badge (e.g. *"1 of 4"*), Title, and Description.
    - Navigation buttons: *"Skip"* and *"Next"* / *"Got It!"*.

---

### Data Preferences

#### [MODIFY] [UserPreferences.kt](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/java/com/kunjika/app/data/preferences/UserPreferences.kt)
- Add DataStore preferences keys:
  - `KEY_HAS_SEEN_GENERATOR_TOUR`
  - `KEY_HAS_SEEN_VAULT_TOUR`
- Expose Flows: `hasSeenGeneratorTour`, `hasSeenVaultTour`.
- Add suspend functions: `setHasSeenGeneratorTour()`, `setHasSeenVaultTour()`, `resetFeatureTours()`.

---

### Screen Integrations

#### [MODIFY] [GeneratorScreen.kt](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/java/com/kunjika/app/ui/screens/generator/GeneratorScreen.kt)
- Integrate `rememberSpotlightState()` and `SpotlightOverlay`.
- Mark key elements with `.spotlightTarget`:
  1. Mode Tabs (Passphrase / Password / PIN / TOTP).
  2. Length & Options Sliders / Chips.
  3. Strength Bar & Generated Password display.
  4. Quick Copy / Save to Vault buttons.
- Trigger tour if user hasn't completed it yet.

#### [MODIFY] [VaultScreen.kt](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/java/com/kunjika/app/ui/screens/vault/VaultScreen.kt)
- Integrate `rememberSpotlightState()` and `SpotlightOverlay`.
- Mark key elements with `.spotlightTarget`:
  1. Search Bar & Category Filters.
  2. Add Password FAB (+ button).
  3. Offline Sync / WebDrop QR scanner button.

#### [MODIFY] [SettingsViewModel.kt](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/java/com/kunjika/app/ui/viewmodel/SettingsViewModel.kt)
- Add `resetFeatureTours()` action calling `userPreferences.resetFeatureTours()`.

#### [MODIFY] [SettingsScreen.kt](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/java/com/kunjika/app/ui/screens/settings/SettingsScreen.kt)
- Add a "Replay Feature Tours" row under General / Display settings.

---

## Verification Plan

### Automated Tests
- Run Gradle unit tests to ensure `UserPreferences` and build remain healthy:
  `./gradlew test`

### Manual Verification
- Deploy app to Android device / emulator.
- Verify first launch triggers the Password Generator spotlight tour.
- Step through "Next -> Next -> Got it!" and verify tour dismissed and saved.
- Navigate to Vault Screen, verify Vault tour appears and "Skip" works properly.
- Open Settings, click "Replay Feature Tours", and verify tours re-trigger on Generator and Vault screens.
