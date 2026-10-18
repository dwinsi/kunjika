package com.kunjika.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Lightweight static background decoration. It is visual styling only and does not
 * prevent camera capture; FLAG_SECURE is the app's screenshot mitigation.
 */
@Composable
fun AntiMoireBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDark = surfaceColor.luminance() < 0.5f

    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF07090E), Color(0xFF111722), Color(0xFF07090E)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF4F6FA), Color(0xFFFFFFFF)))
    }

    Box(
        modifier = modifier.background(backgroundBrush)
    ) {
        content()
    }
}

/**
 * Anti-Camera Moiré Text Composable.
 * Applies a high-frequency repeating micro-mesh brush and subpixel optical shadow
 * directly across font character glyphs to disrupt external camera photography and OCR.
 */
@Composable
fun AntiMoireText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.Start
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val baseColor = color

    // High-frequency repeated micro-mesh gradient brush cutting 2.5px micro-lines across glyphs
    val microMeshBrush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            baseColor.copy(alpha = 0.55f),
            baseColor,
            if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
            baseColor.copy(alpha = 0.65f)
        ),
        start = Offset(0f, 0f),
        end = Offset(10f, 10f), // Ultra-dense 10px repeating period
        tileMode = TileMode.Repeated
    )

    Text(
        text = text,
        modifier = modifier,
        textAlign = textAlign,
        style = style.copy(
            brush = microMeshBrush,
            shadow = Shadow(
                color = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.5f) else Color(0xFFD97706).copy(alpha = 0.4f),
                offset = Offset(1.2f, 1.2f),
                blurRadius = 0.8f
            )
        )
    )
}
