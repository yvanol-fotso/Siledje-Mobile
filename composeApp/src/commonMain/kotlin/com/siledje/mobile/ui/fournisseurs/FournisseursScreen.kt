package com.siledje.mobile.ui.fournisseurs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siledje.mobile.data.repository.CatalogRepository
import com.siledje.mobile.domain.model.Supplier

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

    val catalogRepo = remember { CatalogRepository() }

    LaunchedEffect(Unit) {
        try {
            suppliers = catalogRepo.getAllSuppliers()
        } catch (e: Exception) {
            error = e.message ?: "Erreur de chargement"
        } finally {
            loading = false
        }
    }

    when {
        loading -> Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }

        error != null -> Text(
            "Erreur : $error",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.error
        )

        suppliers.isEmpty() -> Text("Aucun fournisseur pour l'instant.", modifier = Modifier.padding(16.dp))

        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(suppliers) { supplier ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(supplier.name, style = MaterialTheme.typography.bodyLarge)
                        supplier.phone?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        supplier.city?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }
    }
}
