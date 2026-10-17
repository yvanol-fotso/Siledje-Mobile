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
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siledje.mobile.domain.model.Sale
import com.siledje.mobile.ui.components.EmptyState
import com.siledje.mobile.ui.components.ErrorState
import com.siledje.mobile.ui.components.HeaderStat
import com.siledje.mobile.ui.components.IconBadge
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SearchField
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.SkeletonList
import com.siledje.mobile.ui.components.StatusChip
import com.siledje.mobile.ui.components.formatDay
import com.siledje.mobile.ui.components.formatFcfa
import com.siledje.mobile.ui.components.formatSaleDate
import com.siledje.mobile.ui.state.SalesStore
import com.siledje.mobile.ui.theme.semantic

/** Libelle francais d'un statut de vente. */
fun saleStatusLabel(status: String): String = when (status.lowercase()) {
    "completed" -> "Terminée"
    "cancelled", "canceled" -> "Annulée"
    "refunded" -> "Remboursée"
    "pending" -> "En attente"
    else -> status.replaceFirstChar { it.uppercase() }
}

/** Historique des ventes, groupe par jour. Toucher une vente ouvre le detail de la facture. */
@Composable
fun VentesScreen(store: SalesStore, onOpenSale: (Sale) -> Unit, onRefresh: () -> Unit) {
    val ready = store.loaded
    val shown = store.filteredSales

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Ventes",
            subtitle = when {
                !ready && store.error == null -> "Chargement"
                !ready -> "Indisponible"
                else -> "${store.sales.size} transaction" + (if (store.sales.size > 1) "s" else "")
            },
            onRefresh = onRefresh
        ) {
            HeaderStat(
                label = "Total des ventes affichées",
                value = if (ready) formatFcfa(store.sales.sumOf { it.totalAmount }) else "-",
                modifier = Modifier.fillMaxWidth()
            )
        }

        when {
            !ready && store.error != null -> ErrorState(message = store.error ?: "", onRetry = onRefresh)
            !ready -> SkeletonList()
            store.sales.isEmpty() -> EmptyState(
                icon = Icons.Filled.ReceiptLong,
                title = "Aucune vente",
                message = "Les ventes enregistrées apparaîtront ici."
            )
            else -> {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)) {
                    SearchField(store.query, { store.query = it }, "Rechercher une facture ou un client")
                }
                if (shown.isEmpty()) {
                    EmptyState(
                        icon = Icons.Filled.SearchOff,
                        title = "Aucun résultat",
                        message = "Essayez un autre numéro de facture ou un autre client."
                    )
                } else {
                    val groups = shown.groupBy { (it.saleDate ?: "").take(10) }
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        groups.forEach { (day, list) ->
                            item {
                                DayHeader(
                                    day = day,
                                    count = list.size,
                                    total = list.sumOf { it.totalAmount }
                                )
                            }
                            items(list) { sale -> SaleCard(sale, store, onClick = { onOpenSale(sale) }) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(day: String, count: Int, total: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (day.isBlank()) "Date inconnue" else formatDay(day),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            "$count vente" + (if (count > 1) "s" else "") + ", " + formatFcfa(total),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Carte d'une vente, partagee avec l'accueil. */
@Composable
fun SaleCard(sale: Sale, store: SalesStore, onClick: () -> Unit) {
    val semantic = MaterialTheme.semantic
    SiledjeCard(onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Filled.ReceiptLong, semantic.success)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    sale.invoiceNumber.ifBlank { "Vente" },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    store.clientLabel(sale),
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
                Text(formatFcfa(sale.totalAmount), style = MaterialTheme.typography.titleSmall)
                store.paymentLabel(sale)?.let {
                    Spacer(Modifier.height(4.dp))
                    StatusChip(it, MaterialTheme.colorScheme.primary)
                }
                if (sale.status.lowercase() != "completed") {
                    Spacer(Modifier.height(4.dp))
                    StatusChip(saleStatusLabel(sale.status), semantic.warning)
                }
            }
        }
    }
}
