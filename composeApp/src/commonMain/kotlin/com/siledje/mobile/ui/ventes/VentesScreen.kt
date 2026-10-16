package com.siledje.mobile.ui.ventes

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
import androidx.compose.material.icons.filled.ReceiptLong
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
import com.siledje.mobile.data.repository.SalesRepository
import com.siledje.mobile.domain.model.Sale
import com.siledje.mobile.ui.components.EmptyState
import com.siledje.mobile.ui.components.ErrorState
import com.siledje.mobile.ui.components.HeaderStat
import com.siledje.mobile.ui.components.IconBadge
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.SkeletonList
import com.siledje.mobile.ui.components.StatusChip
import com.siledje.mobile.ui.components.formatFcfa
import com.siledje.mobile.ui.components.formatSaleDate
import com.siledje.mobile.ui.theme.SuccessGreen
import kotlinx.coroutines.CancellationException

/**
 * Équivalent mobile de sales_view.py / sales_table.py : historique des
 * ventes, en lecture (l'encaissement reste sur la caisse desktop pour
 * l'instant — voir SalesRepository.createSale si tu veux l'ajouter ici).
 */
@Composable
fun VentesScreen() {
    var loading by remember { mutableStateOf(true) }
    var sales by remember { mutableStateOf<List<Sale>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    val salesRepo = remember { SalesRepository() }

    LaunchedEffect(reloadKey) {
        loading = true
        error = null
        try {
            sales = salesRepo.getRecentSales(limit = 100)
            loading = false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "Erreur de chargement"
            loading = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Ventes",
            subtitle = when {
                loading -> "Chargement…"
                error != null -> "Indisponible"
                else -> "${sales.size} transaction" + (if (sales.size > 1) "s" else "")
            },
            onRefresh = { reloadKey++ }
        ) {
            HeaderStat(
                label = "Total affiché",
                value = if (loading || error != null) "—" else formatFcfa(sales.sumOf { it.totalAmount }),
                modifier = Modifier.fillMaxWidth()
            )
        }

        when {
            loading -> SkeletonList()
            error != null -> ErrorState(message = error ?: "", onRetry = { reloadKey++ })
            sales.isEmpty() -> EmptyState(
                icon = Icons.Filled.ReceiptLong,
                title = "Aucune vente",
                message = "Les ventes enregistrées apparaîtront ici."
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sales) { sale -> SaleCard(sale) }
            }
        }
    }
}

/** Carte d'une vente — partagée avec l'écran Accueil. */
@Composable
fun SaleCard(sale: Sale) {
    SiledjeCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Filled.ReceiptLong, SuccessGreen)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    sale.invoiceNumber.ifBlank { "Vente" },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    sale.clientName ?: "Client anonyme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    formatSaleDate(sale.saleDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatFcfa(sale.totalAmount),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                StatusChip(sale.paymentMethodName ?: "—", MaterialTheme.colorScheme.primary)
                if (sale.status != "completed") {
                    Spacer(Modifier.height(4.dp))
                    StatusChip(sale.status.replaceFirstChar { it.uppercase() }, MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
