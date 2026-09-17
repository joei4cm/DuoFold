package com.duofold.launcher.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import com.duofold.launcher.fold.PanelKind
import kotlin.math.pow

/**
 * Hinge-linked leaf wipe: perspective fold, frosted blur, and veil from the spine.
 * Original DuoFold atmosphere — not a SystemUI projection replacement.
 */
@Composable
fun FoldAtmosphere(
    foldAmount: Float,
    panel: PanelKind,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val target = if (enabled) foldAmount.coerceIn(0f, 1f) else 0f
    val amount by animateFloatAsState(target, animationSpec = DuoMotion.fold, label = "fold-atm")
    // Ease-in so the last degrees of closing get denser blur.
    val eased = amount.pow(1.35f)
    val cover = panel == PanelKind.Cover
    val maxYaw = if (cover) 16f else 28f
    val maxBlur = if (cover) 18f else 32f

    Box(modifier.fillMaxSize()) {
        HomeBackdrop(panel = panel, foldAmount = eased)
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val blurPx = eased * maxBlur
                    alpha = 1f - eased * 0.22f
                    rotationY = -eased * maxYaw
                    scaleX = 1f - eased * 0.04f
                    scaleY = 1f - eased * 0.02f
                    cameraDistance = 28f * density
                    transformOrigin = TransformOrigin(0f, 0.5f)
                    // RenderEffect blur of the home leaf while the hinge moves.
                    if (blurPx > 0.4f) {
                        renderEffect = androidx.compose.ui.graphics.BlurEffect(
                            radiusX = blurPx,
                            radiusY = blurPx,
                            edgeTreatment = TileMode.Clamp,
                        )
                    }
                }
                .drawWithContent {
                    drawContent()
                    if (eased > 0.02f) {
                        // Leaf wipe from hinge (start edge)
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0f to Color.Black.copy(alpha = 0.62f * eased),
                                0.22f to Color(0xFF1A2A34).copy(alpha = 0.35f * eased),
                                0.55f to Color.Transparent,
                                1f to Color.White.copy(alpha = 0.04f * eased),
                                startX = 0f,
                                endX = size.width,
                            ),
                        )
                        // Soft specular streak that rides the wipe
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0f to Color.Transparent,
                                (0.18f + eased * 0.2f) to Color.White.copy(alpha = 0.10f * (1f - eased)),
                                (0.28f + eased * 0.2f) to Color.Transparent,
                                startX = 0f,
                                endX = size.width,
                            ),
                        )
                    }
                },
        ) {
            content()
        }
    }
}
