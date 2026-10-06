package com.kunjika.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * High-Frequency Anti-Camera Moiré Security Canvas Component.
 *
 * - Ultra-Dense Lattice: Uses a tight 3.5dp spatial micro-mesh grid.
 * - Rolling Shutter Phase Defeat: Micro-animates grid phase to defeat external camera
 *   rolling shutters, HDR multi-frame stacking, and camera AI processing.
 * - Human Eye vs Camera: Smooth metallic weave to the human eye, severe aliasing
 *   distortion & wave bands on camera sensors.
 */
@Composable
fun AntiMoireBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDark = surfaceColor.luminance() < 0.5f

    // Micro-phase animation to disrupt rolling shutter camera sensors
    val infiniteTransition = rememberInfiniteTransition(label = "moire_phase")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_shift"
    )

    // Higher contrast, ultra-dense micro-mesh colors
    val meshColor1 = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.12f) else Color(0xFFD97706).copy(alpha = 0.14f)
    val meshColor2 = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.12f)
    val dotColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.10f) else Color(0xFF0284C7).copy(alpha = 0.12f)

    Box(
        modifier = modifier.background(surfaceColor)
    ) {
        // High-Frequency Optical Interference Canvas
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val lineStepPx = 3.5.dp.toPx() // Ultra-dense 3.5dp pitch
            val dotStepPx = 7.dp.toPx()
            val phasePx = phaseShift.dp.toPx()

            // 1. 45° High-Frequency Diagonal Lines
            var x = -height - phasePx
            while (x < width + height + phasePx) {
                drawLine(
                    color = meshColor1,
                    start = Offset(x, 0f),
                    end = Offset(x + height, height),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                x += lineStepPx
            }

            // 2. 135° High-Frequency Anti-Diagonal Lines
            var y = -width - phasePx
            while (y < height + width + phasePx) {
                drawLine(
                    color = meshColor2,
                    start = Offset(width, y),
                    end = Offset(0f, y + width),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                y += lineStepPx
            }

            // 3. Dense Intersecting Micro-Dot Matrix
            var dotX = phasePx % dotStepPx
            while (dotX < width) {
                var dotY = 0f
                while (dotY < height) {
                    drawCircle(
                        color = dotColor,
                        radius = 1.1.dp.toPx(),
                        center = Offset(dotX, dotY)
                    )
                    dotY += dotStepPx
                }
                dotX += dotStepPx
            }
        }

        // Render child content on top of Anti-Moiré background
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
