package com.kunjika.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.kunjika.app.core.generator.PasswordStrength
import com.kunjika.app.core.generator.StrengthResult
import com.kunjika.app.ui.theme.StrengthFair
import com.kunjika.app.ui.theme.StrengthStrong
import com.kunjika.app.ui.theme.StrengthVeryStrong
import com.kunjika.app.ui.theme.StrengthVeryWeak
import com.kunjika.app.ui.theme.StrengthWeak

@Composable
fun StrengthIndicator(
    strengthResult: StrengthResult,
    modifier: Modifier = Modifier
) {
    val progress = when (strengthResult.score) {
        PasswordStrength.VERY_WEAK -> 0.15f
        PasswordStrength.WEAK -> 0.35f
        PasswordStrength.FAIR -> 0.60f
        PasswordStrength.STRONG -> 0.85f
        PasswordStrength.VERY_STRONG -> 1.0f
    }

    val targetColor = when (strengthResult.score) {
        PasswordStrength.VERY_WEAK -> StrengthVeryWeak
        PasswordStrength.WEAK -> StrengthWeak
        PasswordStrength.FAIR -> StrengthFair
        PasswordStrength.STRONG -> StrengthStrong
        PasswordStrength.VERY_STRONG -> StrengthVeryStrong
    }

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
    val animatedColor by animateColorAsState(targetValue = targetColor, label = "color")

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlossyBadge(
                text = strengthResult.score.label,
                badgeColor = animatedColor.copy(alpha = 0.25f),
                textColor = animatedColor,
                shape = RoundedCornerShape(8.dp)
            )

            Text(
                text = "${strengthResult.entropyBits} bits entropy",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Glossy Progress bar
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = animatedColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
