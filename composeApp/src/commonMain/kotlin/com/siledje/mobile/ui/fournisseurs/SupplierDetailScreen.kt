package com.siledje.mobile.ui.fournisseurs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.siledje.mobile.domain.model.Supplier
import com.siledje.mobile.domain.model.SupplierOrder
import com.siledje.mobile.ui.components.ConfirmDialog
import com.siledje.mobile.ui.components.EmptyState
import com.siledje.mobile.ui.components.InfoRow
import com.siledje.mobile.ui.components.ListDivider
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SectionCard
import com.siledje.mobile.ui.components.StatusChip
import com.siledje.mobile.ui.components.formatDay
import com.siledje.mobile.ui.components.formatFcfa
import com.siledje.mobile.ui.state.CatalogStore
import com.siledje.mobile.ui.state.SupplierStore
import com.siledje.mobile.ui.theme.semantic

fun orderStatusLabel(status: String): String = when (status.lowercase()) {
    "pending" -> "En attente"
    "ordered", "sent" -> "Commandée"
    "delivered", "received" -> "Livrée"
    "cancelled", "canceled" -> "Annulée"
    else -> status.replaceFirstChar { it.uppercase() }
}

/** Fiche fournisseur : coordonnees, appel et email en un geste, commandes passees. */
@Composable
fun SupplierDetailScreen(
    catalog: CatalogStore,
    store: SupplierStore,
    onBack: () -> Unit,
    onEdit: (Supplier) -> Unit
) {
    val supplier = store.selected
    if (supplier == null) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title = "Fournisseur", onBack = onBack)
            EmptyState(Icons.Filled.LocalShipping, "Fournisseur introuvable", "Retournez à la liste.")
        }
        return
    }

    val uriHandler = LocalUriHandler.current
    var showDeactivate by remember { mutableStateOf(false) }
    val phone = supplier.phone?.takeIf { it.isNotBlank() }
    val email = supplier.email?.takeIf { it.isNotBlank() }
    val productCount = catalog.productCountForSupplier(supplier.id)

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = supplier.name,
            subtitle = supplier.city?.takeIf { it.isNotBlank() },
            onBack = onBack,
            actions = {
                IconButton(onClick = { onEdit(supplier) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Modifier", tint = Color.White)
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
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(
                    onClick = { phone?.let { runCatching { uriHandler.openUri("tel:$it") } } },
                    enabled = phone != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Appeler")
                }
                FilledTonalButton(
                    onClick = { email?.let { runCatching { uriHandler.openUri("mailto:$it") } } },
                    enabled = email != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Écrire")
                }
            }

            SectionCard("Coordonnées") {
                InfoRow("Contact", supplier.contactName?.takeIf { it.isNotBlank() } ?: "Non renseigné")
                ListDivider()
                InfoRow("Téléphone", phone ?: "Non renseigné")
                supplier.phone2?.takeIf { it.isNotBlank() }?.let {
                    ListDivider()
                    InfoRow("Téléphone 2", it)
                }
                ListDivider()
                InfoRow("Email", email ?: "Non renseigné")
                ListDivider()
                InfoRow("Adresse", supplier.address?.takeIf { it.isNotBlank() } ?: "Non renseignée")
                ListDivider()
                InfoRow("Ville", supplier.city?.takeIf { it.isNotBlank() } ?: "Non renseignée")
            }

            SectionCard("Conditions") {
                InfoRow("Paiement", supplier.paymentTerms?.takeIf { it.isNotBlank() } ?: "Non renseigné")
                ListDivider()
                InfoRow("Produits fournis", productCount.toString())
                supplier.notes?.takeIf { it.isNotBlank() }?.let {
                    ListDivider()
                    InfoRow("Notes", it)
                }
            }

            SectionCard("Commandes") {
                when {
                    store.ordersLoading -> Text(
                        "Chargement des commandes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    store.ordersError -> Text(
                        "Impossible de charger les commandes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    store.orders.isEmpty() -> Text(
                        "Aucune commande passée à ce fournisseur.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    else -> store.orders.forEachIndexed { index, order ->
                        if (index > 0) ListDivider()
                        OrderRow(order)
                    }
                }
            }

            OutlinedButton(onClick = { showDeactivate = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Désactiver ce fournisseur")
            }
        }
    }

    if (showDeactivate) {
        ConfirmDialog(
            title = "Désactiver ce fournisseur ?",
            message = "Il n'apparaîtra plus dans la liste. Ses commandes et ses produits restent conservés.",
            confirmText = "Désactiver",
            destructive = true,
            onConfirm = {
                showDeactivate = false
                store.deactivateSupplier(supplier, onDone = onBack)
            },
            onDismiss = { showDeactivate = false }
        )
    }
}

@Composable
private fun OrderRow(order: SupplierOrder) {
    val semantic = MaterialTheme.semantic
    val delivered = order.status.lowercase() in listOf("delivered", "received")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(order.orderNumber.ifBlank { "Commande" }, style = MaterialTheme.typography.titleSmall)
            Text(
                formatDay(order.orderDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatFcfa(order.totalAmount), style = MaterialTheme.typography.titleSmall)
            StatusChip(
                orderStatusLabel(order.status),
                if (delivered) semantic.success else semantic.warning
            )
        }
    }
}
