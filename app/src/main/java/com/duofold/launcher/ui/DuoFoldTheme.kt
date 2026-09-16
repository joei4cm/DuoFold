package com.duofold.launcher.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Ink = Color(0xFF102028)
private val Sand = Color(0xFFD1BE98)
private val Sea = Color(0xFF30596D)
private val Mist = Color(0xFF9BC5D7)

@Composable
fun DuoFoldTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) darkColorScheme(
            primary = Mist,
            onPrimary = Color(0xFF12303D),
            surface = Color(0xFF17272E),
            onSurface = Color(0xFFE8EEF0),
            secondary = Sand,
        ) else lightColorScheme(
            primary = Sea,
            onPrimary = Color.White,
            surface = Color(0xFFF4F7F8),
            onSurface = Ink,
            secondary = Sand,
        ),
        content = content,
    )
}
