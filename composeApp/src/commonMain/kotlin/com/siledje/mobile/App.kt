package com.siledje.mobile

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.siledje.mobile.data.repository.AuthRepository
import com.siledje.mobile.ui.auth.AuthScreen
import com.siledje.mobile.ui.auth.SplashScreen
import com.siledje.mobile.ui.navigation.SiledjeNavHost
import com.siledje.mobile.ui.state.AuthController
import com.siledje.mobile.ui.state.AuthPhase
import com.siledje.mobile.ui.theme.SiledjeTheme
import com.siledje.mobile.ui.theme.ThemeMode

/**
 * Point d'entree commun, identique sur Android et iOS.
 * Verification de session, puis connexion / inscription, puis application.
 */
@Composable
fun App() {
    var themeMode by remember { mutableStateOf(ThemeMode.System) }
    val scope = rememberCoroutineScope()
    val auth = remember { AuthController(AuthRepository(), scope) }

    val useDarkTheme: Boolean? = when (themeMode) {
        ThemeMode.System -> null
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    SiledjeTheme(useDarkTheme = useDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Crossfade(
                targetState = auth.phase,
                animationSpec = tween(300),
                label = "authPhase"
            ) { phase ->
                when (phase) {
                    AuthPhase.Loading -> SplashScreen()
                    AuthPhase.SignedOut -> AuthScreen(auth)
                    AuthPhase.SignedIn -> SiledjeNavHost(
                        profile = auth.profile,
                        themeMode = themeMode,
                        onThemeChange = { themeMode = it },
                        onLogout = { auth.signOut() }
                    )
                }
            }
        }
    }
}
