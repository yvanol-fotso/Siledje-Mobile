package com.siledje.mobile.ui.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siledje.mobile.data.repository.CatalogRepository
import com.siledje.mobile.domain.model.Product
import com.siledje.mobile.ui.components.EmptyState
import com.siledje.mobile.ui.components.ErrorState
import com.siledje.mobile.ui.components.FilterPill
import com.siledje.mobile.ui.components.InitialAvatar
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SearchField
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.SkeletonList
import com.siledje.mobile.ui.components.StatusChip
import com.siledje.mobile.ui.components.formatFcfa
import com.siledje.mobile.ui.theme.SuccessGreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * Équivalent mobile de stock_view.py / stock_table.py / stock_form.py.
 * L'ajout/édition de produit (stock_form.py) n'est pas encore ici —
 * dis-moi si tu veux qu'on l'ajoute (formulaire + adjustStock côté
 * CatalogRepository, déjà prêt côté repository).
 */
@Composable
fun StockScreen() {
    var loading by remember { mutableStateOf(true) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchTerm by remember { mutableStateOf("") }
    var onlyLow by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }

    val catalogRepo = remember { CatalogRepository() }

    // Un seul effet : chargement initial, recherche (avec petite attente
    // pour ne pas appeler le serveur à chaque lettre) et actualisation.
    // Effacer la recherche recharge maintenant la liste complète.
    LaunchedEffect(searchTerm, reloadKey) {
        if (searchTerm.isNotBlank()) delay(300)
        if (products.isEmpty()) loading = true
        error = null
        try {
            products = if (searchTerm.isBlank()) catalogRepo.getAllProducts()
            else catalogRepo.searchProducts(searchTerm)
            loading = false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Si on a déjà une liste, on la garde en cas d'erreur réseau.
            if (products.isEmpty()) error = e.message ?: "Erreur de chargement"
            loading = false
        }
    }

    val shown = if (onlyLow) products.filter { it.isLowStock } else products

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Stock",
            subtitle = if (loading) "Chargement…"
            else "${products.size} produit" + (if (products.size > 1) "s" else ""),
            onRefresh = { reloadKey++ }
        )

        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)) {
            SearchField(searchTerm, { searchTerm = it }, "Rechercher un produit")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPill("Tous", selected = !onlyLow, onClick = { onlyLow = false })
                FilterPill("Stock bas", selected = onlyLow, onClick = { onlyLow = true })
            }
        }

        when {
            loading -> SkeletonList()
            error != null -> ErrorState(message = error ?: "", onRetry = { reloadKey++ })
            shown.isEmpty() -> EmptyState(
                icon = if (searchTerm.isNotBlank()) Icons.Filled.SearchOff else Icons.Filled.Inventory2,
                title = if (searchTerm.isNotBlank()) "Aucun résultat" else "Aucun produit",
                message = if (searchTerm.isNotBlank()) "Essaie un autre mot-clé."
                else if (onlyLow) "Aucun produit en stock bas." else "Les produits apparaîtront ici."
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(shown) { product -> ProductCard(product) }
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product) {
    val danger = MaterialTheme.colorScheme.error
    val statusColor = if (product.isLowStock) danger else SuccessGreen
    val statusText = when {
        product.stockQuantity <= 0 -> "Rupture"
        product.isLowStock -> "Stock bas · ${product.stockQuantity}"
        else -> "En stock · ${product.stockQuantity}"
    }
    SiledjeCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            InitialAvatar(product.name, color = statusColor)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    product.categoryName ?: "Sans catégorie",
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
                StatusChip(statusText, statusColor)
            }
        }
    }
}
