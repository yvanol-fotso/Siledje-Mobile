package com.siledje.mobile.ui.fournisseurs

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
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
import com.siledje.mobile.domain.model.Supplier
import com.siledje.mobile.ui.components.EmptyState
import com.siledje.mobile.ui.components.ErrorState
import com.siledje.mobile.ui.components.InitialAvatar
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SearchField
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.SkeletonList
import kotlinx.coroutines.CancellationException

/**
 * Équivalent mobile de supplier_view.py / supplier_table.py / supplier_form.py.
 * Ajout/édition de fournisseur pas encore ici — dis-moi si tu veux le
 * formulaire (CatalogRepository.createSupplier est déjà prêt).
 */
@Composable
fun FournisseursScreen() {
    var loading by remember { mutableStateOf(true) }
    var suppliers by remember { mutableStateOf<List<Supplier>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var reloadKey by remember { mutableStateOf(0) }

    val catalogRepo = remember { CatalogRepository() }

    LaunchedEffect(reloadKey) {
        loading = true
        error = null
        try {
            suppliers = catalogRepo.getAllSuppliers()
            loading = false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "Erreur de chargement"
            loading = false
        }
    }

    val shown = if (query.isBlank()) suppliers else suppliers.filter {
        it.name.contains(query, ignoreCase = true) ||
            (it.city ?: "").contains(query, ignoreCase = true) ||
            (it.contactName ?: "").contains(query, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Fournisseurs",
            subtitle = if (loading) "Chargement…"
            else "${suppliers.size} fournisseur" + (if (suppliers.size > 1) "s" else ""),
            onRefresh = { reloadKey++ }
        )

        if (!loading && error == null && suppliers.isNotEmpty()) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)) {
                SearchField(query, { query = it }, "Rechercher un fournisseur")
            }
        }

        when {
            loading -> SkeletonList()
            error != null -> ErrorState(message = error ?: "", onRetry = { reloadKey++ })
            suppliers.isEmpty() -> EmptyState(
                icon = Icons.Filled.LocalShipping,
                title = "Aucun fournisseur",
                message = "Vos fournisseurs apparaîtront ici."
            )
            shown.isEmpty() -> EmptyState(
                icon = Icons.Filled.SearchOff,
                title = "Aucun résultat",
                message = "Essaie un autre mot-clé."
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(shown) { supplier -> SupplierCard(supplier) }
            }
        }
    }
}

@Composable
private fun SupplierCard(supplier: Supplier) {
    SiledjeCard {
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
        }
    }
}

@Composable
private fun InfoLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
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
