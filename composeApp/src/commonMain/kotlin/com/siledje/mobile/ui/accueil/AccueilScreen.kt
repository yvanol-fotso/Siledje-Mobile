package com.siledje.mobile.ui.accueil

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
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
import com.siledje.mobile.data.repository.SalesRepository
import com.siledje.mobile.domain.model.Product
import com.siledje.mobile.domain.model.Sale
import com.siledje.mobile.ui.components.ErrorState
import com.siledje.mobile.ui.components.HeaderStat
import com.siledje.mobile.ui.components.IconBadge
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SectionTitle
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.SkeletonList
import com.siledje.mobile.ui.components.StockBar
import com.siledje.mobile.ui.components.formatFcfa
import com.siledje.mobile.ui.theme.SuccessGreen
import com.siledje.mobile.ui.ventes.SaleCard
import kotlinx.coroutines.CancellationException

/**
 * Équivalent mobile de accueil_manager.py / accueil_view.py : vue
 * d'ensemble pour le propriétaire — alertes stock bas + dernières ventes.
 * Chargement en LaunchedEffect : simple pour l'instant, à migrer
 * vers un vrai ViewModel si l'écran grossit.
 */
@Composable
fun AccueilScreen() {
    var loading by remember { mutableStateOf(true) }
    var lowStock by remember { mutableStateOf<List<Product>>(emptyList()) }
    var recentSales by remember { mutableStateOf<List<Sale>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    val catalogRepo = remember { CatalogRepository() }
    val salesRepo = remember { SalesRepository() }

    LaunchedEffect(reloadKey) {
        loading = true
        error = null
        try {
            lowStock = catalogRepo.getLowStockProducts()
            recentSales = salesRepo.getRecentSales(limit = 10)
            loading = false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "Erreur de chargement"
            loading = false
        }
    }

    val ready = !loading && error == null

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Bonjour 👋",
            subtitle = "Voici l'état de votre activité",
            onRefresh = { reloadKey++ }
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeaderStat(
                    label = "Dernières ventes",
                    value = if (ready) formatFcfa(recentSales.sumOf { it.totalAmount }) else "—",
                    modifier = Modifier.weight(1.4f)
                )
                HeaderStat(
                    label = "Stock bas",
                    value = if (ready) lowStock.size.toString() else "—",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when {
            loading -> SkeletonList()
            error != null -> ErrorState(message = error ?: "", onRetry = { reloadKey++ })
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { SectionTitle("Stock bas", lowStock.size) }
                if (lowStock.isEmpty()) {
                    item { AllGoodCard() }
                } else {
                    items(lowStock) { product -> LowStockCard(product) }
                }

                item { Spacer(Modifier.height(6.dp)) }
                item { SectionTitle("Ventes récentes") }
                if (recentSales.isEmpty()) {
                    item {
                        Text(
                            "Aucune vente pour l'instant.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(recentSales) { sale -> SaleCard(sale) }
                }
            }
        }
    }
}

@Composable
private fun AllGoodCard() {
    SiledjeCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Filled.CheckCircle, SuccessGreen)
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
private fun LowStockCard(product: Product) {
    val danger = MaterialTheme.colorScheme.error
    val outOfStock = product.stockQuantity <= 0
    SiledjeCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Filled.Warning, danger)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (outOfStock) "Rupture de stock"
                    else "Reste ${product.stockQuantity} · seuil ${product.minStockThreshold}",
                    style = MaterialTheme.typography.bodySmall,
                    color = danger
                )
                Spacer(Modifier.height(8.dp))
                StockBar(product.stockQuantity, product.minStockThreshold, danger)
            }
        }
    }
}
