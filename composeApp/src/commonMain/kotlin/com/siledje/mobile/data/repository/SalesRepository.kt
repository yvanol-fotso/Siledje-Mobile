package com.siledje.mobile.data.repository

import com.siledje.mobile.data.remote.SupabaseClientProvider.db
import com.siledje.mobile.domain.model.PaymentMethod
import com.siledje.mobile.domain.model.Sale
import com.siledje.mobile.domain.model.SaleItem
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order

/**
 * Usage principal côté mobile : CONSULTER l'historique des ventes.
 * Clés = uuid (colonne sync_uuid), booléens = entiers 0/1.
 */
class SalesRepository {

    suspend fun getSalesBetween(startIso: String, endIso: String): List<Sale> =
        db.from("sales").select {
            filter {
                gte("sale_date", startIso)
                lte("sale_date", endIso)
            }
            order("sale_date", Order.DESCENDING)
        }.decodeList()

    suspend fun getRecentSales(limit: Int = 50): List<Sale> =
        db.from("sales").select {
            order("sale_date", Order.DESCENDING)
            limit(limit.toLong())
        }.decodeList()

    suspend fun getSaleById(saleId: String): Sale =
        db.from("sales").select { filter { eq("sync_uuid", saleId) } }.decodeSingle()

    suspend fun getSaleItems(saleId: String): List<SaleItem> =
        db.from("sale_items").select { filter { eq("sale_id", saleId) } }.decodeList()

    suspend fun getPaymentMethods(): List<PaymentMethod> =
        db.from("payment_methods").select {
            filter { eq("is_active", 1) }
            order("name", Order.ASCENDING)
        }.decodeList()

    suspend fun countSales(): Long =
        db.from("sales").select { count(Count.EXACT) }
            .countOrNull() ?: 0L
}