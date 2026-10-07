package com.siledje.mobile.data.repository

import com.siledje.mobile.data.remote.SupabaseClientProvider.db
import com.siledje.mobile.domain.model.Product
import com.siledje.mobile.domain.model.StockMovement
import com.siledje.mobile.domain.model.Supplier
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Ce repository ne fait QUE parler à Supabase.
 * Dans la base, les clés sont des uuid (colonne sync_uuid) et les
 * booléens sont des entiers 0/1.
 */
class CatalogRepository {

    // ── PRODUITS ────────────────────────────────────────────────────

    suspend fun getAllProducts(activeOnly: Boolean = true): List<Product> =
        db.from("products").select {
            if (activeOnly) filter { eq("is_active", 1) }
            order("name", Order.ASCENDING)
        }.decodeList()

    suspend fun searchProducts(term: String): List<Product> =
        db.from("products").select {
            filter {
                eq("is_active", 1)
                or {
                    ilike("name", "%$term%")
                    ilike("sku", "%$term%")
                }
            }
            order("name", Order.ASCENDING)
        }.decodeList()

    suspend fun getLowStockProducts(): List<Product> =
        db.from("products").select { filter { eq("is_active", 1) } }
            .decodeList<Product>()
            .filter { it.isLowStock }
            .sortedBy { it.stockQuantity }

    suspend fun createProduct(product: Product): Product =
        db.from("products").insert(product) { select() }.decodeSingle()

    suspend fun updateProduct(id: String, fields: Map<String, Any?>): Boolean {
        db.from("products").update(fields.toJson()) { filter { eq("sync_uuid", id) } }
        return true
    }

    suspend fun setProductActive(id: String, isActive: Boolean) {
        db.from("products").update(mapOf("is_active" to isActive.toInt())) {
            filter { eq("sync_uuid", id) }
        }
    }

    // ── FOURNISSEURS ────────────────────────────────────────────────

    suspend fun getAllSuppliers(activeOnly: Boolean = true): List<Supplier> =
        db.from("suppliers").select {
            if (activeOnly) filter { eq("is_active", 1) }
            order("name", Order.ASCENDING)
        }.decodeList()

    suspend fun createSupplier(supplier: Supplier): Supplier =
        db.from("suppliers").insert(supplier) { select() }.decodeSingle()

    suspend fun updateSupplier(id: String, fields: Map<String, Any?>) {
        db.from("suppliers").update(fields.toJson()) { filter { eq("sync_uuid", id) } }
    }

    suspend fun setSupplierActive(id: String, isActive: Boolean) {
        db.from("suppliers").update(mapOf("is_active" to isActive.toInt())) {
            filter { eq("sync_uuid", id) }
        }
    }

    // ── MOUVEMENTS DE STOCK ─────────────────────────────────────────

    /**
     * L'API REST ne fait pas de transaction multi-tables : on met à jour
     * le stock PUIS on trace le mouvement.
     */
    suspend fun adjustStock(
        productId: String,
        quantityChange: Int,
        movementType: String,
        userId: String?,
        reason: String? = null
    ): StockMovement {
        val product = db.from("products").select { filter { eq("sync_uuid", productId) } }
            .decodeSingle<Product>()
        val after = product.stockQuantity + quantityChange

        db.from("products").update(mapOf("stock_quantity" to after)) {
            filter { eq("sync_uuid", productId) }
        }

        return db.from("stock_movements").insert(
            StockMovement(
                productId = productId,
                userId = userId,
                movementType = movementType,
                quantity = quantityChange,
                reason = reason
            )
        ) { select() }.decodeSingle()
    }

    suspend fun getStockMovements(productId: String? = null, limit: Int = 100): List<StockMovement> =
        db.from("stock_movements").select {
            if (productId != null) filter { eq("product_id", productId) }
            order("created_at", Order.DESCENDING)
            limit(limit.toLong())
        }.decodeList()

    // ── OUTILS INTERNES ─────────────────────────────────────────────

    private fun Boolean.toInt() = if (this) 1 else 0

    private fun Map<String, Any?>.toJson(): JsonObject = buildJsonObject {
        forEach { (key, value) ->
            when (value) {
                null -> put(key, JsonNull)
                is Boolean -> put(key, value.toInt())
                is Number -> put(key, value)
                is String -> put(key, value)
                else -> put(key, value.toString())
            }
        }
    }
}