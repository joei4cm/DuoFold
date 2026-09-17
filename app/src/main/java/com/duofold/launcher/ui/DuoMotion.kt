package com.duofold.launcher.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.dp

object DuoMotion {
    val soft = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
    val fold = spring<Float>(dampingRatio = 0.90f, stiffness = 220f)
    val snappy = spring<Float>(dampingRatio = 0.86f, stiffness = Spring.StiffnessMedium)
    val settle = spring<Float>(dampingRatio = 0.92f, stiffness = 380f)
    val pressScale = 0.94f
    val iconRadius = 16.dp
    val panelRadius = 28.dp
    val coverIconRadius = 18.dp
}
