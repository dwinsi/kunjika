# Walkthrough: Card-Scoped Stealth Dimming & Angle-Locked Privacy Louver

Implemented the **Stealth Dimming & Angle-Locked Privacy System** to protect sensitive passwords against shoulder-surfers, external cameras, status-bar brightness overrides, and flat-on-table passive viewing.

## Accomplished Implementation

### 1. Hardware Angle-Locked Privacy Louver (`DeviceAngleManager.kt` & `PrivacyAngleState.kt`)
- Created [DeviceAngleManager.kt](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/java/com/kunjika/app/core/sensor/DeviceAngleManager.kt)
  - Reads hardware `Sensor.TYPE_ROTATION_VECTOR` / `Sensor.TYPE_GRAVITY` (0 permissions).
  - Evaluates whether the device is currently held within a natural reading cone (Pitch 25°–80°, Roll ±22°).
  - If the device is laid flat on a desk, held up high, or tilted sideways towards an onlooker, passwords automatically mask to `••••••••` with a helpful hint (*"📐 Tilt phone toward you"*).
- Created [PrivacyAngleState.kt](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/java/com/kunjika/app/ui/components/PrivacyAngleState.kt)
  - Unregisters sensor listeners immediately when card is closed or masked, ensuring **0 background battery consumption**.

### 2. Card-Scoped Stealth Dimming & Focus Loss Sentinel (`CardStealthBrightnessEffect.kt`)
- Created [CardStealthBrightnessEffect.kt](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/java/com/kunjika/app/ui/components/CardStealthBrightnessEffect.kt)
  - Overrides window brightness to **8% stealth level** ONLY while the password card is revealed/unmasked.
  - Automatically restores previous system brightness as soon as the card is closed or masked.
  - Listens for window focus loss (`ViewTreeObserver.OnWindowFocusChangeListener`). If a user or attacker swipes down the notification shade to drag brightness up, the password **instantly re-masks to `••••••••`**.

### 3. Integrated into Password Detail Dialog
- Updated [PasswordDetailDialog.kt](file:///Users/ashwinsingh/PasswordGenerator/app/src/main/java/com/kunjika/app/ui/screens/vault/PasswordDetailDialog.kt)
  - Combines Stealth Dimming, Focus Loss Sentinel, and Angle-Locked Privacy Louver with `AntiMoireText`.

## Verification Results

- `./gradlew assembleDebug` compiled successfully.
- `./gradlew test` passed all 11 unit tests cleanly.
