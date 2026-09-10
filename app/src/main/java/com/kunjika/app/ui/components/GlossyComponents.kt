package com.kunjika.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Custom Modifier to draw a distinct specular top-half glass reflection lens overlay.
 */
fun Modifier.glossyTopShine(
    alpha: Float = 0.35f,
    shineRatio: Float = 0.45f
): Modifier = this.drawWithContent {
    drawContent()
    // Specular light lens overlay on top portion
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha),
                Color.White.copy(alpha = alpha * 0.3f),
                Color.Transparent
            ),
            startY = 0f,
            endY = size.height * shineRatio
        ),
        size = Size(size.width, size.height * shineRatio)
    )
    // Subtle horizontal specular edge beam
    drawLine(
        color = Color.White.copy(alpha = alpha * 0.8f),
        start = Offset(0f, 1f),
        end = Offset(size.width, 1f),
        strokeWidth = 1.5f
    )
}

/**
 * Modifier for a distinct 1.5dp - 2dp glass/metallic border with top-left highlight.
 */
fun Modifier.glossyBorder(
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.5.dp,
    highlightColor: Color = Color.White.copy(alpha = 0.65f),
    accentColor: Color = Color(0xFFF59E0B).copy(alpha = 0.4f),
    darkEdgeColor: Color = Color.Black.copy(alpha = 0.15f)
): Modifier = this.border(
    width = borderWidth,
    brush = Brush.linearGradient(
        colors = listOf(
            highlightColor,
            Color.White.copy(alpha = 0.25f),
            accentColor,
            darkEdgeColor
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    ),
    shape = shape
)

/**
 * Ultra-Glossy Surface container Modifier.
 */
fun Modifier.glossySurface(
    shape: Shape = RoundedCornerShape(18.dp),
    topHighlight: Color = Color.White.copy(alpha = 0.30f),
    mainColor: Color = Color(0xFF131A26),
    bottomShadow: Color = Color(0xFF090D14),
    borderColor: Color = Color.White.copy(alpha = 0.5f)
): Modifier = this
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                topHighlight,
                mainColor,
                bottomShadow
            )
        )
    )
    .glossyBorder(shape = shape, highlightColor = borderColor)

/**
 * A Glossy Card Container component with glass top shine overlay and metallic border.
 */
@Composable
fun GlossyCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = Color.White.copy(alpha = 0.5f),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var cardModifier = modifier
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.25f),
                    containerColor,
                    Color.Black.copy(alpha = 0.08f)
                )
            )
        )
        .glossyBorder(shape = shape, highlightColor = borderColor)
        .glossyTopShine(alpha = 0.25f)

    if (onClick != null) {
        cardModifier = cardModifier.clickable(onClick = onClick)
    }

    Box(
        modifier = cardModifier.padding(2.dp),
        content = content
    )
}

/**
 * An ultra-shiny Glossy Button with gradient lens fill and specular highlight.
 */
@Composable
fun GlossyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(14.dp),
    gradientColors: List<Color> = listOf(
        Color(0xFFFDE68A),
        Color(0xFFF59E0B),
        Color(0xFFB45309)
    ),
    contentPaddingHorizontal: Dp = 10.dp,
    contentPaddingVertical: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    val buttonShape = shape
    val disabledBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    
    Box(
        modifier = modifier
            .clip(buttonShape)
            .background(
                brush = if (enabled) {
                    Brush.verticalGradient(colors = gradientColors)
                } else {
                    Brush.verticalGradient(colors = listOf(disabledBg, disabledBg))
                }
            )
            .glossyBorder(
                shape = buttonShape,
                highlightColor = if (enabled) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.3f)
            )
            .glossyTopShine(alpha = if (enabled) 0.45f else 0.15f, shineRatio = 0.5f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = contentPaddingHorizontal, vertical = contentPaddingVertical),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * An ultra-premium Glossy Icon Container with radial sheen highlight and specular glass border.
 */
@Composable
fun GlossyIconBox(
    icon: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    iconSize: Dp = 26.dp,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: Shape = CircleShape
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.5f),
                        containerColor.copy(alpha = 0.35f),
                        containerColor.copy(alpha = 0.15f)
                    )
                )
            )
            .glossyBorder(shape = shape, borderWidth = 1.5.dp, highlightColor = Color.White.copy(alpha = 0.75f))
            .glossyTopShine(alpha = 0.35f, shineRatio = 0.5f),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * A glossy glass badge pill component.
 */
@Composable
fun GlossyBadge(
    text: String,
    modifier: Modifier = Modifier,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    icon: ImageVector? = null,
    shape: Shape = RoundedCornerShape(10.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        badgeColor,
                        badgeColor
                    )
                )
            )
            .glossyBorder(shape = shape, borderWidth = 1.2.dp, highlightColor = Color.White.copy(alpha = 0.75f))
            .glossyTopShine(alpha = 0.35f)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}
