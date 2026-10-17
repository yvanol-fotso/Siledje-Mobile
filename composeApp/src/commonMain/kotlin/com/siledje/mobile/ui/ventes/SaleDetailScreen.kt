package com.siledje.mobile.ui.ventes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.siledje.mobile.domain.model.SaleItem
import com.siledje.mobile.ui.components.EmptyState
import com.siledje.mobile.ui.components.InfoRow
import com.siledje.mobile.ui.components.ListDivider
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SectionCard
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.StatusChip
import com.siledje.mobile.ui.components.formatFcfa
import com.siledje.mobile.ui.components.formatSaleDate
import com.siledje.mobile.ui.state.CatalogStore
import com.siledje.mobile.ui.state.SalesStore
import com.siledje.mobile.ui.theme.semantic

/** Detail d'une facture : en-tete, client, articles vendus et totaux. */
@Composable
fun SaleDetailScreen(store: SalesStore, catalog: CatalogStore, onBack: () -> Unit) {
    val sale = store.selected
    if (sale == null) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title = "Facture", onBack = onBack)
            EmptyState(Icons.Filled.ReceiptLong, "Facture introuvable", "Retournez à la liste des ventes.")
        }
        return
    }

    val semantic = MaterialTheme.semantic
    val statusColor = if (sale.status.lowercase() == "completed") semantic.success else semantic.warning
    val lineCount = store.items.size
    val unitCount = store.items.sumOf { it.quantity }
    val articlesTitle = "Articles ($lineCount ligne" + (if (lineCount > 1) "s" else "") +
        ", $unitCount unité" + (if (unitCount > 1) "s" else "") + ")"

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = sale.invoiceNumber.ifBlank { "Facture" },
            subtitle = formatSaleDate(sale.saleDate),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Montant total mis en avant
            SiledjeCard {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Montant total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(formatFcfa(sale.totalAmount), style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(saleStatusLabel(sale.status), statusColor)
                        store.paymentLabel(sale)?.let { StatusChip(it, MaterialTheme.colorScheme.primary) }
                    }
                }
            }

            SectionCard("Informations") {
                InfoRow("Client", store.selectedClient?.name ?: store.clientLabel(sale))
                store.selectedClient?.phone?.takeIf { it.isNotBlank() }?.let {
                    ListDivider()
                    InfoRow("Téléphone", it)
                }
                ListDivider()
                InfoRow("Date", formatSaleDate(sale.saleDate))
                store.paymentLabel(sale)?.let {
                    ListDivider()
                    InfoRow("Paiement", it)
                }
                sale.notes?.takeIf { it.isNotBlank() }?.let {
                    ListDivider()
                    InfoRow("Note", it)
                }
            }

            SectionCard(articlesTitle) {
                when {
                    store.itemsLoading -> Text(
                        "Chargement des articles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    store.itemsError -> Text(
                        "Impossible de charger les articles de cette facture.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    store.items.isEmpty() -> Text(
                        "Aucun article enregistré pour cette facture.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    else -> store.items.forEachIndexed { index, item ->
                        if (index > 0) ListDivider()
                        SaleItemRow(item, catalog)
                    }
                }
            }

            SectionCard("Récapitulatif") {
                InfoRow("Sous-total", formatFcfa(sale.subtotal))
                if (sale.discountAmount > 0) {
                    InfoRow("Remise", "-" + formatFcfa(sale.discountAmount))
                }
                if (sale.taxAmount > 0) {
                    InfoRow("Taxes", formatFcfa(sale.taxAmount))
                }
                ListDivider()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", style = MaterialTheme.typography.titleMedium)
                    Text(formatFcfa(sale.totalAmount), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun SaleItemRow(item: SaleItem, catalog: CatalogStore) {
    val name = item.productNameSnap?.takeIf { it.isNotBlank() }
        ?: catalog.products.firstOrNull { it.id == item.productId }?.name
        ?: "Article"
    Row(verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleSmall)
            Text(
                "${item.quantity} x ${formatFcfa(item.unitPrice)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (item.discount > 0) {
                Text(
                    "Remise " + formatFcfa(item.discount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.semantic.warning
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            formatFcfa(item.totalPrice),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.End
        )
    }
}
