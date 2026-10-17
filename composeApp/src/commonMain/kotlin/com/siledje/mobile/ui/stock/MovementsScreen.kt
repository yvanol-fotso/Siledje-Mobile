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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SwapVert
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
import com.siledje.mobile.domain.model.StockMovement
import com.siledje.mobile.ui.components.EmptyState
import com.siledje.mobile.ui.components.ErrorState
import com.siledje.mobile.ui.components.FilterPill
import com.siledje.mobile.ui.components.IconBadge
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.SkeletonList
import com.siledje.mobile.ui.components.StatusChip
import com.siledje.mobile.ui.components.formatSaleDate
import com.siledje.mobile.ui.state.CatalogStore
import com.siledje.mobile.ui.state.RemoteList
import com.siledje.mobile.ui.theme.semantic

private enum class MovementFilter(val label: String) {
    ALL("Tous"),
    IN("Entrées"),
    OUT("Sorties"),
    ADJUST("Corrections")
}

/** Historique global des mouvements de stock, filtrable par type. */
@Composable
fun MovementsScreen(
    movements: RemoteList<StockMovement>,
    catalog: CatalogStore,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) { movements.ensureLoaded() }
    var filter by remember { mutableStateOf(MovementFilter.ALL) }
    val shown = movements.items.filter { m ->
        when (filter) {
            MovementFilter.ALL -> true
            MovementFilter.IN -> movementLabel(m.movementType) == "Entrée"
            MovementFilter.OUT -> movementLabel(m.movementType) == "Sortie"
            MovementFilter.ADJUST -> movementLabel(m.movementType) == "Correction"
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Mouvements de stock",
            subtitle = if (movements.loaded) "${movements.items.size} derniers mouvements" else "Chargement",
            onBack = onBack,
            onRefresh = { movements.refresh() }
        )
        when {
            !movements.loaded && movements.error != null ->
                ErrorState(message = movements.error ?: "", onRetry = { movements.refresh() })
            !movements.loaded -> SkeletonList()
            movements.items.isEmpty() -> EmptyState(
                icon = Icons.Filled.History,
                title = "Aucun mouvement",
                message = "Les entrées, sorties et corrections de stock apparaîtront ici."
            )
            else -> {
                LazyRow(
                    modifier = Modifier.padding(top = 14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MovementFilter.values().toList()) { f ->
                        FilterPill(f.label, selected = filter == f, onClick = { filter = f })
                    }
                }
                if (shown.isEmpty()) {
                    EmptyState(Icons.Filled.SwapVert, "Aucun résultat", "Aucun mouvement de ce type.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(shown) { movement ->
                            val name = movement.productName?.takeIf { it.isNotBlank() }
                                ?: catalog.products.firstOrNull { it.id == movement.productId }?.name
                                ?: "Produit"
                            MovementCard(movement, name)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovementCard(movement: StockMovement, productName: String) {
    val semantic = MaterialTheme.semantic
    val positive = movement.quantity >= 0
    val tint = if (positive) semantic.success else semantic.warning
    SiledjeCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Filled.SwapVert, tint)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    productName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    formatSaleDate(movement.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                movement.reason?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (positive) "+" else "") + movement.quantity,
                    style = MaterialTheme.typography.titleSmall,
                    color = tint
                )
                Spacer(Modifier.height(4.dp))
                StatusChip(movementLabel(movement.movementType), tint)
            }
        }
    }
}
