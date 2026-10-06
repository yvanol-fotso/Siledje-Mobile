package com.siledje.mobile.data.repository

import com.siledje.mobile.data.remote.SupabaseClientProvider.db
import com.siledje.mobile.domain.model.SupplierOrder
import com.siledje.mobile.domain.model.SupplierOrderItem

/**
 * Équivalent mobile de supplier_order_repository.py — permet au
 * propriétaire d'ajouter/suivre des commandes fournisseurs depuis
 * le téléphone.
 */
class SupplierOrderRepository {

    suspend fun getOrdersForSupplier(supplierId: Int): List<SupplierOrder> =
        db.from("supplier_orders").select {
            filter { eq("supplier_id", supplierId) }
            order("order_date", ascending = false)
        }.decodeList()

    suspend fun getOrderItems(orderId: Int): List<SupplierOrderItem> =
        db.from("supplier_order_items").select { filter { eq("order_id", orderId) } }.decodeList()

    suspend fun createOrder(
        supplierId: Int,
        createdBy: Int?,
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
        val year = kotlinx.datetime.Clock.System.now()
            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).year
        val count = db.from("supplier_orders").select {
            filter { ilike("order_number", "CMD-$year-%") }
        }.decodeList<SupplierOrder>().size + 1
        return "CMD-$year-${count.toString().padStart(3, '0')}"
    }

    suspend fun markReceived(orderId: Int, receipts: Map<Int, Int>) {
        receipts.forEach { (itemId, qty) ->
            db.from("supplier_order_items").update(mapOf("quantity_received" to qty)) {
                filter { eq("id", itemId) }
            }
        }
        db.from("supplier_orders").update(mapOf("status" to "delivered")) {
            filter { eq("id", orderId) }
        }
    }
}
