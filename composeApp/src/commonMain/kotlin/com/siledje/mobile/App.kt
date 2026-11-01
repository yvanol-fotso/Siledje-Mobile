package com.siledje.mobile

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.siledje.mobile.core.ShopConfig
import com.siledje.mobile.core.ShopConfigStore
import com.siledje.mobile.data.remote.SupabaseClientProvider
import com.siledje.mobile.data.repository.AuthRepository
import com.siledje.mobile.ui.auth.AuthScreen
import com.siledje.mobile.ui.auth.SplashScreen
import com.siledje.mobile.ui.config.ConfigScreen
import com.siledje.mobile.ui.navigation.SiledjeNavHost
import com.siledje.mobile.ui.state.AuthController
import com.siledje.mobile.ui.state.AuthPhase
import com.siledje.mobile.ui.theme.SiledjeTheme
import com.siledje.mobile.ui.theme.ThemeMode

/**
 * Point d'entree commun, identique sur Android et iOS.
 *
 * 1. Configuration de la boutique (code boutique, une seule fois par appareil),
 * 2. verification de session, puis connexion / inscription,
 * 3. application.
 *
 * L'APK est le meme pour tous les clients : rien n'est fige a la compilation.
 */
@Composable
fun App() {
    var themeMode by remember { mutableStateOf(ThemeMode.System) }
    var config by remember { mutableStateOf(ShopConfigStore.load()) }

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
            val current = config
            if (current == null) {
                ConfigScreen(onConfigured = { newConfig ->
                    ShopConfigStore.save(newConfig)
                    config = newConfig
                })
            } else {
                // key : un changement de boutique recree tout l'etat (session, controleur, ecrans)
                key(current) {
                    ConfiguredApp(
                        config = current,
                        themeMode = themeMode,
                        onThemeChange = { themeMode = it },
                        onChangeShop = {
                            ShopConfigStore.clear()
                            config = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfiguredApp(
    config: ShopConfig,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onChangeShop: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val auth = remember(config) {
        // Le client Supabase doit exister avant tout repository.
        SupabaseClientProvider.configure(config.url, config.anonKey)
        AuthController(AuthRepository(), scope)
    }

    Crossfade(
        targetState = auth.phase,
        animationSpec = tween(300),
        label = "authPhase"
    ) { phase ->
        when (phase) {
            AuthPhase.Loading -> SplashScreen()
            AuthPhase.SignedOut -> AuthScreen(
                auth = auth,
                shopName = config.name,
                onChangeShop = onChangeShop
            )
            AuthPhase.SignedIn -> SiledjeNavHost(
                profile = auth.profile,
                themeMode = themeMode,
                onThemeChange = onThemeChange,
                onLogout = { auth.signOut() }
            )
        }
    }
}
