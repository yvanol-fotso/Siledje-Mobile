package com.siledje.mobile.ui.theme

import androidx.compose.ui.graphics.Color

// Même accent que l'app desktop (light.qss / dark.qss) pour une
// identité visuelle cohérente entre les deux plateformes.
val SiledjeAccent = Color(0xFF567BA1)
val SiledjeAccentVariant = Color(0xFF7895B4) // couleur de sélection de ligne côté desktop
val SiledjeAccentDark = Color(0xFF8FB3D9)

// ── Clair ────────────────────────────────────────────────
val LightBackground = Color(0xFFF3F5F8)      // fond de l'écran (légèrement teinté)
val LightSurface = Color(0xFFFFFFFF)         // cartes : blanc, elles ressortent du fond
val LightSurfaceVariant = Color(0xFFE6EBF1)
val LightOnSurface = Color(0xFF1A1C1E)
val LightOnSurfaceVariant = Color(0xFF5B6470)
val LightBorder = Color(0xFFB0B4B9)          // gris, jamais de noir pur
val LightOutlineVariant = Color(0xFFDDE2E8)
val LightPrimaryContainer = Color(0xFFD9E6F3)
val LightOnPrimaryContainer = Color(0xFF14304B)

// ── Sombre ───────────────────────────────────────────────
val DarkBackground = Color(0xFF0F1215)
val DarkSurface = Color(0xFF1A1E23)
val DarkSurfaceVariant = Color(0xFF262B32)
val DarkOnSurface = Color(0xFFE3E2E6)
val DarkOnSurfaceVariant = Color(0xFF9EA6B0)
val DarkBorder = Color(0xFF3A3D42)
val DarkOutlineVariant = Color(0xFF2E333A)
val DarkPrimaryContainer = Color(0xFF2B4560)
val DarkOnPrimaryContainer = Color(0xFFD3E4F7)

// ── Sémantiques ──────────────────────────────────────────
val DangerRed = Color(0xFFC62828) // stock bas / erreurs
val SuccessGreen = Color(0xFF2E7D32)
val WarningAmber = Color(0xFFB26A00)

// ── Bandeau d'en-tête (dégradé) ──────────────────────────
val HeaderLightStart = Color(0xFF3E5F82)
val HeaderLightEnd = Color(0xFF5F86AE)
val HeaderDarkStart = Color(0xFF1B2A3A)
val HeaderDarkEnd = Color(0xFF2D4864)
