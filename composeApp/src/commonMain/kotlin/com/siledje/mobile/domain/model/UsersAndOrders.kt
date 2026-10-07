package com.siledje.mobile.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Role(
    @SerialName("sync_uuid") val id: String = "",
    val name: String,
    val description: String? = null,
    @Serializable(with = FlexBooleanSerializer::class)
    @SerialName("can_manage_stock") val canManageStock: Boolean = false,
    @Serializable(with = FlexBooleanSerializer::class)
    @SerialName("can_manage_users") val canManageUsers: Boolean = false,
    @Serializable(with = FlexBooleanSerializer::class)
    @SerialName("can_view_reports") val canViewReports: Boolean = false,
    @Serializable(with = FlexBooleanSerializer::class)
    @SerialName("can_process_returns") val canProcessReturns: Boolean = false,
    @Serializable(with = FlexBooleanSerializer::class)
    @SerialName("can_manage_suppliers") val canManageSuppliers: Boolean = false
)

@Serializable
data class AppUser(
    @SerialName("sync_uuid") val id: String = "",
    val username: String,
    @SerialName("role_id") val roleId: String? = null,
    @SerialName("full_name") val fullName: String = "",
    val email: String? = null,
    val phone: String? = null,
    @Serializable(with = FlexBooleanSerializer::class)
    @SerialName("is_active") val isActive: Boolean = true,
    // Jointure lecture
    @SerialName("role_name") val roleName: String? = null
)

@Serializable
data class SupplierOrderItem(
    @SerialName("sync_uuid") val id: String = "",
    @SerialName("order_id") val orderId: String = "",
    @SerialName("product_id") val productId: String = "",
    @SerialName("quantity_ordered") val quantityOrdered: Int = 0,
    @SerialName("quantity_received") val quantityReceived: Int = 0,
    @SerialName("unit_price") val unitPrice: Double = 0.0,
    val notes: String? = null,
    @SerialName("product_name") val productName: String? = null
)

@Serializable
data class SupplierOrder(
    @SerialName("sync_uuid") val id: String = "",
    @SerialName("supplier_id") val supplierId: String = "",
    @Serializable(with = FlexStringSerializer::class)
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("order_number") val orderNumber: String = "",
    val status: String = "pending",
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    val notes: String? = null,
    @SerialName("order_date") val orderDate: String? = null,
    @SerialName("expected_date") val expectedDate: String? = null,
    @SerialName("received_date") val receivedDate: String? = null,
    val items: List<SupplierOrderItem> = emptyList()
)