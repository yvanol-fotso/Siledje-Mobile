package com.siledje.mobile.ui.stock

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
import androidx.compose.material3.OutlinedTextField
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
import com.siledje.mobile.domain.model.Product

/**
 * Équivalent mobile de stock_view.py / stock_table.py / stock_form.py.
 * L'ajout/édition de produit (stock_form.py) n'est pas encore ici —
 * dis-moi si tu veux qu'on l'ajoute (formulaire + adjustStock côté
 * CatalogRepository, déjà prêt côté repository).
 */
@Composable
fun StockScreen() {
    var loading by remember { mutableStateOf(true) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchTerm by remember { mutableStateOf("") }

    val catalogRepo = remember { CatalogRepository() }

    LaunchedEffect(Unit) {
        try {
            products = catalogRepo.getAllProducts()
        } catch (e: Exception) {
            error = e.message ?: "Erreur de chargement"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(searchTerm) {
        if (searchTerm.isBlank()) return@LaunchedEffect
        try {
            products = catalogRepo.searchProducts(searchTerm)
        } catch (_: Exception) { /* on garde la liste précédente en cas d'erreur réseau */ }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchTerm,
            onValueChange = { searchTerm = it },
            label = { Text("Rechercher un produit") },
            modifier = Modifier.fillMaxWidth()
        )

        when {
            loading -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }

            error != null -> Text(
                "Erreur : $error",
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.error
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(products) { product ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(product.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${product.sellPrice} FCFA · ${product.categoryName ?: "Sans catégorie"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Stock : ${product.stockQuantity}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (product.isLowStock)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
