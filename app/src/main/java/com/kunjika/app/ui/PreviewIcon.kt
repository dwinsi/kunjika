package com.keyfortress.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keyfortress.app.R

@Composable
fun AppIcon512() {
    // Recreating the background radial gradient from ic_launcher_background.xml
    val backgroundBrush = Brush.radialGradient(
        colors = listOf(Color(0xFF131B2A), Color(0xFF0B0F17)),
    )

    Box(
        modifier = Modifier
            .size(512.dp)
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(512.dp)
        )
    }
}

@Preview(widthDp = 512, heightDp = 512)
@Composable
fun PreviewAppIcon() {
    AppIcon512()
}
