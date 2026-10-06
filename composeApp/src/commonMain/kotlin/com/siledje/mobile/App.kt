package com.siledje.mobile

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.siledje.mobile.ui.navigation.SiledjeNavHost
import com.siledje.mobile.ui.theme.SiledjeTheme

/**
 * Point d'entrée commun — identique sur Android et iOS.
 * `useDarkTheme` sera branché plus tard sur un réglage utilisateur
 * persisté (DataStore côté commun), même logique que le sélecteur
 * de thème clair/sombre de l'app desktop.
 */
@Composable
fun App() {
    val useDarkTheme = remember { mutableStateOf<Boolean?>(null) } // null = suit le système

    SiledjeTheme(useDarkTheme = useDarkTheme.value) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SiledjeNavHost()
        }
    }
}
