package com.miguelangel.rickandmortyai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = PortalGreen,
    onPrimary = SpaceNavy,
    secondary = RickBlue,
    onSecondary = SurfaceLight,
    tertiary = MortyYellow,
    background = SurfaceLight,
    surface = SurfaceLight,
    onBackground = SpaceNavy,
    onSurface = SpaceNavy,
)

private val DarkScheme = darkColorScheme(
    primary = PortalGreen,
    onPrimary = SpaceNavy,
    secondary = RickBlue,
    onSecondary = SurfaceLight,
    tertiary = MortyYellow,
    background = SpaceNavy,
    surface = SurfaceDark,
    onBackground = SurfaceLight,
    onSurface = SurfaceLight,
)

@Composable
fun RickAndMortyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = AppTypography,
        content = content,
    )
}
