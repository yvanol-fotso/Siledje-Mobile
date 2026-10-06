package com.siledje.mobile.ui.accueil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.siledje.mobile.data.repository.SalesRepository
import com.siledje.mobile.domain.model.Product
import com.siledje.mobile.domain.model.Sale

/**
 * Équivalent mobile de accueil_manager.py / accueil_view.py : vue
 * d'ensemble pour le propriétaire — alertes stock bas + dernières ventes.
 * Chargement en LaunchedEffect(Unit) : simple pour l'instant, à migrer
 * vers un vrai ViewModel si l'écran grossit.
 */
@Composable
fun AccueilScreen() {
    var loading by remember { mutableStateOf(true) }
    var lowStock by remember { mutableStateOf<List<Product>>(emptyList()) }
    var recentSales by remember { mutableStateOf<List<Sale>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    val catalogRepo = remember { CatalogRepository() }
    val salesRepo = remember { SalesRepository() }

    LaunchedEffect(Unit) {
        try {
            lowStock = catalogRepo.getLowStockProducts()
            recentSales = salesRepo.getRecentSales(limit = 10)
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

        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Stock bas (${lowStock.size})", style = MaterialTheme.typography.titleMedium) }
            items(lowStock) { product ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(product.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Reste ${product.stockQuantity} (seuil ${product.minStockThreshold})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            item { Text("Ventes récentes", style = MaterialTheme.typography.titleMedium) }
            items(recentSales) { sale ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(sale.invoiceNumber, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${sale.totalAmount} FCFA — ${sale.clientName ?: "Client anonyme"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
