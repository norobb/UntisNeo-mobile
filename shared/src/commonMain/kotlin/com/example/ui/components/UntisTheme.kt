package com.example.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp

@Composable
fun UntisTheme(
    useLiquidGlass: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// Extension to conditionally apply liquid glass blur
fun Modifier.liquidGlass(enabled: Boolean): Modifier = if (enabled) {
    this.blur(radius = 16.dp)
} else {
    this
}
