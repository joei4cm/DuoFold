package com.duofold.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.duofold.launcher.fold.PanelKind

enum class GlassTone {
    Dock,
    Card,
    Sheet,
    Leading,
}

/**
 * Layered frosted glass — specular rim, depth fill, soft inner veil.
 * Simulated backdrop frost (launcher cannot blur live wallpaper cheaply).
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    radius: Dp = DuoMotion.panelRadius,
    tone: GlassTone = GlassTone.Card,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    val fill = when (tone) {
        GlassTone.Dock -> listOf(Color(0x55FFFFFF), Color(0x22FFFFFF), Color(0x14000000))
        GlassTone.Card -> listOf(Color(0x44FFFFFF), Color(0x18FFFFFF), Color(0x22081018))
        GlassTone.Sheet -> listOf(Color(0x33FFFFFF), Color(0x14101820), Color(0xEE0C141C))
        GlassTone.Leading -> listOf(Color(0x38FFFFFF), Color(0x14A8C4D4), Color(0x22061014))
    }
    val rim = when (tone) {
        GlassTone.Dock -> Color.White.copy(alpha = 0.34f)
        GlassTone.Card -> Color.White.copy(alpha = 0.22f)
        GlassTone.Sheet -> Color.White.copy(alpha = 0.14f)
        GlassTone.Leading -> Color.White.copy(alpha = 0.28f)
    }
    Box(
        modifier
            .clip(shape)
            .border(1.dp, rim, shape)
            .drawBehind {
                // Outer ambient glow
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(size.width * 0.35f, size.height * 0.12f),
                        radius = size.minDimension * 0.95f,
                    ),
                )
            }
            .background(Brush.verticalGradient(fill)),
    ) {
        // Specular highlight band
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(radius.coerceAtMost(18.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                    ),
                ),
        )
        // Bottom depth veil
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.22f)),
                    ),
                ),
        )
        content()
    }
}

@Composable
fun HomeBackdrop(
    panel: PanelKind,
    foldAmount: Float,
    modifier: Modifier = Modifier,
) {
    val open = 1f - foldAmount.coerceIn(0f, 1f)
    val base = when (panel) {
        PanelKind.Cover -> listOf(
            Color(0xFF070B10),
            Color(0xFF121C26),
            Color(0xFF243442),
            Color(0xFF4A5E6C),
        )
        PanelKind.Inner -> listOf(
            Color(0xFF06090E),
            Color(0xFF0E1822),
            Color(0xFF1A2C38),
            Color(0xFF2F4554),
            Color(0xFF6B8290),
        )
        PanelKind.Unknown -> listOf(
            Color(0xFF0B1218),
            Color(0xFF1C2A36),
            Color(0xFF3E4F5E),
            Color(0xFF7A8B97),
        )
    }
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(base))
            .drawBehind {
                // Soft window light — stronger when fully open
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF9EC4D4).copy(alpha = 0.22f * open),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.72f, size.height * 0.18f),
                        radius = size.maxDimension * 0.62f,
                    ),
                )
                // Hinge-side shade grows while folding
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.55f * foldAmount),
                        0.28f to Color.Black.copy(alpha = 0.18f * foldAmount),
                        0.55f to Color.Transparent,
                        startX = 0f,
                        endX = size.width,
                        tileMode = TileMode.Clamp,
                    ),
                )
            },
    )
}

/** Narrow frosted hinge groove between leading + home on inner display. */
@Composable
fun HingeGroove(modifier: Modifier = Modifier) {
    Box(
        modifier
            .width(10.dp)
            .fillMaxHeight()
            .padding(vertical = 18.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.12f),
                        Color.Black.copy(alpha = 0.28f),
                    ),
                ),
            )
            .border(0.5.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(8.dp)),
    )
}

@Deprecated("Use HomeBackdrop", ReplaceWith("HomeBackdrop(PanelKind.Unknown, 0f)"))
fun homeBackdropBrush() = Brush.verticalGradient(
    listOf(
        Color(0xFF0B1218),
        Color(0xFF1C2A36),
        Color(0xFF3E4F5E),
        Color(0xFF7A8B97),
    ),
)
