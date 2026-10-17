package com.siledje.mobile.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.siledje.mobile.data.repository.CatalogRepository
import com.siledje.mobile.data.repository.SalesRepository
import com.siledje.mobile.data.repository.SupplierOrderRepository
import com.siledje.mobile.domain.model.Client
import com.siledje.mobile.domain.model.StockMovement
import com.siledje.mobile.domain.model.SupplierOrder
import com.siledje.mobile.ui.accueil.AccueilScreen
import com.siledje.mobile.ui.categories.CategoriesScreen
import com.siledje.mobile.ui.clients.ClientDetailScreen
import com.siledje.mobile.ui.clients.ClientsScreen
import com.siledje.mobile.ui.components.ToastHost
import com.siledje.mobile.ui.components.ToastState
import com.siledje.mobile.ui.fournisseurs.FournisseursScreen
import com.siledje.mobile.ui.fournisseurs.OrdersScreen
import com.siledje.mobile.ui.fournisseurs.SupplierDetailScreen
import com.siledje.mobile.ui.fournisseurs.SupplierFormScreen
import com.siledje.mobile.ui.plus.PlusScreen
import com.siledje.mobile.ui.profile.ProfileDialog
import com.siledje.mobile.ui.rapports.RapportsScreen
import com.siledje.mobile.ui.state.CatalogStore
import com.siledje.mobile.ui.state.RemoteList
import com.siledje.mobile.ui.state.SalesStore
import com.siledje.mobile.ui.state.StockFilter
import com.siledje.mobile.ui.state.SupplierStore
import com.siledje.mobile.ui.state.UserProfile
import com.siledje.mobile.ui.stock.MovementsScreen
import com.siledje.mobile.ui.stock.ProductDetailScreen
import com.siledje.mobile.ui.stock.ProductFormScreen
import com.siledje.mobile.ui.stock.StockScreen
import com.siledje.mobile.ui.theme.ThemeMode
import com.siledje.mobile.ui.theme.semantic
import com.siledje.mobile.ui.ventes.SaleDetailScreen
import com.siledje.mobile.ui.ventes.VentesScreen

private fun iconFor(screen: Screen, selected: Boolean) = when (screen) {
    Screen.Accueil -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
    Screen.Ventes -> if (selected) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong
    Screen.Stock -> if (selected) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2
    Screen.Fournisseurs -> if (selected) Icons.Filled.LocalShipping else Icons.Outlined.LocalShipping
    Screen.Plus -> if (selected) Icons.Filled.Apps else Icons.Outlined.Apps
}

