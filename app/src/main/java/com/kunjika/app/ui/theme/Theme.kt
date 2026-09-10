package com.kunjika.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Gold500,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Gold900,
    onPrimaryContainer = Gold300,

    secondary = Indigo400,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Indigo950,
    onSecondaryContainer = Indigo300,

    tertiary = Emerald400,
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = Emerald950,
    onTertiaryContainer = Emerald300,

    background = DarkBg,
    onBackground = TextPrimaryDark,

    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,

    outline = DarkBorder,
    outlineVariant = DarkOutlineVariant,

    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),
)

private val LightColorScheme = lightColorScheme(
    primary = Gold600,
    onPrimary = Color.White,
    primaryContainer = Gold300,
    onPrimaryContainer = Gold700,

    secondary = Indigo600,
    onSecondary = Color.White,
    secondaryContainer = Indigo300,
    onSecondaryContainer = Indigo950,

    tertiary = Emerald600,
    onTertiary = Color.White,
    tertiaryContainer = Emerald300,
    onTertiaryContainer = Emerald950,

    background = LightBg,
    onBackground = TextPrimaryLight,

    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,

    outline = LightBorder,
    outlineVariant = LightOutlineVariant,

    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
)

@Composable
fun KunjikaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+. Default to false to showcase Kunjika's signature brand theme.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
