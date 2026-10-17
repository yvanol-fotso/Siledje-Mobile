package com.siledje.mobile.ui.navigation

sealed class Screen(val route: String, val label: String) {
    data object Accueil : Screen("accueil", "Accueil")
    data object Ventes : Screen("ventes", "Ventes")
    data object Stock : Screen("stock", "Stock")
    data object Fournisseurs : Screen("fournisseurs", "Fournisseurs")
    data object Plus : Screen("plus", "Plus")

    companion object {
        val bottomBarScreens = listOf(Accueil, Ventes, Stock, Fournisseurs, Plus)
    }
}

/** Ecrans secondaires (detail et formulaires) : la barre du bas y est masquee. */
object Routes {
    const val SALE_DETAIL = "ventes/detail"
    const val PRODUCT_DETAIL = "stock/detail"
    const val PRODUCT_FORM = "stock/form"
    const val SUPPLIER_DETAIL = "fournisseurs/detail"
    const val SUPPLIER_FORM = "fournisseurs/form"
    const val REPORTS = "plus/rapports"
    const val CLIENTS = "plus/clients"
    const val CLIENT_DETAIL = "plus/clients/detail"
    const val CATEGORIES = "plus/categories"
    const val ORDERS = "plus/commandes"
    const val MOVEMENTS = "plus/mouvements"
}
