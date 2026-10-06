package com.siledje.mobile.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Client(
    val id: Int = 0,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    @SerialName("loyalty_points") val loyaltyPoints: Int = 0,
    @SerialName("total_spent") val totalSpent: Double = 0.0,
    val notes: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class PaymentMethod(
    val id: Int = 0,
    val name: String,
    val icon: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class SaleItem(
    val id: Int = 0,
    @SerialName("sale_id") val saleId: Int = 0,
    @SerialName("product_id") val productId: Int,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
    val discount: Double = 0.0,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("product_name_snap") val productNameSnap: String? = null
)

@Serializable
data class Sale(
    val id: Int = 0,
    @SerialName("invoice_number") val invoiceNumber: String,
    @SerialName("user_id") val userId: Int,
    @SerialName("client_id") val clientId: Int? = null,
    val subtotal: Double = 0.0,
    @SerialName("tax_amount") val taxAmount: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    val status: String = "completed",
    val notes: String? = null,
    @SerialName("sale_date") val saleDate: String? = null,
    // Jointures lecture (comme get_sale_by_id / get_sales_between)
    @SerialName("client_name") val clientName: String? = null,
    @SerialName("payment_method_name") val paymentMethodName: String? = null,
    val items: List<SaleItem> = emptyList()
)
