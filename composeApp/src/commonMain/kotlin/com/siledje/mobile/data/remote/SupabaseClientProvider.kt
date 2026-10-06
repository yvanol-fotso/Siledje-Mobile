package com.siledje.mobile.data.remote

import io.github.jantennert.supabase.createSupabaseClient
import io.github.jantennert.supabase.gotrue.GoTrue
import io.github.jantennert.supabase.postgrest.Postgrest

/**
 * Point d'entrée réseau UNIQUE vers Supabase — même projet que
 * l'app desktop (SUPABASE_URL / SUPABASE_API_KEY dans le .env Python).
 * Ne mets jamais la clé en dur ici en prod : injecte-la via
 * BuildConfig (Android) / Info.plist (iOS) au moment du build.
 *
 * ⚠️ Ton projet Supabase apparaît actuellement "paused" (plan gratuit) —
 * il faudra le réactiver ("Resume project") avant de pouvoir tester
 * une seule requête depuis le mobile, sinon toutes les erreurs réseau
 * vont sembler être des bugs côté app alors qu'elles viennent du
 * projet en pause.
 */
object SupabaseClientProvider {

    private const val SUPABASE_URL = "https://asrldlepgaklekzoihft.supabase.co"
    private const val SUPABASE_ANON_KEY = "REMPLACE_PAR_TA_CLE_ANON_PUBLIQUE"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(GoTrue)
    }

    val db get() = client.postgrest
}
