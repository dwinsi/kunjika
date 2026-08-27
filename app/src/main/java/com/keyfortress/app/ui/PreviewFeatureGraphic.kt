package com.keyfortress.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keyfortress.app.R

@Composable
fun FeatureGraphic1024x500() {
    val backgroundBrush = Brush.radialGradient(
        colors = listOf(Color(0xFF131B2A), Color(0xFF0B0F17)),
    )

    Box(
        modifier = Modifier
            .size(width = 1024.dp, height = 500.dp)
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon in the center
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(240.dp) // Large shield
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "Kunjika",
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )

            // Tagline
            Text(
                text = "MILITARY-GRADE • OFFLINE-FIRST • VAULT",
                color = Color(0xFF10B981), // Emerald 500
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        
        // Decorative background icon (Subtle)
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 100.dp)
                .graphicsLayer(alpha = 0.03f),
            tint = Color.White
        )
    }
}

@Preview(widthDp = 1024, heightDp = 500)
@Composable
fun PreviewFeatureGraphic() {
    FeatureGraphic1024x500()
}
