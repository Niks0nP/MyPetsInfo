package com.niks0n.mypetsinfo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Mint80,
    secondary = Sage80,
    tertiary = Peach80,
)

private val LightColorScheme = lightColorScheme(
    primary = Mint40,
    secondary = Sage40,
    tertiary = Peach40,
)

@Composable
fun MyPetsInfoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
