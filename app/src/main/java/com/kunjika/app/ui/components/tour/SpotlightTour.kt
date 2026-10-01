package com.kunjika.app.ui.components.tour

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kunjika.app.ui.components.glossyBorder
import com.kunjika.app.ui.components.glossyTopShine

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
        val bounds = coordinates.boundsInWindow()
        state.registerTarget(key, bounds)
    }
}

@Composable
fun SpotlightOverlay(
    state: SpotlightState,
    modifier: Modifier = Modifier
) {
    if (!state.isTourActive || state.currentStep == null) return

    val step = state.currentStep ?: return
    val targetBoundsInWindow = state.currentTargetBounds
    val density = LocalDensity.current

    var overlayBoundsInWindow by remember { mutableStateOf<Rect?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                if (coordinates.isAttached) {
                    overlayBoundsInWindow = coordinates.boundsInWindow()
                }
            }
    ) {
        val overlayRect = overlayBoundsInWindow

        // Calculate relative cutout rect inside overlay bounds
        val relativeTargetRect = if (targetBoundsInWindow != null && overlayRect != null) {
            Rect(
                left = targetBoundsInWindow.left - overlayRect.left,
                top = targetBoundsInWindow.top - overlayRect.top,
                right = targetBoundsInWindow.right - overlayRect.left,
                bottom = targetBoundsInWindow.bottom - overlayRect.top
            )
        } else null

        // Canvas drawing dimmed overlay using Path Difference
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (relativeTargetRect != null) {
                val paddingPx = with(density) { 8.dp.toPx() }
                val cutoutRect = Rect(
                    left = relativeTargetRect.left - paddingPx,
                    top = relativeTargetRect.top - paddingPx,
                    right = relativeTargetRect.right + paddingPx,
                    bottom = relativeTargetRect.bottom + paddingPx
                )
                val cornerRadiusPx = with(density) { step.cornerRadiusDp.dp.toPx() }

                // Outer screen path
                val fullScreenPath = Path().apply {
                    addRect(Rect(0f, 0f, size.width, size.height))
                }

                // Inner target cutout path
                val cutoutPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = cutoutRect,
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                        )
                    )
                }

                // Subtract cutout from full screen
                val dimmedPath = Path().apply {
                    op(fullScreenPath, cutoutPath, PathOperation.Difference)
                }

                // Draw semi-transparent dark overlay everywhere EXCEPT the cutout
                drawPath(
                    path = dimmedPath,
                    color = Color.Black.copy(alpha = 0.65f)
                )

                // Bright Accent Outline around highlighted target
                drawRoundRect(
                    color = Color(0xFF38BDF8), // Vibrant cyan accent
                    topLeft = Offset(cutoutRect.left, cutoutRect.top),
                    size = Size(cutoutRect.width, cutoutRect.height),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = with(density) { 2.5.dp.toPx() })
                )
            } else {
                // Fallback dimmed background if target not measured yet
                drawRect(color = Color.Black.copy(alpha = 0.65f))
            }
        }

        // Flyout Card Positioned Safely Relative to Target
        if (relativeTargetRect != null && overlayRect != null) {
            val densityPx = density.density
            val overlayHeightDp = (overlayRect.height / densityPx).dp

            val targetCenterYDp = (relativeTargetRect.center.y / densityPx).dp
            val targetTopDp = (relativeTargetRect.top / densityPx).dp
            val targetBottomDp = (relativeTargetRect.bottom / densityPx).dp

            val placeAbove = targetCenterYDp > (overlayHeightDp * 0.5f)

            val cardTopDp = if (placeAbove) {
                // Place above target
                (targetTopDp - 220.dp).coerceAtLeast(16.dp)
            } else {
                // Place below target
                (targetBottomDp + 16.dp).coerceAtMost(overlayHeightDp - 220.dp)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = cardTopDp)
            ) {
                // High-Contrast Glossy Flyout Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1E293B), // High-contrast Slate 800
                                    Color(0xFF0F172A)  // Slate 900
                                )
                            )
                        )
                        .glossyBorder(
                            shape = RoundedCornerShape(22.dp),
                            borderWidth = 1.8.dp,
                            highlightColor = Color(0xFF38BDF8),
                            accentColor = Color(0xFF6366F1)
                        )
                        .glossyTopShine(alpha = 0.3f)
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
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
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7)) // Sky Blue badge
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${state.currentStepIndex + 1} of ${state.steps.size}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Step Description (High Contrast Text)
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 21.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Navigation Control Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Skip Button
                            TextButton(
                                onClick = { state.dismissTour() }
                            ) {
                                Text(
                                    text = "Skip",
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (state.currentStepIndex > 0) {
                                    OutlinedButton(
                                        onClick = { state.previousStep() },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFF475569))
                                    ) {
                                        Text(
                                            text = "Back",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Button(
                                    onClick = { state.nextStep() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF38BDF8)
                                    )
                                ) {
                                    Text(
                                        text = if (state.currentStepIndex == state.steps.size - 1) "Got It!" else "Next",
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold
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
