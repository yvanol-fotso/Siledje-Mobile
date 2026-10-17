package com.siledje.mobile.ui.fournisseurs

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
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
import com.siledje.mobile.domain.model.SupplierOrder
import com.siledje.mobile.ui.components.EmptyState
import com.siledje.mobile.ui.components.ErrorState
import com.siledje.mobile.ui.components.FilterPill
import com.siledje.mobile.ui.components.IconBadge
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.SkeletonList
import com.siledje.mobile.ui.components.StatusChip
import com.siledje.mobile.ui.components.formatDay
import com.siledje.mobile.ui.components.formatFcfa
import com.siledje.mobile.ui.state.CatalogStore
import com.siledje.mobile.ui.state.RemoteList
import com.siledje.mobile.ui.theme.semantic

private enum class OrderFilter(val label: String) {
    ALL("Toutes"),
    OPEN("En cours"),
    DONE("Livrées")
}

private fun isDelivered(order: SupplierOrder): Boolean =
    order.status.lowercase() in listOf("delivered", "received")

private fun isCancelled(order: SupplierOrder): Boolean =
    order.status.lowercase() in listOf("cancelled", "canceled")

/** Suivi de toutes les commandes passees aux fournisseurs. */
@Composable
fun OrdersScreen(
    orders: RemoteList<SupplierOrder>,
    catalog: CatalogStore,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) { orders.ensureLoaded() }
    var filter by remember { mutableStateOf(OrderFilter.ALL) }
    val shown = when (filter) {
        OrderFilter.ALL -> orders.items
        OrderFilter.OPEN -> orders.items.filter { !isDelivered(it) && !isCancelled(it) }
        OrderFilter.DONE -> orders.items.filter { isDelivered(it) }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Commandes fournisseurs",
            subtitle = if (orders.loaded) "${orders.items.size} commande" + (if (orders.items.size > 1) "s" else "") else "Chargement",
            onBack = onBack,
            onRefresh = { orders.refresh() }
        )
        when {
            !orders.loaded && orders.error != null ->
                ErrorState(message = orders.error ?: "", onRetry = { orders.refresh() })
            !orders.loaded -> SkeletonList()
            orders.items.isEmpty() -> EmptyState(
                icon = Icons.Filled.LocalShipping,
                title = "Aucune commande",
                message = "Les commandes passées à vos fournisseurs apparaîtront ici."
            )
            else -> {
                LazyRow(
                    modifier = Modifier.padding(top = 14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(OrderFilter.values().toList()) { f ->
                        FilterPill(f.label, selected = filter == f, onClick = { filter = f })
                    }
                }
                if (shown.isEmpty()) {
                    EmptyState(Icons.Filled.LocalShipping, "Aucune commande", "Aucune commande dans cette catégorie.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(shown) { order -> OrderCard(order, catalog.supplierNames[order.supplierId]) }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: SupplierOrder, supplierName: String?) {
    val semantic = MaterialTheme.semantic
    val color = when {
        isDelivered(order) -> semantic.success
        isCancelled(order) -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> semantic.warning
    }
    SiledjeCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Filled.LocalShipping, color)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    order.orderNumber.ifBlank { "Commande" },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    supplierName ?: "Fournisseur inconnu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Passée le " + formatDay(order.orderDate) +
                        (order.expectedDate?.takeIf { it.isNotBlank() }?.let { ", prévue le " + formatDay(it) } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(formatFcfa(order.totalAmount), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                StatusChip(orderStatusLabel(order.status), color)
            }
        }
    }
}
