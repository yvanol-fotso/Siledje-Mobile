package com.siledje.mobile.data.repository

import com.siledje.mobile.data.remote.SupabaseClientProvider
import com.siledje.mobile.data.remote.SupabaseClientProvider.db
import com.siledje.mobile.domain.model.AppUser
import com.siledje.mobile.domain.model.Role
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Authentification (Supabase Auth : email + mot de passe) et profil applicatif
 * (table users du desktop : username, full_name, email, role_id).
 */
class AuthRepository {

    private val authPlugin get() = SupabaseClientProvider.client.auth

    val sessionStatus: StateFlow<SessionStatus> get() = authPlugin.sessionStatus

    suspend fun signIn(email: String, password: String) {
        authPlugin.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * Cree le compte. Retourne true si une session est deja ouverte
     * (confirmation email desactivee cote Supabase), false si l'utilisateur
     * doit d'abord confirmer son adresse.
     */
    suspend fun signUp(email: String, username: String, password: String): Boolean {
        authPlugin.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val hasSession = authPlugin.currentSessionOrNull() != null

        // Ligne de profil dans la table users : meilleur effort, ne bloque jamais l'inscription.
        try {
            db.from("users").insert(
                buildJsonObject {
                    put("username", username)
                    put("full_name", username)
                    put("email", email)
                    put("is_active", 1)
                }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // La table peut exiger d'autres colonnes ou etre protegee : on ignore.
        }
        return hasSession
    }

    suspend fun signOut() {
        authPlugin.signOut()
    }

    fun currentEmail(): String? = authPlugin.currentUserOrNull()?.email

    suspend fun findAppUserByEmail(email: String): AppUser? =
        db.from("users").select { filter { eq("email", email) } }
            .decodeList<AppUser>().firstOrNull()

    suspend fun findRoleName(roleId: String): String? =
        db.from("roles").select { filter { eq("sync_uuid", roleId) } }
            .decodeList<Role>().firstOrNull()?.name
}
