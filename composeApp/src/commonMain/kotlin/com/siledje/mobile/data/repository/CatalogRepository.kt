package com.siledje.mobile.data.repository

import com.siledje.mobile.data.remote.SupabaseClientProvider.db
import com.siledje.mobile.domain.model.Product
import com.siledje.mobile.domain.model.StockMovement
import com.siledje.mobile.domain.model.Supplier
import io.github.jantennert.supabase.postgrest.query.Columns

/**
 * Équivalent mobile de src/database/repositories/catalog_repository.py.
 * Même découpage de responsabilités : ce repository ne fait QUE parler
 * à Supabase ; les écrans (ViewModel) ne touchent jamais au réseau
 * directement — exactement comme StockManager ne touche jamais SQLite
 * directement côté desktop.
 */
class CatalogRepository {

    suspend fun getAllProducts(activeOnly: Boolean = true): List<Product> {
        var query = db.from("products").select(Columns.raw(
            "*, category_name:categories(name), supplier_name:suppliers(name)"
        ))
        return if (activeOnly) {
            db.from("products").select {
                filter { eq("is_active", true) }
                order("name")
            }.decodeList()
        } else {
            db.from("products").select { order("name") }.decodeList()
        }
    }

    suspend fun searchProducts(term: String): List<Product> =
        db.from("products").select {
            filter {
                eq("is_active", true)
                or {
                    ilike("name", "%$term%")
                    ilike("sku", "%$term%")
                }
            }
            order("name")
        }.decodeList()

    suspend fun getLowStockProducts(): List<Product> =
        // Supabase ne compare pas deux colonnes entre elles côté filtre REST ;
        // on récupère les produits actifs et on filtre côté client avec
        // Product.isLowStock (même règle que get_low_stock_products côté Python).
        db.from("products").select { filter { eq("is_active", true) } }
            .decodeList<Product>()
            .filter { it.isLowStock }
            .sortedBy { it.stockQuantity }

    suspend fun createProduct(product: Product): Product =
        db.from("products").insert(product) { select() }.decodeSingle()

    suspend fun updateProduct(id: Int, fields: Map<String, Any?>): Boolean {
        db.from("products").update(fields) { filter { eq("id", id) } }
        return true
    }

    suspend fun setProductActive(id: Int, isActive: Boolean) {
        db.from("products").update(mapOf("is_active" to isActive)) { filter { eq("id", id) } }
    }

    // ── FOURNISSEURS ────────────────────────────────────────────────

    suspend fun getAllSuppliers(activeOnly: Boolean = true): List<Supplier> =
        db.from("suppliers").select {
            if (activeOnly) filter { eq("is_active", true) }
            order("name")
        }.decodeList()

    suspend fun createSupplier(supplier: Supplier): Supplier =
        db.from("suppliers").insert(supplier) { select() }.decodeSingle()

    suspend fun updateSupplier(id: Int, fields: Map<String, Any?>) {
        db.from("suppliers").update(fields) { filter { eq("id", id) } }
    }

    suspend fun setSupplierActive(id: Int, isActive: Boolean) {
        db.from("suppliers").update(mapOf("is_active" to isActive)) { filter { eq("id", id) } }
    }

    // ── MOUVEMENTS DE STOCK ─────────────────────────────────────────

    /**
     * Contrairement au desktop (adjust_stock côté Python, qui écrit
     * products + stock_movements dans la même transaction SQLite),
     * l'API REST Supabase ne fait pas de transaction multi-tables :
     * on met donc à jour le stock PUIS on trace le mouvement. En cas
     * de coupure réseau entre les deux appels, ce sera à ta logique
     * de sync (déjà présente côté desktop) de réconcilier — même
     * philosophie que le "fusion additive des mouvements" du README.
     */
    suspend fun adjustStock(
        productId: Int,
        quantityChange: Int,
        movementType: String,
        userId: Int?,
        reason: String? = null
    ): StockMovement {
        val product = db.from("products").select { filter { eq("id", productId) } }
            .decodeSingle<Product>()
        val before = product.stockQuantity
        val after = before + quantityChange

        db.from("products").update(mapOf("stock_quantity" to after)) {
            filter { eq("id", productId) }
        }

        return db.from("stock_movements").insert(
            StockMovement(
                productId = productId,
                userId = userId,
                movementType = movementType,
                quantity = quantityChange,
                quantityBefore = before,
                quantityAfter = after,
                reason = reason
            )
        ) { select() }.decodeSingle()
    }

    suspend fun getStockMovements(productId: Int? = null, limit: Int = 100): List<StockMovement> =
        db.from("stock_movements").select {
            if (productId != null) filter { eq("product_id", productId) }
            order("created_at", ascending = false)
            limit(limit.toLong())
        }.decodeList()
}