/** Application connectee : etats partages, navigation a 5 onglets, toasts et profil. */
@Composable
fun SiledjeNavHost(
    profile: UserProfile?,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val toast = remember { ToastState(scope) }
    val currentProfile = rememberUpdatedState(profile)

    val catalogRepo = remember { CatalogRepository() }
    val salesRepo = remember { SalesRepository() }
    val orderRepo = remember { SupplierOrderRepository() }

    val catalog = remember { CatalogStore(catalogRepo, scope, toast) { currentProfile.value?.userId } }
    val suppliers = remember { SupplierStore(catalog, catalogRepo, orderRepo, scope, toast) }
    val sales = remember { SalesStore(salesRepo, scope, toast) }
    val clients = remember { RemoteList<Client>(scope, toast) { salesRepo.getAllClients() } }
    val orders = remember { RemoteList<SupplierOrder>(scope, toast) { orderRepo.getAllOrders() } }
    val movements = remember {
        RemoteList<StockMovement>(scope, toast) { catalogRepo.getStockMovements(null, 200) }
    }

    var selectedClient by remember { mutableStateOf<Client?>(null) }
    var showProfile by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        catalog.refresh()
        sales.refresh()
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == null || Screen.bottomBarScreens.any { it.route == currentRoute }
    val lowStockCount = catalog.products.count { it.isLowStock }

    val goTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val back: () -> Unit = { navController.popBackStack() }
    val openSale: (com.siledje.mobile.domain.model.Sale) -> Unit = { sale ->
        sales.open(sale)
        navController.navigate(Routes.SALE_DETAIL)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Les ecrans gerent eux-memes la barre d'etat (ScreenHeader) ; la barre du bas gere son inset.
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(100))
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Screen.bottomBarScreens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { goTab(screen.route) },
                            icon = {
                                val icon = iconFor(screen, selected)
                                if (screen == Screen.Stock && lowStockCount > 0) {
                                    // Pastille ambre : nombre de produits a reapprovisionner.
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = MaterialTheme.semantic.warning,
                                                contentColor = Color.White
                                            ) {
                                                Text(if (lowStockCount > 99) "99+" else lowStockCount.toString())
                                            }
                                        }
                                    ) {
                                        Icon(icon, contentDescription = screen.label)
                                    }
                                } else {
                                    Icon(icon, contentDescription = screen.label)
                                }
                            },
                            label = {
                                Text(screen.label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Accueil.route,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { fadeIn(tween(220)) },
                exitTransition = { fadeOut(tween(120)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = { fadeOut(tween(120)) }
            ) {
                composable(Screen.Accueil.route) {
                    AccueilScreen(
                        catalog = catalog,
                        sales = sales,
                        profile = profile,
                        onOpenProfile = { showProfile = true },
                        onOpenProduct = { product ->
                            catalog.openProduct(product)
                            navController.navigate(Routes.PRODUCT_DETAIL)
                        },
                        onOpenSale = openSale,
                        onSeeLowStock = {
                            catalog.stockFilter = StockFilter.LOW
                            catalog.query = ""
                            catalog.categoryFilter = null
                            goTab(Screen.Stock.route)
                        },
                        onSeeSales = { goTab(Screen.Ventes.route) },
                        onRefresh = {
                            catalog.refresh()
                            sales.refresh()
                        }
                    )
                }

                composable(Screen.Ventes.route) {
                    VentesScreen(store = sales, onOpenSale = openSale, onRefresh = { sales.refresh() })
                }

                composable(Screen.Stock.route) {
                    StockScreen(
                        catalog = catalog,
                        onOpenProduct = { product ->
                            catalog.openProduct(product)
                            navController.navigate(Routes.PRODUCT_DETAIL)
                        },
                        onAddProduct = {
                            catalog.formProduct = null
                            navController.navigate(Routes.PRODUCT_FORM)
                        },
                        onRefresh = { catalog.refresh() }
                    )
                }

                composable(Screen.Fournisseurs.route) {
                    FournisseursScreen(
                        catalog = catalog,
                        store = suppliers,
                        onOpenSupplier = { supplier ->
                            suppliers.open(supplier)
                            navController.navigate(Routes.SUPPLIER_DETAIL)
                        },
                        onAddSupplier = {
                            suppliers.formSupplier = null
                            navController.navigate(Routes.SUPPLIER_FORM)
                        },
                        onRefresh = { catalog.refresh() }
                    )
                }

                composable(Screen.Plus.route) {
                    PlusScreen(
                        profile = profile,
                        themeMode = themeMode,
                        onThemeChange = onThemeChange,
                        onNavigate = { route -> navController.navigate(route) },
                        onLogout = onLogout
                    )
                }

                composable(Routes.SALE_DETAIL) {
                    SaleDetailScreen(store = sales, catalog = catalog, onBack = back)
                }

                composable(Routes.PRODUCT_DETAIL) {
                    ProductDetailScreen(
                        catalog = catalog,
                        onBack = back,
                        onEdit = { product ->
                            catalog.formProduct = product
                            navController.navigate(Routes.PRODUCT_FORM)
                        }
                    )
                }

                composable(Routes.PRODUCT_FORM) {
                    ProductFormScreen(catalog = catalog, onClose = back)
                }

                composable(Routes.SUPPLIER_DETAIL) {
                    SupplierDetailScreen(
                        catalog = catalog,
                        store = suppliers,
                        onBack = back,
                        onEdit = { supplier ->
                            suppliers.formSupplier = supplier
                            navController.navigate(Routes.SUPPLIER_FORM)
                        }
                    )
                }

                composable(Routes.SUPPLIER_FORM) {
                    SupplierFormScreen(store = suppliers, onClose = back)
                }

                composable(Routes.REPORTS) {
                    RapportsScreen(
                        catalog = catalog,
                        sales = sales,
                        onBack = back,
                        onRefresh = {
                            catalog.refresh()
                            sales.refresh()
                        }
                    )
                }

                composable(Routes.CLIENTS) {
                    ClientsScreen(
                        clients = clients,
                        onOpenClient = { client ->
                            selectedClient = client
                            navController.navigate(Routes.CLIENT_DETAIL)
                        },
                        onBack = back
                    )
                }

                composable(Routes.CLIENT_DETAIL) {
                    ClientDetailScreen(
                        client = selectedClient,
                        sales = sales,
                        onOpenSale = openSale,
                        onBack = back
                    )
                }

                composable(Routes.CATEGORIES) {
                    CategoriesScreen(
                        catalog = catalog,
                        onOpenCategory = { category ->
                            // Ouvre le stock deja filtre sur cette categorie.
                            catalog.query = ""
                            catalog.stockFilter = StockFilter.ALL
                            catalog.categoryFilter = category.id
                            goTab(Screen.Stock.route)
                        },
                        onBack = back,
                        onRefresh = { catalog.refresh() }
                    )
                }

                composable(Routes.ORDERS) {
                    OrdersScreen(orders = orders, catalog = catalog, onBack = back)
                }

                composable(Routes.MOVEMENTS) {
                    MovementsScreen(movements = movements, catalog = catalog, onBack = back)
                }
            }

            ToastHost(
                state = toast,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
            )
        }
    }

    if (showProfile) {
        ProfileDialog(
            profile = profile,
            themeMode = themeMode,
            onThemeChange = onThemeChange,
            onLogout = {
                showProfile = false
                onLogout()
            },
            onDismiss = { showProfile = false }
        )
    }
}
