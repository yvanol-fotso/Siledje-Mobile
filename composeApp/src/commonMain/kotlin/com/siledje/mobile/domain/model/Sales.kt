package com.siledje.mobile.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Client(
    @SerialName("sync_uuid") val id: String = "",
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    @SerialName("loyalty_points") val loyaltyPoints: Int = 0,
    @SerialName("total_spent") val totalSpent: Double = 0.0,
    val notes: String? = null,
    @Serializable(with = FlexBooleanSerializer::class)
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class PaymentMethod(
    @SerialName("sync_uuid") val id: String = "",
    val name: String,
    val icon: String? = null,
    @Serializable(with = FlexBooleanSerializer::class)
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class SaleItem(
    @SerialName("sync_uuid") val id: String = "",
    @SerialName("sale_id") val saleId: String = "",
    @SerialName("product_id") val productId: String = "",
    val quantity: Int = 0,
    @SerialName("unit_price") val unitPrice: Double = 0.0,
    val discount: Double = 0.0,
    @SerialName("total_price") val totalPrice: Double = 0.0,
    @SerialName("product_name_snap") val productNameSnap: String? = null
)

@Serializable
data class Sale(
    @SerialName("sync_uuid") val id: String = "",
    @SerialName("invoice_number") val invoiceNumber: String = "",
    @Serializable(with = FlexStringSerializer::class)
    @SerialName("user_id") val userId: String? = null,
    @SerialName("client_id") val clientId: String? = null,
    val subtotal: Double = 0.0,
    @SerialName("tax_amount") val taxAmount: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    val status: String = "completed",
    val notes: String? = null,
    @SerialName("sale_date") val saleDate: String? = null,
    // Jointures lecture
    @SerialName("client_name") val clientName: String? = null,
    @SerialName("payment_method_name") val paymentMethodName: String? = null,
    val items: List<SaleItem> = emptyList()
)