package com.siledje.mobile.data.repository

import com.siledje.mobile.data.remote.SupabaseClientProvider.db
import com.siledje.mobile.domain.model.PaymentMethod
import com.siledje.mobile.domain.model.Sale
import com.siledje.mobile.domain.model.SaleItem

/**
 * Équivalent mobile de src/database/repositories/sales_repository.py.
 * Côté mobile, l'usage principal (d'après ta demande) est la
 * CONSULTATION de l'historique des ventes par le propriétaire — pas
 * forcément la création de nouvelles ventes, qui reste probablement
 * sur le poste de caisse desktop. Les deux méthodes de lecture
 * ci-dessous couvrent ce besoin ; createSale est incluse pour plus
 * tard si tu veux aussi encaisser depuis le mobile.
 */
class SalesRepository {

    suspend fun getSalesBetween(startIso: String, endIso: String): List<Sale> =
        db.from("sales").select {
            filter {
                gte("sale_date", startIso)
                lte("sale_date", endIso)
            }
            order("sale_date", ascending = false)
        }.decodeList()

    suspend fun getRecentSales(limit: Int = 50): List<Sale> =
        db.from("sales").select {
            order("sale_date", ascending = false)
            limit(limit.toLong())
        }.decodeList()

    suspend fun getSaleById(saleId: Int): Sale =
        db.from("sales").select { filter { eq("id", saleId) } }.decodeSingle()

    suspend fun getSaleItems(saleId: Int): List<SaleItem> =
        db.from("sale_items").select { filter { eq("sale_id", saleId) } }.decodeList()

    suspend fun getPaymentMethods(): List<PaymentMethod> =
        db.from("payment_methods").select {
            filter { eq("is_active", true) }
            order("sort_order")
        }.decodeList()

    suspend fun countSales(): Long =
        db.from("sales").select { count(io.github.jantennert.supabase.postgrest.query.Count.EXACT) }
            .countOrNull() ?: 0L
}
