package com.siledje.mobile.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.siledje.mobile.data.repository.CatalogRepository
import com.siledje.mobile.domain.model.Category
import com.siledje.mobile.domain.model.Product
import com.siledje.mobile.domain.model.StockMovement
import com.siledje.mobile.domain.model.Supplier
import com.siledje.mobile.ui.components.ToastState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Libelle des produits sans categorie. */
const val UNCATEGORIZED_LABEL = "Sans catégorie"

/** Donnees saisies dans le formulaire categorie. */
data class CategoryInput(val name: String, val description: String) {
    fun toCategory(): Category = Category(
        name = name.trim(),
        description = description.trim().ifBlank { null }
    )

    fun toFields(): Map<String, Any?> = mapOf(
        "name" to name.trim(),
        "description" to description.trim().ifBlank { null }
    )
}

enum class StockFilter(val label: String) {
    ALL("Tous"),
    LOW("Stock bas"),
    OUT("Rupture")
}

/** Types de mouvement envoyes a la base. A aligner avec ceux utilises par l'application desktop. */
object StockMovementTypes {
    const val IN = "in"
    const val OUT = "out"
    const val ADJUSTMENT = "adjustment"
}

/** Donnees saisies dans le formulaire produit. */
data class ProductInput(
    val name: String,
    val description: String,
    val categoryId: String?,
    val supplierId: String?,
    val buyPrice: Double,
    val sellPrice: Double,
    val stockQuantity: Int,
    val minStockThreshold: Int,
    val sku: String,
    val location: String
) {
    fun toProduct(): Product = Product(
        name = name.trim(),
        description = description.trim().ifBlank { null },
        categoryId = categoryId,
        supplierId = supplierId,
        buyPrice = buyPrice,
        sellPrice = sellPrice,
        stockQuantity = stockQuantity,
        minStockThreshold = minStockThreshold,
        sku = sku.trim().ifBlank { null },
        location = location.trim().ifBlank { null }
    )

    /** Champs modifiables d'un produit existant (le stock passe par l'ajustement, pas par la modification). */
    fun toFields(): Map<String, Any?> = mapOf(
        "name" to name.trim(),
        "description" to description.trim().ifBlank { null },
        "category_id" to categoryId,
        "supplier_id" to supplierId,
        "buy_price" to buyPrice,
        "sell_price" to sellPrice,
        "min_stock_threshold" to minStockThreshold,
        "sku" to sku.trim().ifBlank { null },
        "location" to location.trim().ifBlank { null }
    )
}

/**
 * Etat et logique du catalogue (produits, categories, fournisseurs) partages par
 * l'accueil, le stock et les fournisseurs : une seule source, chargee une seule fois.
 */
