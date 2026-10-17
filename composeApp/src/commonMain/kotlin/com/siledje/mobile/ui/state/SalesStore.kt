package com.siledje.mobile.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.siledje.mobile.data.repository.SalesRepository
import com.siledje.mobile.domain.model.Client
import com.siledje.mobile.domain.model.Sale
import com.siledje.mobile.domain.model.SaleItem
import com.siledje.mobile.ui.components.ToastState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Etat de l'historique des ventes et du detail d'une facture. */
class SalesStore(
    private val repo: SalesRepository,
    private val scope: CoroutineScope,
    private val toast: ToastState
) {
    var sales by mutableStateOf<List<Sale>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var loaded by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    var query by mutableStateOf("")

    var paymentNames by mutableStateOf<Map<String, String>>(emptyMap())
        private set
    var clientNames by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    var selected by mutableStateOf<Sale?>(null)
        private set
    var items by mutableStateOf<List<SaleItem>>(emptyList())
        private set
    var itemsLoading by mutableStateOf(false)
        private set
    var itemsError by mutableStateOf(false)
        private set
    var selectedClient by mutableStateOf<Client?>(null)
        private set

    private var refreshJob: Job? = null

    val filteredSales: List<Sale>
        get() {
            val q = query.trim()
            if (q.isEmpty()) return sales
            return sales.filter {
                it.invoiceNumber.contains(q, ignoreCase = true) ||
                    clientLabel(it).contains(q, ignoreCase = true)
            }
        }

    fun clientLabel(sale: Sale): String =
        sale.clientName?.takeIf { it.isNotBlank() }
            ?: sale.clientId?.let { clientNames[it] }
            ?: "Client anonyme"

    fun paymentLabel(sale: Sale): String? =
        sale.paymentMethodName?.takeIf { it.isNotBlank() }
            ?: sale.paymentMethodId?.let { paymentNames[it] }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            loading = true
            if (!loaded) error = null
            safeCall { repo.getRecentSales(100) }
                .onSuccess { list ->
                    sales = list
                    loaded = true
                    error = null
                    loadLabels(list)
                }
                .onFailure { e ->
                    if (loaded) toast.error("Actualisation impossible. Vérifiez votre connexion.")
                    else error = e.message ?: "Erreur de chargement"
                }
            loading = false
        }
    }

    /** Noms des modes de paiement et des clients : facultatifs, un echec n'empeche rien. */
    private suspend fun loadLabels(list: List<Sale>) {
        safeCall { repo.getPaymentMethods() }
            .onSuccess { methods -> paymentNames = methods.associate { it.id to it.name } }
        val ids = list.mapNotNull { it.clientId }.filter { it.isNotBlank() }.distinct()
        if (ids.isNotEmpty()) {
            safeCall { repo.getClientsByIds(ids) }
                .onSuccess { clients -> clientNames = clients.associate { it.id to it.name } }
        }
    }

    fun open(sale: Sale) {
        selected = sale
        items = emptyList()
        itemsError = false
        itemsLoading = true
        selectedClient = null
        scope.launch {
            safeCall { repo.getSaleItems(sale.id) }
                .onSuccess { items = it }
                .onFailure { itemsError = true }
            itemsLoading = false
        }
        val clientId = sale.clientId
        if (!clientId.isNullOrBlank()) {
            scope.launch {
                selectedClient = safeCall { repo.getClientById(clientId) }.getOrNull()
            }
        }
    }
}
