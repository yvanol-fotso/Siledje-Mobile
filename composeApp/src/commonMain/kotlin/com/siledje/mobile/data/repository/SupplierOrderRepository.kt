package com.siledje.mobile.data.repository

import com.siledje.mobile.data.remote.SupabaseClientProvider.db
import com.siledje.mobile.domain.model.SupplierOrder
import com.siledje.mobile.domain.model.SupplierOrderItem
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Suivi des commandes fournisseurs depuis le téléphone.
 * Clés = uuid (colonne sync_uuid).
 */
class SupplierOrderRepository {

    suspend fun getOrdersForSupplier(supplierId: String): List<SupplierOrder> =
        db.from("supplier_orders").select {
            filter { eq("supplier_id", supplierId) }
            order("order_date", Order.DESCENDING)
        }.decodeList()

    suspend fun getOrderItems(orderId: String): List<SupplierOrderItem> =
        db.from("supplier_order_items").select { filter { eq("order_id", orderId) } }.decodeList()

    suspend fun createOrder(
        supplierId: String,
        createdBy: String?,
        items: List<SupplierOrderItem>,
        notes: String? = null
    ): SupplierOrder {
        val total = items.sumOf { it.quantityOrdered * it.unitPrice }
        val orderNumber = generateOrderNumber()

        val order = db.from("supplier_orders").insert(
            SupplierOrder(
                supplierId = supplierId,
                createdBy = createdBy,
                orderNumber = orderNumber,
                totalAmount = total,
                notes = notes
            )
        ) { select() }.decodeSingle<SupplierOrder>()

        items.forEach { item ->
            db.from("supplier_order_items").insert(item.copy(orderId = order.id))
        }
        return order
    }

    private suspend fun generateOrderNumber(): String {
        val year = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).year
        val count = db.from("supplier_orders").select {
            filter { ilike("order_number", "CMD-$year-%") }
        }.decodeList<SupplierOrder>().size + 1
        return "CMD-$year-${count.toString().padStart(3, '0')}"
    }

    /** receipts : itemId (uuid) -> quantité reçue */
    suspend fun markReceived(orderId: String, receipts: Map<String, Int>) {
        receipts.forEach { (itemId, qty) ->
            db.from("supplier_order_items").update(mapOf("quantity_received" to qty)) {
                filter { eq("sync_uuid", itemId) }
            }
        }
        db.from("supplier_orders").update(mapOf("status" to "delivered")) {
            filter { eq("sync_uuid", orderId) }
        }
    }
}