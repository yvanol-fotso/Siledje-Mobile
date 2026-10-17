package com.siledje.mobile.ui.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.siledje.mobile.domain.model.Product
import com.siledje.mobile.domain.model.StockMovement
import com.siledje.mobile.ui.components.ConfirmDialog
import com.siledje.mobile.ui.components.EmptyState
import com.siledje.mobile.ui.components.FilterPill
import com.siledje.mobile.ui.components.FormTextField
import com.siledje.mobile.ui.components.InfoRow
import com.siledje.mobile.ui.components.ListDivider
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SectionCard
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.StatusChip
import com.siledje.mobile.ui.components.StockBar
import com.siledje.mobile.ui.components.formatFcfa
import com.siledje.mobile.ui.components.formatSaleDate
import com.siledje.mobile.ui.state.CatalogStore
import com.siledje.mobile.ui.state.StockMovementTypes
import com.siledje.mobile.ui.theme.semantic

fun movementLabel(type: String): String = when (type.lowercase()) {
    "in", "purchase", "restock", "entry" -> "Entrée"
    "out", "sale", "exit" -> "Sortie"
    "adjustment", "correction" -> "Correction"
    else -> type.replaceFirstChar { it.uppercase() }
}

/** Fiche produit : etat du stock, informations, actions et historique des mouvements. */
@Composable
fun ProductDetailScreen(
    catalog: CatalogStore,
    onBack: () -> Unit,
    onEdit: (Product) -> Unit
) {
    val product = catalog.selectedProduct
    if (product == null) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title = "Produit", onBack = onBack)
            EmptyState(Icons.Filled.Inventory2, "Produit introuvable", "Retournez à la liste du stock.")
        }
        return
    }

    var showAdjust by remember { mutableStateOf(false) }
    var showDeactivate by remember { mutableStateOf(false) }
    val statusColor = stockStatusColor(product)
    val margin = product.sellPrice - product.buyPrice

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = product.name,
            subtitle = catalog.categoryLabel(product),
            onBack = onBack,
            actions = {
                IconButton(onClick = { onEdit(product) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Modifier", tint = androidx.compose.ui.graphics.Color.White)
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SiledjeCard {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Quantité en stock",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(product.stockQuantity.toString(), style = MaterialTheme.typography.headlineSmall)
                        }
                        StatusChip(stockStatusLabel(product), statusColor)
                    }
                    Spacer(Modifier.height(12.dp))
                    StockBar(product.stockQuantity, product.minStockThreshold, statusColor)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Seuil d'alerte : ${product.minStockThreshold}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(onClick = { showAdjust = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.SwapVert, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ajuster le stock")
                }
                OutlinedButton(onClick = { showDeactivate = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Désactiver")
                }
            }

            SectionCard("Informations") {
                InfoRow("Catégorie", catalog.categoryLabel(product))
                ListDivider()
                InfoRow("Fournisseur", catalog.supplierLabel(product) ?: "Non renseigné")
                ListDivider()
                InfoRow("SKU", product.sku?.takeIf { it.isNotBlank() } ?: "Non renseigné")
                ListDivider()
                InfoRow("Emplacement", product.location?.takeIf { it.isNotBlank() } ?: "Non renseigné")
                product.description?.takeIf { it.isNotBlank() }?.let {
                    ListDivider()
                    InfoRow("Description", it)
                }
            }

            SectionCard("Prix") {
                InfoRow("Prix d'achat", formatFcfa(product.buyPrice))
                ListDivider()
                InfoRow("Prix de vente", formatFcfa(product.sellPrice))
                ListDivider()
                InfoRow(
                    "Marge unitaire",
                    formatFcfa(margin),
                    valueColor = if (margin >= 0) MaterialTheme.semantic.success else MaterialTheme.colorScheme.onSurface
                )
            }

            SectionCard("Derniers mouvements de stock") {
                when {
                    catalog.movementsLoading -> Text(
                        "Chargement de l'historique",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    catalog.movements.isEmpty() -> Text(
                        "Aucun mouvement enregistré pour ce produit.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    else -> catalog.movements.forEachIndexed { index, movement ->
                        if (index > 0) ListDivider()
                        MovementRow(movement)
                    }
                }
            }
        }
    }

    if (showAdjust) {
        AdjustStockDialog(
            product = product,
            saving = catalog.saving,
            onDismiss = { showAdjust = false },
            onConfirm = { delta, type, reason ->
                catalog.adjustStock(product, delta, type, reason)
                showAdjust = false
            }
        )
    }

    if (showDeactivate) {
        ConfirmDialog(
            title = "Désactiver ce produit ?",
            message = "Le produit n'apparaîtra plus dans le stock ni à la vente. Son historique est conservé.",
            confirmText = "Désactiver",
            destructive = true,
            onConfirm = {
                showDeactivate = false
                catalog.deactivateProduct(product, onDone = onBack)
            },
            onDismiss = { showDeactivate = false }
        )
    }
}

@Composable
private fun MovementRow(movement: StockMovement) {
    val semantic = MaterialTheme.semantic
    val positive = movement.quantity >= 0
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(movementLabel(movement.movementType), style = MaterialTheme.typography.titleSmall)
            Text(
                formatSaleDate(movement.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            movement.reason?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                (if (positive) "+" else "") + movement.quantity,
                style = MaterialTheme.typography.titleSmall,
                color = if (positive) semantic.success else semantic.warning
            )
            if (movement.quantityAfter != null) {
                Text(
                    "Stock : ${movement.quantityAfter}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private enum class AdjustMode(val label: String) {
    IN("Entrée"),
    OUT("Sortie"),
    SET("Correction")
}

@Composable
private fun AdjustStockDialog(
    product: Product,
    saving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (delta: Int, type: String, reason: String) -> Unit
) {
    var mode by remember { mutableStateOf(AdjustMode.IN) }
    var qtyText by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    val qty = qtyText.toIntOrNull()
    val current = product.stockQuantity
    val delta = when (mode) {
        AdjustMode.IN -> qty ?: 0
        AdjustMode.OUT -> -(qty ?: 0)
        AdjustMode.SET -> (qty ?: current) - current
    }
    val newStock = current + delta
    val valid = qty != null && qty >= 0 && newStock >= 0 && delta != 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajuster le stock") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdjustMode.values().forEach { m ->
                        FilterPill(m.label, selected = m == mode, onClick = { mode = m })
                    }
                }
                FormTextField(
                    value = qtyText,
                    onValueChange = { v -> qtyText = v.filter { it.isDigit() } },
                    label = if (mode == AdjustMode.SET) "Nouvelle quantité" else "Quantité",
                    keyboardType = KeyboardType.Number,
                    error = if (qtyText.isNotEmpty() && newStock < 0) "Stock insuffisant" else null
                )
                FormTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = "Motif (facultatif)"
                )
                Text(
                    "Stock actuel : $current, après ajustement : ${if (newStock < 0) "-" else newStock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid && !saving,
                onClick = {
                    val type = when (mode) {
                        AdjustMode.IN -> StockMovementTypes.IN
                        AdjustMode.OUT -> StockMovementTypes.OUT
                        AdjustMode.SET -> StockMovementTypes.ADJUSTMENT
                    }
                    onConfirm(delta, type, reason)
                }
            ) { Text("Valider") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
