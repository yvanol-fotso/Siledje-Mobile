package com.siledje.mobile.ui.rapports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siledje.mobile.ui.components.BarChart
import com.siledje.mobile.ui.components.EmptyState
import com.siledje.mobile.ui.components.ErrorState
import com.siledje.mobile.ui.components.KpiCard
import com.siledje.mobile.ui.components.RatioBar
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SectionCard
import com.siledje.mobile.ui.components.SkeletonList
import com.siledje.mobile.ui.components.formatDay
import com.siledje.mobile.ui.components.formatFcfa
import com.siledje.mobile.ui.state.CatalogStore
import com.siledje.mobile.ui.state.SalesStore
import com.siledje.mobile.ui.theme.semantic

private val EXCLUDED_STATUSES = listOf("cancelled", "canceled", "refunded")

/** Tableau de bord chiffre, calcule sur les 100 dernieres ventes deja chargees. */
@Composable
fun RapportsScreen(catalog: CatalogStore, sales: SalesStore, onBack: () -> Unit, onRefresh: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Rapports",
            subtitle = "Sur les ${sales.sales.size} dernières ventes",
            onBack = onBack,
            onRefresh = onRefresh
        )

        when {
            !sales.loaded && sales.error != null -> ErrorState(message = sales.error ?: "", onRetry = onRefresh)
            !sales.loaded -> SkeletonList()
            sales.sales.isEmpty() -> EmptyState(
                icon = Icons.Filled.BarChart,
                title = "Pas encore de données",
                message = "Les rapports apparaîtront dès que des ventes seront enregistrées."
            )
            else -> ReportBody(catalog, sales)
        }
    }
}

@Composable
private fun ReportBody(catalog: CatalogStore, sales: SalesStore) {
    val semantic = MaterialTheme.semantic
    val counted = sales.sales.filter { it.status.lowercase() !in EXCLUDED_STATUSES }
    val revenue = counted.sumOf { it.totalAmount }
    val average = if (counted.isEmpty()) 0.0 else revenue / counted.size

    val byDay = counted
        .groupBy { (it.saleDate ?: "").take(10) }
        .filterKeys { it.length == 10 }
        .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
    val lastDays = byDay.keys.sorted().takeLast(7)
    val chart = lastDays.map { day -> (day.substring(8, 10) + "/" + day.substring(5, 7)) to (byDay[day] ?: 0.0) }
    val bestDay = byDay.entries.maxByOrNull { it.value }

    val byPayment = counted
        .groupBy { sales.paymentLabel(it) ?: "Non précisé" }
        .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
        .entries
        .sortedByDescending { it.value }

    val byCategory = catalog.products
        .groupBy { catalog.categoryLabel(it) }
        .map { (name, list) -> Triple(name, list.size, list.sumOf { it.buyPrice * it.stockQuantity }) }
        .sortedByDescending { it.second }
    val maxCount = byCategory.maxOfOrNull { it.second } ?: 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard("Chiffre d'affaires", formatFcfa(revenue), Icons.Filled.Payments, semantic.success, Modifier.weight(1f))
            KpiCard("Panier moyen", formatFcfa(average), Icons.Filled.ShoppingCart, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard("Ventes", counted.size.toString(), Icons.Filled.ReceiptLong, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            KpiCard(
                "Meilleur jour",
                bestDay?.let { formatFcfa(it.value) } ?: "-",
                Icons.Filled.TrendingUp,
                semantic.success,
                Modifier.weight(1f)
            )
        }

        SectionCard("Ventes des derniers jours") {
            if (chart.isEmpty()) {
                Text(
                    "Aucune date de vente exploitable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                BarChart(chart)
                bestDay?.let {
                    Text(
                        "Meilleure journée : ${formatDay(it.key)} avec ${formatFcfa(it.value)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        SectionCard("Modes de paiement") {
            if (byPayment.isEmpty() || revenue <= 0) {
                Text(
                    "Aucun paiement à afficher.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                byPayment.forEach { entry ->
                    val share = (entry.value / revenue).toFloat()
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(entry.key, style = MaterialTheme.typography.titleSmall)
                            Text(
                                formatFcfa(entry.value) + " (" + (share * 100).toInt() + " %)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RatioBar(share, MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        SectionCard("Produits par catégorie") {
            if (byCategory.isEmpty()) {
                Text(
                    "Aucun produit enregistré.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                byCategory.forEach { (name, count, value) ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "$count produit" + (if (count > 1) "s" else "") + ", stock " + formatFcfa(value),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RatioBar(count.toFloat() / maxCount, semantic.success)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
