package com.siledje.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = SiledjeAccent,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = SiledjeAccentVariant,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightOutlineVariant,
    error = DangerRed
)

private val DarkColors = darkColorScheme(
    primary = SiledjeAccentDark,
    onPrimary = Color(0xFF0E2236),
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = SiledjeAccentVariant,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    outlineVariant = DarkOutlineVariant,
    error = Color(0xFFEF6B6B)
)

private val SiledjeShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

/** Couleurs du bandeau dégradé en haut de chaque écran. */
data class HeaderColors(val start: Color, val end: Color)

val LocalHeaderColors = staticCompositionLocalOf {
    HeaderColors(HeaderLightStart, HeaderLightEnd)
}

/** Couleurs "metier" : succes (en stock) et avertissement (stock bas). Le rouge reste reserve aux erreurs. */
data class SemanticColors(val success: Color, val warning: Color)

val LocalSemanticColors = staticCompositionLocalOf {
    SemanticColors(SuccessGreen, WarningAmber)
}

val MaterialTheme.semantic: SemanticColors
    @Composable
    get() = LocalSemanticColors.current

/** Choix du theme dans le profil. */
enum class ThemeMode(val label: String) {
    System("Système"),
    Light("Clair"),
    Dark("Sombre")
}

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
    val header = if (darkTheme) HeaderColors(HeaderDarkStart, HeaderDarkEnd)
    else HeaderColors(HeaderLightStart, HeaderLightEnd)

    val semantic = if (darkTheme) SemanticColors(SuccessGreenDark, WarningAmberDark)
    else SemanticColors(SuccessGreen, WarningAmber)

    CompositionLocalProvider(
        LocalHeaderColors provides header,
        LocalSemanticColors provides semantic
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = SiledjeTypography,
            shapes = SiledjeShapes,
            content = content
        )
    }
}
