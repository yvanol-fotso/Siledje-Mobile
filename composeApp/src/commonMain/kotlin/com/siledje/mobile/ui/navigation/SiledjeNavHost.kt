package com.siledje.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.siledje.mobile.ui.accueil.AccueilScreen
import com.siledje.mobile.ui.fournisseurs.FournisseursScreen
import com.siledje.mobile.ui.stock.StockScreen
import com.siledje.mobile.ui.ventes.VentesScreen

private fun iconFor(screen: Screen) = when (screen) {
    Screen.Accueil -> Icons.Filled.Home
    Screen.Ventes -> Icons.Filled.ReceiptLong
    Screen.Stock -> Icons.Filled.Inventory2
    Screen.Fournisseurs -> Icons.Filled.LocalShipping
}

@Composable
fun SiledjeNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route

            NavigationBar {
                Screen.bottomBarScreens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(iconFor(screen), contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Accueil.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Accueil.route) { AccueilScreen() }
            composable(Screen.Ventes.route) { VentesScreen() }
            composable(Screen.Stock.route) { StockScreen() }
            composable(Screen.Fournisseurs.route) { FournisseursScreen() }
        }
    }
}
