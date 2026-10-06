package com.siledje.mobile.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Role(
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    @SerialName("can_manage_stock") val canManageStock: Boolean = false,
    @SerialName("can_manage_users") val canManageUsers: Boolean = false,
    @SerialName("can_view_reports") val canViewReports: Boolean = false,
    @SerialName("can_process_returns") val canProcessReturns: Boolean = false,
    @SerialName("can_manage_suppliers") val canManageSuppliers: Boolean = false
)

@Serializable
data class AppUser(
    val id: Int = 0,
    val username: String,
    @SerialName("role_id") val roleId: Int? = null,
    @SerialName("full_name") val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    // Jointure lecture (comme get_by_username)
    @SerialName("role_name") val roleName: String? = null
)

@Serializable
data class SupplierOrderItem(
    val id: Int = 0,
    @SerialName("order_id") val orderId: Int = 0,
    @SerialName("product_id") val productId: Int,
    @SerialName("quantity_ordered") val quantityOrdered: Int,
    @SerialName("quantity_received") val quantityReceived: Int = 0,
    @SerialName("unit_price") val unitPrice: Double,
    val notes: String? = null,
    @SerialName("product_name") val productName: String? = null
)

@Serializable
data class SupplierOrder(
    val id: Int = 0,
    @SerialName("supplier_id") val supplierId: Int,
    @SerialName("created_by") val createdBy: Int? = null,
    @SerialName("order_number") val orderNumber: String,
    val status: String = "pending",
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    val notes: String? = null,
    @SerialName("order_date") val orderDate: String? = null,
    @SerialName("expected_date") val expectedDate: String? = null,
    @SerialName("received_date") val receivedDate: String? = null,
    val items: List<SupplierOrderItem> = emptyList()
)
