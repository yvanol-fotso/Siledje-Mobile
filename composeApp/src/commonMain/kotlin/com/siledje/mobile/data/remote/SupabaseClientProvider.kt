package com.siledje.mobile.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

/**
 * Point d'entrée réseau UNIQUE vers Supabase.
 *
 * L'URL et la clé publique ne sont plus figées à la compilation : elles sont
 * saisies au premier lancement (code boutique), enregistrées sur l'appareil
 * (ShopConfigStore), puis passées à configure() avant tout accès réseau.
 * Un seul APK sert donc tous les clients.
 *
 * Ne mets JAMAIS la clé secrète ni service_role dans l'app.
 */
object SupabaseClientProvider {

    private var current: SupabaseClient? = null
    private var currentUrl: String? = null
    private var currentKey: String? = null

    val client: SupabaseClient
        get() = current ?: error("Supabase n'est pas configure : appelez configure() d'abord.")

    /** Cree le client pour cette boutique (sans effet si la configuration n'a pas change). */
    fun configure(url: String, anonKey: String) {
        if (current != null && currentUrl == url && currentKey == anonKey) return
        current = createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = anonKey
        ) {
            install(Postgrest)
            install(Auth)
        }
        currentUrl = url
        currentKey = anonKey
    }

    val db get() = client.postgrest
}
