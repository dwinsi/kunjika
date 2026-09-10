package com.kunjika.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetTextColor = if (isSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9)
    val textColor by animateColorAsState(targetValue = targetTextColor, label = "chip_text")

    val chipShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .clip(chipShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isSelected) {
                        listOf(
                            Color(0xFFFDE68A), // Light Gold Specular Cap
                            Color(0xFFF59E0B), // Golden Primary
                            Color(0xFFB45309)  // Rich Gold Base
                        )
                    } else {
                        listOf(
                            Color(0xFF334155), // Slate 700 - Highly visible top
                            Color(0xFF1E293B), // Slate 800 - Rich mid tone
                            Color(0xFF0F172A)  // Slate 900 - Base
                        )
                    }
                )
            )
            .glossyBorder(
                shape = chipShape,
                borderWidth = if (isSelected) 1.5.dp else 1.2.dp,
                highlightColor = if (isSelected) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.55f)
            )
            .glossyTopShine(alpha = if (isSelected) 0.5f else 0.35f, shineRatio = 0.5f)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            letterSpacing = 0.3.sp
        )
    }
}
