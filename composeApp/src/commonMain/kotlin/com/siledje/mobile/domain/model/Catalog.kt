package com.siledje.mobile.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * La base vient du desktop : les booléens y sont des entiers 0/1.
 * Ce sérialiseur accepte 0/1 ET true/false en lecture, et écrit 0/1.
 */
object FlexBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexBoolean", PrimitiveKind.BOOLEAN)

    override fun deserialize(decoder: Decoder): Boolean {
        val p = (decoder as JsonDecoder).decodeJsonElement().jsonPrimitive
        return p.booleanOrNull ?: ((p.intOrNull ?: 0) != 0)
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeInt(if (value) 1 else 0)
    }
}

/** Lit n'importe quel identifiant (uuid, nombre...) comme une String. */
object FlexStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String =
        (decoder as JsonDecoder).decodeJsonElement().jsonPrimitive.content

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}

@Serializable
data class Category(
    @SerialName("sync_uuid") val id: String = "",
    val name: String,
    @SerialName("parent_id") val parentId: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val color: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @Serializable(with = FlexBooleanSerializer::class)
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class Supplier(
    @SerialName("sync_uuid") val id: String = "",
    val name: String,
    @SerialName("contact_name") val contactName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val phone2: String? = null,
    val address: String? = null,
    val city: String? = null,
    @SerialName("payment_terms") val paymentTerms: String? = null,
    val notes: String? = null,
    @Serializable(with = FlexBooleanSerializer::class)
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class Product(
    @SerialName("sync_uuid") val id: String = "",
    val name: String,
    val description: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("supplier_id") val supplierId: String? = null,
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
    @Serializable(with = FlexBooleanSerializer::class)
    @SerialName("is_active") val isActive: Boolean = true,
    @Serializable(with = FlexBooleanSerializer::class)
    @SerialName("is_book") val isBook: Boolean = false,
    val notes: String? = null,
    // Champs de jointure (lecture uniquement)
    @SerialName("category_name") val categoryName: String? = null,
    @SerialName("supplier_name") val supplierName: String? = null
) {
    val isLowStock: Boolean get() = stockQuantity <= minStockThreshold
}

@Serializable
data class Barcode(
    @SerialName("sync_uuid") val id: String = "",
    @SerialName("barcode_text") val barcodeText: String,
    @SerialName("product_id") val productId: String,
    @SerialName("barcode_type") val barcodeType: String = "internal",
    @Serializable(with = FlexBooleanSerializer::class)
    @SerialName("is_primary") val isPrimary: Boolean = false
)

@Serializable
data class StockMovement(
    @SerialName("sync_uuid") val id: String = "",
    @SerialName("product_id") val productId: String,
    @Serializable(with = FlexStringSerializer::class)
    @SerialName("user_id") val userId: String? = null,
    @SerialName("movement_type") val movementType: String,
    val quantity: Int,
    @SerialName("quantity_before") val quantityBefore: Int? = null,
    @SerialName("quantity_after") val quantityAfter: Int? = null,
    @SerialName("unit_cost") val unitCost: Double? = null,
    val reason: String? = null,
    @Serializable(with = FlexStringSerializer::class)
    @SerialName("reference_id") val referenceId: String? = null,
    @SerialName("reference_type") val referenceType: String? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    // Jointure lecture
    @SerialName("product_name") val productName: String? = null
)