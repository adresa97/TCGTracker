package com.boogie_knight.tcgtracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.boogie_knight.tcgtracker.R

private val DarkColorScheme = darkColorScheme(
    surface = PocketBlack,
    onSurface = PocketWhite,
    primaryContainer = DarkGreenContainer,
    onPrimaryContainer = PocketWhite,
    secondaryContainer = DarkGreenAccent,
    onSecondaryContainer = PocketWhite,
    tertiaryContainer = PocketBlack,
    onTertiaryContainer = PocketWhite
)

private val LightColorScheme = lightColorScheme(
    surface = PocketWhite,
    onSurface = PocketBlack,
    primaryContainer = LightGreenContainer,
    onPrimaryContainer = PocketBlack,
    secondaryContainer = LightGreenAccent,
    onSecondaryContainer = PocketBlack,
    tertiaryContainer = PocketBlack,
    onTertiaryContainer = PocketWhite
)

val ptcgFontFamily = FontFamily(
    fonts = listOf(
        Font(R.font.ptcg_font)
    )
)

@Composable
fun TCGTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}