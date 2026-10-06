# Tasks

- `[x]` Step 1 & 2: Hardware Sensor Angle Manager & Compose State
  - `[x]` Create `com.kunjika.app.core.sensor.DeviceAngleManager.kt` using `Sensor.TYPE_ROTATION_VECTOR`
  - `[x]` Create `com.kunjika.app.ui.components.PrivacyAngleState.kt` with sensor registration lifecycle
- `[x]` Step 3: Card Stealth Brightness & Focus Loss Sentinel
  - `[x]` Create `com.kunjika.app.ui.components.CardStealthBrightnessEffect.kt` for 8% brightness override & notification shade focus loss re-masking
- `[x]` Step 4: Integrate into `PasswordDetailDialog.kt`
  - `[x]` Attach `CardStealthBrightnessEffect` and `rememberPrivacyAngleState` in `PasswordDetailDialog.kt`
- `[x]` Build & Verification
  - `[x]` Run `./gradlew assembleDebug` and `./gradlew test` to verify build & unit tests
