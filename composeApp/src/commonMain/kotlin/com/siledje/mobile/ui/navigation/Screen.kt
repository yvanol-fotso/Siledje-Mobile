package com.siledje.mobile.ui.navigation

sealed class Screen(val route: String, val label: String) {
    data object Accueil : Screen("accueil", "Accueil")
    data object Ventes : Screen("ventes", "Ventes")
    data object Stock : Screen("stock", "Stock")
    data object Fournisseurs : Screen("fournisseurs", "Fournisseurs")

    companion object {
        val bottomBarScreens = listOf(Accueil, Ventes, Stock, Fournisseurs)
    }
}
