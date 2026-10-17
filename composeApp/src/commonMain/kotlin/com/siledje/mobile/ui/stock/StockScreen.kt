package com.siledje.mobile.ui.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siledje.mobile.domain.model.Product
import com.siledje.mobile.ui.components.EmptyState
import com.siledje.mobile.ui.components.ErrorState
import com.siledje.mobile.ui.components.FilterPill
import com.siledje.mobile.ui.components.HeaderStat
import com.siledje.mobile.ui.components.InitialAvatar
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SearchField
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.SkeletonList
import com.siledje.mobile.ui.components.StatusChip
import com.siledje.mobile.ui.components.formatFcfa
import com.siledje.mobile.ui.state.CatalogStore
import com.siledje.mobile.ui.state.StockFilter
import com.siledje.mobile.ui.state.UNCATEGORIZED_LABEL
import com.siledje.mobile.ui.theme.semantic

/** Couleur et libelle d'etat d'un produit. Le rouge n'est pas utilise : rupture = neutre, stock bas = ambre. */
@Composable
fun stockStatusColor(product: Product): Color = when {
    product.stockQuantity <= 0 -> MaterialTheme.colorScheme.onSurfaceVariant
    product.isLowStock -> MaterialTheme.semantic.warning
    else -> MaterialTheme.semantic.success
}

fun stockStatusLabel(product: Product): String = when {
    product.stockQuantity <= 0 -> "Rupture"
    product.isLowStock -> "Stock bas : ${product.stockQuantity}"
    else -> "En stock : ${product.stockQuantity}"
}

/** Liste du stock avec recherche, filtres par etat et par categorie, et bouton d'ajout. */
@Composable
fun StockScreen(
    catalog: CatalogStore,
    onOpenProduct: (Product) -> Unit,
    onAddProduct: () -> Unit,
    onRefresh: () -> Unit
) {
    val ready = catalog.loaded
    val shown = catalog.filteredProducts

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "Stock",
                subtitle = when {
                    !ready && catalog.error == null -> "Chargement"
                    !ready -> "Indisponible"
                    else -> "${catalog.products.size} produit" + (if (catalog.products.size > 1) "s" else "")
                },
                onRefresh = onRefresh
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HeaderStat(
                        label = "Valeur du stock",
                        value = if (ready) formatFcfa(catalog.stockValue) else "-",
                        modifier = Modifier.weight(1.4f)
                    )
                    HeaderStat(
                        label = "Stock bas",
                        value = if (ready) catalog.lowStockProducts.size.toString() else "-",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            when {
                !ready && catalog.error != null -> ErrorState(message = catalog.error ?: "", onRetry = onRefresh)
                !ready -> SkeletonList()
                else -> {
                    Column(Modifier.padding(top = 14.dp)) {
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            SearchField(catalog.query, { catalog.query = it }, "Rechercher un produit ou un SKU")
                        }
                        Spacer(Modifier.height(10.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(StockFilter.values().toList()) { filter ->
                                FilterPill(
                                    filter.label,
                                    selected = catalog.stockFilter == filter,
                                    onClick = { catalog.stockFilter = filter }
                                )
                            }
                        }
                        if (catalog.categories.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    FilterPill(
                                        "Toutes les catégories",
                                        selected = catalog.categoryFilter == null,
                                        onClick = { catalog.categoryFilter = null }
                                    )
                                }
                                items(catalog.categories) { category ->
                                    FilterPill(
                                        "${category.name} (${catalog.productCountForCategory(category.id)})",
                                        selected = catalog.categoryFilter == category.id,
                                        onClick = { catalog.categoryFilter = category.id }
                                    )
                                }
                            }
                        }
                    }

                    when {
                        catalog.products.isEmpty() -> EmptyState(
                            icon = Icons.Filled.Inventory2,
                            title = "Aucun produit",
                            message = "Ajoutez votre premier produit avec le bouton en bas à droite."
                        )
                        shown.isEmpty() -> EmptyState(
                            icon = Icons.Filled.SearchOff,
                            title = "Aucun résultat",
                            message = "Modifiez la recherche ou les filtres."
                        )
                        else -> LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (catalog.categoryFilter == null) {
                                // Sans filtre de categorie : produits regroupes par categorie.
                                val groups = shown
                                    .groupBy { catalog.categoryLabel(it) }
                                    .entries
                                    .sortedWith(compareBy<Map.Entry<String, List<Product>>>({ it.key == UNCATEGORIZED_LABEL }, { it.key }))
                                groups.forEach { (category, list) ->
                                    item { CategoryHeader(category, list.size) }
                                    items(list) { product ->
                                        ProductCard(product, category) { onOpenProduct(product) }
                                    }
                                }
                            } else {
                                items(shown) { product ->
                                    ProductCard(product, catalog.categoryLabel(product)) { onOpenProduct(product) }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (ready) {
            ExtendedFloatingActionButton(
                onClick = onAddProduct,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Ajouter un produit") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
    }
}

@Composable
private fun ProductCard(product: Product, category: String, onClick: () -> Unit) {
    val statusColor = stockStatusColor(product)
    SiledjeCard(onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            InitialAvatar(product.name)
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
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(formatFcfa(product.sellPrice), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                StatusChip(stockStatusLabel(product), statusColor)
            }
        }
    }
}

@Composable
private fun CategoryHeader(name: String, count: Int) {
    Row(
        modifier = Modifier.padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(8.dp))
        StatusChip(count.toString(), MaterialTheme.colorScheme.primary)
    }
}
