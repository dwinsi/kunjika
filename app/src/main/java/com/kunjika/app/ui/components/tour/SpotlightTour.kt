package com.kunjika.app.ui.components.tour

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kunjika.app.ui.components.GlossyCard
import com.kunjika.app.ui.components.glossyBorder
import kotlin.math.roundToInt

data class TourStep(
    val id: String,
    val targetKey: String,
    val title: String,
    val description: String,
    val cornerRadiusDp: Float = 16f
)

@Stable
class SpotlightState {
    var steps by mutableStateOf<List<TourStep>>(emptyList())
        private set

    var currentStepIndex by mutableIntStateOf(0)
        private set

    var isTourActive by mutableStateOf(false)
        private set

    private val targetBounds = mutableStateMapOf<String, Rect>()

    var onTourDismissed: (() -> Unit)? = null

    val currentStep: TourStep?
        get() = steps.getOrNull(currentStepIndex)

    val currentTargetBounds: Rect?
        get() = currentStep?.let { targetBounds[it.targetKey] }

    fun registerTarget(key: String, bounds: Rect) {
        targetBounds[key] = bounds
    }

    fun startTour(steps: List<TourStep>, onDismissed: (() -> Unit)? = null) {
        if (steps.isEmpty()) return
        this.steps = steps
        this.currentStepIndex = 0
        this.onTourDismissed = onDismissed
        this.isTourActive = true
    }

    fun nextStep() {
        if (currentStepIndex < steps.size - 1) {
            currentStepIndex++
        } else {
            dismissTour()
        }
    }

    fun previousStep() {
        if (currentStepIndex > 0) {
            currentStepIndex--
        }
    }

    fun dismissTour() {
        isTourActive = false
        onTourDismissed?.invoke()
    }
}

@Composable
fun rememberSpotlightState(): SpotlightState {
    return remember { SpotlightState() }
}

fun Modifier.spotlightTarget(
    key: String,
    state: SpotlightState
): Modifier = this.onGloballyPositioned { coordinates ->
    if (coordinates.isAttached) {
        val bounds = coordinates.boundsInRoot()
        state.registerTarget(key, bounds)
    }
}

@Composable
fun SpotlightOverlay(
    state: SpotlightState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isTourActive && state.currentStep != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val step = state.currentStep ?: return@AnimatedVisibility
        val bounds = state.currentTargetBounds
        val density = LocalDensity.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Dark Backdrop Canvas with Clear Cutout
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            ) {
                // Dimmed Overlay
                drawRect(
                    color = Color.Black.copy(alpha = 0.78f)
                )

                if (bounds != null) {
                    val paddingPx = with(density) { 8.dp.toPx() }
                    val cutoutRect = Rect(
                        left = bounds.left - paddingPx,
                        top = bounds.top - paddingPx,
                        right = bounds.right + paddingPx,
                        bottom = bounds.bottom + paddingPx
                    )
                    val cornerRadiusPx = with(density) { step.cornerRadiusDp.dp.toPx() }

                    // Cutout target area
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(cutoutRect.left, cutoutRect.top),
                        size = Size(cutoutRect.width, cutoutRect.height),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                        blendMode = BlendMode.Clear
                    )

                    // Accent Glowing Outline around cutout
                    drawRoundRect(
                        color = Color(0xFF3B82F6), // Accent cyan/blue
                        topLeft = Offset(cutoutRect.left, cutoutRect.top),
                        size = Size(cutoutRect.width, cutoutRect.height),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                        style = Stroke(width = with(density) { 2.dp.toPx() })
                    )
                }
            }

            // Flyout Tooltip Card Positioned Near Target
            bounds?.let { rect ->
                val densityPx = density.density
                val screenHeightPx = with(density) { 800.dp.toPx() } // fallback reference
                val targetCenterY = rect.center.y
                val placeAbove = targetCenterY > (screenHeightPx * 0.55f)

                // Position card above or below target
                val topMarginDp = if (placeAbove) {
                    val targetTopDp = (rect.top / densityPx).dp
                    (targetTopDp - 220.dp).coerceAtLeast(40.dp)
                } else {
                    val targetBottomDp = (rect.bottom / densityPx).dp
                    (targetBottomDp + 16.dp).coerceAtLeast(40.dp)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .offset(y = topMarginDp)
                ) {
                    GlossyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glossyBorder(
                                shape = RoundedCornerShape(20.dp),
                                borderWidth = 1.5.dp,
                                highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            ),
                        shape = RoundedCornerShape(20.dp),
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Step Badge & Title Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = step.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${state.currentStepIndex + 1} of ${state.steps.size}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Step Description
                            Text(
                                text = step.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { state.dismissTour() }
                                ) {
                                    Text(
                                        text = "Skip Tour",
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (state.currentStepIndex > 0) {
                                        OutlinedButton(
                                            onClick = { state.previousStep() },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(text = "Back")
                                        }
                                    }

                                    Button(
                                        onClick = { state.nextStep() },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text(
                                            text = if (state.currentStepIndex == state.steps.size - 1) "Got It!" else "Next"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
