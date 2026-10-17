package com.siledje.mobile.ui.fournisseurs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siledje.mobile.domain.model.Supplier
import com.siledje.mobile.ui.components.EmptyState
import com.siledje.mobile.ui.components.ErrorState
import com.siledje.mobile.ui.components.InitialAvatar
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SearchField
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.SkeletonList
import com.siledje.mobile.ui.components.StatusChip
import com.siledje.mobile.ui.state.CatalogStore
import com.siledje.mobile.ui.state.SupplierStore

/** Liste des fournisseurs avec recherche et bouton d'ajout. */
@Composable
fun FournisseursScreen(
    catalog: CatalogStore,
    store: SupplierStore,
    onOpenSupplier: (Supplier) -> Unit,
    onAddSupplier: () -> Unit,
    onRefresh: () -> Unit
) {
    val ready = catalog.loaded
    val shown = store.filteredSuppliers

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "Fournisseurs",
                subtitle = when {
                    !ready && catalog.error == null -> "Chargement"
                    !ready -> "Indisponible"
                    else -> "${catalog.suppliers.size} fournisseur" + (if (catalog.suppliers.size > 1) "s" else "")
                },
                onRefresh = onRefresh
            )

            when {
                !ready && catalog.error != null -> ErrorState(message = catalog.error ?: "", onRetry = onRefresh)
                !ready -> SkeletonList()
                catalog.suppliers.isEmpty() -> EmptyState(
                    icon = Icons.Filled.LocalShipping,
                    title = "Aucun fournisseur",
                    message = "Ajoutez votre premier fournisseur avec le bouton en bas à droite."
                )
                else -> {
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)) {
                        SearchField(store.query, { store.query = it }, "Rechercher par nom, contact ou ville")
                    }
                    if (shown.isEmpty()) {
                        EmptyState(
                            icon = Icons.Filled.SearchOff,
                            title = "Aucun résultat",
                            message = "Essayez un autre mot-clé."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(shown) { supplier ->
                                SupplierCard(
                                    supplier = supplier,
                                    productCount = catalog.productCountForSupplier(supplier.id),
                                    onClick = { onOpenSupplier(supplier) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (ready) {
            ExtendedFloatingActionButton(
                onClick = onAddSupplier,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Ajouter un fournisseur") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
    }
}

@Composable
private fun SupplierCard(supplier: Supplier, productCount: Int, onClick: () -> Unit) {
    SiledjeCard(onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            InitialAvatar(supplier.name)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    supplier.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                supplier.contactName?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                supplier.phone?.takeIf { it.isNotBlank() }?.let { InfoLine(Icons.Filled.Phone, it) }
                supplier.city?.takeIf { it.isNotBlank() }?.let { InfoLine(Icons.Filled.LocationOn, it) }
            }
            if (productCount > 0) {
                Spacer(Modifier.width(8.dp))
                StatusChip(
                    "$productCount produit" + (if (productCount > 1) "s" else ""),
                    MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun InfoLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
