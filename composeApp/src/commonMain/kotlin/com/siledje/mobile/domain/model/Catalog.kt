package com.siledje.mobile.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Int = 0,
    val name: String,
    @SerialName("parent_id") val parentId: Int? = null,
    val description: String? = null,
    val icon: String? = null,
    val color: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class Supplier(
    val id: Int = 0,
    val name: String,
    @SerialName("contact_name") val contactName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val phone2: String? = null,
    val address: String? = null,
    val city: String? = null,
    @SerialName("payment_terms") val paymentTerms: String? = null,
    val notes: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class Product(
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    @SerialName("category_id") val categoryId: Int? = null,
    @SerialName("supplier_id") val supplierId: Int? = null,
    @SerialName("buy_price") val buyPrice: Double = 0.0,
    @SerialName("sell_price") val sellPrice: Double = 0.0,
    @SerialName("stock_quantity") val stockQuantity: Int = 0,
    @SerialName("min_stock_threshold") val minStockThreshold: Int = 10,
    @SerialName("packaging_type") val packagingType: String = "unitaire",
    @SerialName("units_per_pack") val unitsPerPack: Int = 1,
    val location: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    val sku: String? = null,
    @SerialName("tax_rate") val taxRate: Double = 0.0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("is_book") val isBook: Boolean = false,
    val notes: String? = null,
    // Champs de jointure côté lecture uniquement (comme dans CatalogRepository.get_all_products)
    @SerialName("category_name") val categoryName: String? = null,
    @SerialName("supplier_name") val supplierName: String? = null
) {
    val isLowStock: Boolean get() = stockQuantity <= minStockThreshold
}

@Serializable
data class Barcode(
    val id: Int = 0,
    @SerialName("barcode_text") val barcodeText: String,
    @SerialName("product_id") val productId: Int,
    @SerialName("barcode_type") val barcodeType: String = "internal",
    @SerialName("is_primary") val isPrimary: Boolean = false
)

@Serializable
data class StockMovement(
    val id: Int = 0,
    @SerialName("product_id") val productId: Int,
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("movement_type") val movementType: String,
    val quantity: Int,
    @SerialName("quantity_before") val quantityBefore: Int,
    @SerialName("quantity_after") val quantityAfter: Int,
    @SerialName("unit_cost") val unitCost: Double? = null,
    val reason: String? = null,
    @SerialName("reference_id") val referenceId: Int? = null,
    @SerialName("reference_type") val referenceType: String? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    // Jointure lecture (comme get_stock_movements)
    @SerialName("product_name") val productName: String? = null
)
