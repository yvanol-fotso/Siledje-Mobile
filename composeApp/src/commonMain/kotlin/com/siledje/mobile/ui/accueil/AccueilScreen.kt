package com.siledje.mobile.ui.accueil

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siledje.mobile.domain.model.Product
import com.siledje.mobile.domain.model.Sale
import com.siledje.mobile.ui.components.ErrorState
import com.siledje.mobile.ui.components.HeaderStat
import com.siledje.mobile.ui.components.IconBadge
import com.siledje.mobile.ui.components.InitialAvatar
import com.siledje.mobile.ui.components.KpiCard
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SectionTitle
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.SkeletonList
import com.siledje.mobile.ui.components.StockBar
import com.siledje.mobile.ui.components.formatFcfa
import com.siledje.mobile.ui.state.CatalogStore
import com.siledje.mobile.ui.state.SalesStore
import com.siledje.mobile.ui.state.UserProfile
import com.siledje.mobile.ui.theme.semantic
import com.siledje.mobile.ui.ventes.SaleCard

private const val PREVIEW_COUNT = 5

/** Vue d'ensemble du proprietaire : chiffres cles, alertes de stock et dernieres ventes. */
@Composable
fun AccueilScreen(
    catalog: CatalogStore,
    sales: SalesStore,
    profile: UserProfile?,
    onOpenProfile: () -> Unit,
    onOpenProduct: (Product) -> Unit,
    onOpenSale: (Sale) -> Unit,
    onSeeLowStock: () -> Unit,
    onSeeSales: () -> Unit,
    onRefresh: () -> Unit
) {
    val semantic = MaterialTheme.semantic
    val ready = catalog.loaded && sales.loaded
    val error = catalog.error ?: sales.error
    val lowStock = catalog.lowStockProducts

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = if (profile != null) "Bonjour, ${profile.username}" else "Bonjour",
            subtitle = "Voici l'état de votre activité",
            onRefresh = onRefresh,
            actions = {
                Box(Modifier.clip(CircleShape).clickable(onClick = onOpenProfile)) {
                    InitialAvatar(profile?.username ?: "?", color = Color.White, size = 40.dp)
                }
            }
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeaderStat(
                    label = "Dernières ventes",
                    value = if (sales.loaded) formatFcfa(sales.sales.take(10).sumOf { it.totalAmount }) else "-",
                    modifier = Modifier.weight(1.4f)
                )
                HeaderStat(
                    label = "Transactions",
                    value = if (sales.loaded) sales.sales.size.toString() else "-",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when {
            !ready && error != null -> ErrorState(message = error, onRetry = onRefresh)
            !ready -> SkeletonList()
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KpiCard(
                                label = "Produits actifs",
                                value = catalog.products.size.toString(),
                                icon = Icons.Filled.Inventory2,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                label = "Stock bas",
                                value = lowStock.size.toString(),
                                icon = Icons.Filled.Warning,
                                tint = semantic.warning,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KpiCard(
                                label = "Fournisseurs",
                                value = catalog.suppliers.size.toString(),
                                icon = Icons.Filled.LocalShipping,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                label = "Valeur du stock",
                                value = formatFcfa(catalog.stockValue),
                                icon = Icons.Filled.Payments,
                                tint = semantic.success,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item { SectionTitle("À réapprovisionner", lowStock.size) }
                if (lowStock.isEmpty()) {
                    item { AllGoodCard() }
                } else {
                    lowStock.take(PREVIEW_COUNT).forEach { product ->
                        item { LowStockCard(product, catalog.categoryLabel(product)) { onOpenProduct(product) } }
                    }
                    if (lowStock.size > PREVIEW_COUNT) {
                        item {
                            TextButton(onClick = onSeeLowStock, modifier = Modifier.fillMaxWidth()) {
                                Text("Voir les ${lowStock.size} produits")
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(6.dp)) }
                item { SectionTitle("Ventes récentes") }
                if (sales.sales.isEmpty()) {
                    item {
                        Text(
                            "Aucune vente pour l'instant.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    sales.sales.take(PREVIEW_COUNT).forEach { sale ->
                        item { SaleCard(sale, sales, onClick = { onOpenSale(sale) }) }
                    }
                    item {
                        TextButton(onClick = onSeeSales, modifier = Modifier.fillMaxWidth()) {
                            Text("Voir toutes les ventes")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllGoodCard() {
    SiledjeCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Filled.CheckCircle, MaterialTheme.semantic.success)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Tout est en ordre", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Aucun produit sous son seuil d'alerte.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LowStockCard(product: Product, category: String, onClick: () -> Unit) {
    val warning = MaterialTheme.semantic.warning
    val outOfStock = product.stockQuantity <= 0
    SiledjeCard(onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Filled.Warning, warning)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (outOfStock) "Rupture de stock"
                    else "Reste ${product.stockQuantity} sur un seuil de ${product.minStockThreshold}",
                    style = MaterialTheme.typography.bodySmall,
                    color = warning
                )
                Spacer(Modifier.height(6.dp))
                StockBar(product.stockQuantity, product.minStockThreshold, warning)
            }
        }
    }
}
