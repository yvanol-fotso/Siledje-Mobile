package com.siledje.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = SiledjeAccent,
    secondary = SiledjeAccentVariant,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    outline = LightBorder,
    error = DangerRed
)

private val DarkColors = darkColorScheme(
    primary = SiledjeAccentDark,
    secondary = SiledjeAccentVariant,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    outline = DarkBorder,
    error = DangerRed
)

/**
 * @param useDarkTheme null = suit le thème système (comportement par défaut) ;
 * passe true/false pour un sélecteur manuel clair/sombre dans les réglages
 * de l'app, comme côté desktop.
 */
@Composable
fun SiledjeTheme(
    useDarkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val darkTheme = useDarkTheme ?: isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SiledjeTypography,
        content = content
    )
}
