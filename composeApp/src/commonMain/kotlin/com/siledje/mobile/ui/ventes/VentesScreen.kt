package com.siledje.mobile.ui.ventes

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
import com.siledje.mobile.data.repository.SalesRepository
import com.siledje.mobile.domain.model.Sale

/**
 * Équivalent mobile de sales_view.py / sales_table.py : historique des
 * ventes, en lecture (l'encaissement reste sur la caisse desktop pour
 * l'instant — voir SalesRepository.createSale si tu veux l'ajouter ici).
 */
@Composable
fun VentesScreen() {
    var loading by remember { mutableStateOf(true) }
    var sales by remember { mutableStateOf<List<Sale>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    val salesRepo = remember { SalesRepository() }

    LaunchedEffect(Unit) {
        try {
            sales = salesRepo.getRecentSales(limit = 100)
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

        sales.isEmpty() -> Text("Aucune vente pour l'instant.", modifier = Modifier.padding(16.dp))

        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sales) { sale ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(sale.invoiceNumber, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${sale.totalAmount} FCFA · ${sale.paymentMethodName ?: "—"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            sale.clientName ?: "Client anonyme",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
