package com.kunjika.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.kunjika.app.core.sensor.DeviceAngleManager

@Stable
class PrivacyAngleState {
    var isWithinPrivacyCone by mutableStateOf(true)
    var currentPitch by mutableFloatStateOf(45f)
    var currentRoll by mutableFloatStateOf(0f)
}

@Composable
fun rememberPrivacyAngleState(isActive: Boolean): PrivacyAngleState {
    val context = LocalContext.current
    val state = remember { PrivacyAngleState() }

    DisposableEffect(isActive) {
        if (!isActive) {
            state.isWithinPrivacyCone = true
            return@DisposableEffect onDispose {}
        }

        val angleManager = DeviceAngleManager(
            context = context,
            onAngleChanged = { pitch, roll, isWithinCone ->
                state.currentPitch = pitch
                state.currentRoll = roll
                state.isWithinPrivacyCone = isWithinCone
            }
        )
        angleManager.start()

        onDispose {
            angleManager.stop()
            state.isWithinPrivacyCone = true
        }
    }

    return state
}
