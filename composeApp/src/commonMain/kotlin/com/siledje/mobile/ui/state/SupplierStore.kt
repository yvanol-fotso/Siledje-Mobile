package com.siledje.mobile.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.siledje.mobile.data.repository.CatalogRepository
import com.siledje.mobile.data.repository.SupplierOrderRepository
import com.siledje.mobile.domain.model.Supplier
import com.siledje.mobile.domain.model.SupplierOrder
import com.siledje.mobile.ui.components.ToastState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Donnees saisies dans le formulaire fournisseur. */
data class SupplierInput(
    val name: String,
    val contactName: String,
    val email: String,
    val phone: String,
    val phone2: String,
    val address: String,
    val city: String,
    val paymentTerms: String,
    val notes: String
) {
    private fun String.orNull(): String? = trim().ifBlank { null }

    fun toSupplier(): Supplier = Supplier(
        name = name.trim(),
        contactName = contactName.orNull(),
        email = email.orNull(),
        phone = phone.orNull(),
        phone2 = phone2.orNull(),
        address = address.orNull(),
        city = city.orNull(),
        paymentTerms = paymentTerms.orNull(),
        notes = notes.orNull()
    )

    fun toFields(): Map<String, Any?> = mapOf(
        "name" to name.trim(),
        "contact_name" to contactName.orNull(),
        "email" to email.orNull(),
        "phone" to phone.orNull(),
        "phone2" to phone2.orNull(),
        "address" to address.orNull(),
        "city" to city.orNull(),
        "payment_terms" to paymentTerms.orNull(),
        "notes" to notes.orNull()
    )
}

/** Etat et logique de l'ecran Fournisseurs. La liste vient du CatalogStore (chargee une seule fois). */
class SupplierStore(
    private val catalog: CatalogStore,
    private val repo: CatalogRepository,
    private val orderRepo: SupplierOrderRepository,
    private val scope: CoroutineScope,
    private val toast: ToastState
) {
    var query by mutableStateOf("")

    var selected by mutableStateOf<Supplier?>(null)
        private set
    var orders by mutableStateOf<List<SupplierOrder>>(emptyList())
        private set
    var ordersLoading by mutableStateOf(false)
        private set
    var ordersError by mutableStateOf(false)
        private set

    var formSupplier by mutableStateOf<Supplier?>(null)
    var saving by mutableStateOf(false)
        private set

    val filteredSuppliers: List<Supplier>
        get() {
            val q = query.trim()
            if (q.isEmpty()) return catalog.suppliers
            return catalog.suppliers.filter {
                it.name.contains(q, ignoreCase = true) ||
                    (it.city ?: "").contains(q, ignoreCase = true) ||
                    (it.contactName ?: "").contains(q, ignoreCase = true)
            }
        }

    fun open(supplier: Supplier) {
        selected = supplier
        orders = emptyList()
        ordersError = false
        ordersLoading = true
        scope.launch {
            safeCall { orderRepo.getOrdersForSupplier(supplier.id) }
                .onSuccess { orders = it }
                .onFailure { ordersError = true }
            ordersLoading = false
        }
    }

    // ── Ecritures (toutes passent par WriteGuard) ────────────────────

    fun saveSupplier(input: SupplierInput, existing: Supplier?, onDone: () -> Unit) =
        WriteGuard.run(toast) {
            scope.launch {
                saving = true
                val result = safeCall {
                    if (existing == null) repo.createSupplier(input.toSupplier())
                    else repo.updateSupplier(existing.id, input.toFields())
                }
                saving = false
                result.onSuccess {
                    toast.success(if (existing == null) "Fournisseur ajouté." else "Fournisseur modifié.")
                    catalog.refresh()
                    onDone()
                }.onFailure {
                    toast.error("Enregistrement impossible. ${friendlyNetworkMessage(it.message)}")
                }
            }
        }

    fun deactivateSupplier(supplier: Supplier, onDone: () -> Unit) =
        WriteGuard.run(toast) {
            scope.launch {
                val result = safeCall { repo.setSupplierActive(supplier.id, false) }
                result.onSuccess {
                    toast.success("Fournisseur désactivé.")
                    catalog.refresh()
                    onDone()
                }.onFailure {
                    toast.error("Désactivation impossible. ${friendlyNetworkMessage(it.message)}")
                }
            }
        }
}
