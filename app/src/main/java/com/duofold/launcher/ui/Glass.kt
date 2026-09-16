package com.duofold.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Soft frosted panel — original DuoFold material language. */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    radius: Dp = DuoMotion.panelRadius,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Box(modifier.clip(shape)) {
        Box(
            Modifier
                .matchParentSize()
                .blur(28.dp)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.08f),
                        )
                    )
                )
        )
        Box(
            Modifier
                .matchParentSize()
                .background(Color(0x33101820))
                .border(1.dp, Color.White.copy(alpha = 0.18f), shape)
        )
        content()
    }
}

fun homeBackdropBrush() = Brush.verticalGradient(
    listOf(
        Color(0xFF0B1218),
        Color(0xFF1C2A36),
        Color(0xFF3E4F5E),
        Color(0xFF7A8B97),
    )
)