class CatalogStore(
    private val repo: CatalogRepository,
    private val scope: CoroutineScope,
    private val toast: ToastState,
    private val currentUserId: () -> String?
) {
    var products by mutableStateOf<List<Product>>(emptyList())
        private set
    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var suppliers by mutableStateOf<List<Supplier>>(emptyList())
        private set
    var categoryNames by mutableStateOf<Map<String, String>>(emptyMap())
        private set
    var supplierNames by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    var loading by mutableStateOf(false)
        private set
    var loaded by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    // Filtres de l'ecran Stock
    var query by mutableStateOf("")
    var categoryFilter by mutableStateOf<String?>(null)
    var stockFilter by mutableStateOf(StockFilter.ALL)

    // Detail / formulaire
    var selectedProduct by mutableStateOf<Product?>(null)
        private set
    var movements by mutableStateOf<List<StockMovement>>(emptyList())
        private set
    var movementsLoading by mutableStateOf(false)
        private set
    var formProduct by mutableStateOf<Product?>(null)
    var saving by mutableStateOf(false)
        private set

    private var refreshJob: Job? = null

    val filteredProducts: List<Product>
        get() {
            val q = query.trim()
            return products.filter { p ->
                val matchQuery = q.isEmpty() ||
                    p.name.contains(q, ignoreCase = true) ||
                    (p.sku ?: "").contains(q, ignoreCase = true)
                val matchCategory = categoryFilter == null || p.categoryId == categoryFilter
                val matchStock = when (stockFilter) {
                    StockFilter.ALL -> true
                    StockFilter.LOW -> p.isLowStock
                    StockFilter.OUT -> p.stockQuantity <= 0
                }
                matchQuery && matchCategory && matchStock
            }
        }

    val lowStockProducts: List<Product>
        get() = products.filter { it.isLowStock }.sortedBy { it.stockQuantity }

    val stockValue: Double
        get() = products.sumOf { it.buyPrice * it.stockQuantity }

    fun categoryLabel(product: Product): String =
        product.categoryId?.let { categoryNames[it] }
            ?: product.categoryName?.takeIf { it.isNotBlank() }
            ?: UNCATEGORIZED_LABEL

    fun supplierLabel(product: Product): String? =
        product.supplierId?.let { supplierNames[it] } ?: product.supplierName

    fun productCountForSupplier(supplierId: String): Int =
        products.count { it.supplierId == supplierId }

    fun productCountForCategory(categoryId: String): Int =
        products.count { it.categoryId == categoryId }

    // ── Chargement ──────────────────────────────────────────────────

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            loading = true
            if (!loaded) error = null
            val result = safeCall {
                coroutineScope {
                    val p = async { repo.getAllProducts() }
                    val s = async { repo.getAllSuppliers() }
                    // Les categories sont optionnelles : leur echec ne bloque pas l'ecran.
                    val c = async { safeCall { repo.getCategories() }.getOrDefault(emptyList()) }
                    Triple(p.await(), s.await(), c.await())
                }
            }
            result.onSuccess { (p, s, c) ->
                products = p
                suppliers = s
                categories = c
                categoryNames = c.associate { it.id to it.name }
                supplierNames = s.associate { it.id to it.name }
                selectedProduct = selectedProduct?.let { sel -> p.firstOrNull { it.id == sel.id } ?: sel }
                loaded = true
                error = null
            }.onFailure { e ->
                if (loaded) toast.error("Actualisation impossible. Vérifiez votre connexion.")
                else error = e.message ?: "Erreur de chargement"
            }
            loading = false
        }
    }

    fun openProduct(product: Product) {
        selectedProduct = product
        movements = emptyList()
        movementsLoading = true
        scope.launch {
            movements = safeCall { repo.getStockMovements(product.id, 50) }.getOrDefault(emptyList())
            movementsLoading = false
        }
    }

    // ── Ecritures (toutes passent par WriteGuard) ────────────────────

    fun saveCategory(input: CategoryInput, existing: Category?, onDone: () -> Unit) =
        WriteGuard.run(toast) {
            scope.launch {
                saving = true
                val result = safeCall {
                    if (existing == null) repo.createCategory(input.toCategory())
                    else repo.updateCategory(existing.id, input.toFields())
                }
                saving = false
                result.onSuccess {
                    toast.success(if (existing == null) "Catégorie ajoutée." else "Catégorie modifiée.")
                    refresh()
                    onDone()
                }.onFailure {
                    toast.error("Enregistrement impossible. ${friendlyNetworkMessage(it.message)}")
                }
            }
        }

    fun deactivateCategory(category: Category) =
        WriteGuard.run(toast) {
            scope.launch {
                val result = safeCall { repo.setCategoryActive(category.id, false) }
                result.onSuccess {
                    toast.success("Catégorie désactivée.")
                    refresh()
                }.onFailure {
                    toast.error("Désactivation impossible. ${friendlyNetworkMessage(it.message)}")
                }
            }
        }

    fun saveProduct(input: ProductInput, existing: Product?, onDone: () -> Unit) =
        WriteGuard.run(toast) {
            scope.launch {
                saving = true
                val result = safeCall {
                    if (existing == null) repo.createProduct(input.toProduct())
                    else repo.updateProduct(existing.id, input.toFields())
                }
                saving = false
                result.onSuccess {
                    toast.success(if (existing == null) "Produit ajouté." else "Produit modifié.")
                    refresh()
                    onDone()
                }.onFailure {
                    toast.error("Enregistrement impossible. ${friendlyNetworkMessage(it.message)}")
                }
            }
        }

    fun deactivateProduct(product: Product, onDone: () -> Unit) =
        WriteGuard.run(toast) {
            scope.launch {
                val result = safeCall { repo.setProductActive(product.id, false) }
                result.onSuccess {
                    toast.success("Produit désactivé.")
                    refresh()
                    onDone()
                }.onFailure {
                    toast.error("Désactivation impossible. ${friendlyNetworkMessage(it.message)}")
                }
            }
        }

    fun adjustStock(product: Product, delta: Int, movementType: String, reason: String?) =
        WriteGuard.run(toast) {
            scope.launch {
                saving = true
                val result = safeCall {
                    repo.adjustStock(product.id, delta, movementType, currentUserId(), reason?.ifBlank { null })
                }
                saving = false
                result.onSuccess {
                    toast.success("Stock mis à jour.")
                    refresh()
                    openProduct(product)
                }.onFailure {
                    toast.error("Ajustement impossible. ${friendlyNetworkMessage(it.message)}")
                }
            }
        }
}
