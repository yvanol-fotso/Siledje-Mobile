package com.siledje.mobile.data.remote

import com.siledje.mobile.BuildKonfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

/**
 * Point d'entrée réseau UNIQUE vers Supabase.
 * L'URL et la clé publique viennent de local.properties (hors Git),
 * injectées au build par BuildKonfig.
 * Ne mets JAMAIS la clé service_role dans l'app.
 */
object SupabaseClientProvider {

    val client = createSupabaseClient(
        supabaseUrl = BuildKonfig.SUPABASE_URL,
        supabaseKey = BuildKonfig.SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Auth)
    }

    val db get() = client.postgrest
}