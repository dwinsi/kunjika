package com.kunjika.app.ui.components

import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.kunjika.app.core.security.findActivity

/**
 * Card Stealth Brightness Effect.
 *
 * - Dims screen window brightness to 8% ONLY while the password card is unmasked.
 * - Restores normal system brightness as soon as the card is closed or masked.
 * - Listens for Window Focus Loss (e.g. status bar / notification shade pulled down to drag brightness up)
 *   and triggers `onFocusLost()` to instantly re-mask the password!
 */
@Composable
fun CardStealthBrightnessEffect(
    isRevealed: Boolean,
    onFocusLost: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    DisposableEffect(isRevealed) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val window = activity.window
        val layoutParams = window.attributes
        val previousBrightness = layoutParams.screenBrightness

        if (isRevealed) {
            layoutParams.screenBrightness = 0.08f // 8% stealth brightness
            window.attributes = layoutParams
        }

        // ViewTreeObserver Window Focus listener
        val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (!hasFocus && isRevealed) {
                onFocusLost() // Instantly re-mask password if notification shade pulled!
            }
        }
        view.viewTreeObserver.addOnWindowFocusChangeListener(focusListener)

        onDispose {
            view.viewTreeObserver.removeOnWindowFocusChangeListener(focusListener)
            layoutParams.screenBrightness = previousBrightness // Restore previous system brightness
            window.attributes = layoutParams
        }
    }
}
