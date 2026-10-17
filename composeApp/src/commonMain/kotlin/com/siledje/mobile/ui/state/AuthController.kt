package com.siledje.mobile.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.siledje.mobile.data.repository.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class AuthPhase { Loading, SignedOut, SignedIn }

data class UserProfile(
    val userId: String?,
    val username: String,
    val email: String?,
    val roleName: String?
)

/** Regles de validation des formulaires de connexion et d'inscription. */
object AuthValidation {
    fun emailError(value: String): String? {
        val v = value.trim()
        val at = v.indexOf('@')
        val validShape = at > 0 && v.indexOf('.', at) > at + 1 && !v.endsWith(".") && !v.contains(' ')
        return if (validShape) null else "Adresse email invalide"
    }

    fun usernameError(value: String): String? {
        val v = value.trim()
        return when {
            v.length < 3 -> "Au moins 3 caractères"
            v.contains(' ') -> "Sans espace"
            else -> null
        }
    }

    fun passwordError(value: String): String? =
        if (value.length < 8) "Au moins 8 caractères" else null

    fun confirmError(password: String, confirm: String): String? =
        if (password != confirm) "Les mots de passe ne correspondent pas" else null

    /** 0 = vide, 1 = faible, 2 = moyen, 3 = fort. */
    fun strength(password: String): Int {
        if (password.isEmpty()) return 0
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isDigit() } && password.any { it.isLetter() }) score++
        if (password.length >= 12 || password.any { !it.isLetterOrDigit() }) score++
        return score.coerceAtLeast(1)
    }
}

fun friendlyAuthError(message: String?): String {
    val m = message ?: return "Une erreur est survenue. Réessayez."
    return when {
        m.contains("Invalid login", true) || m.contains("invalid_credentials", true) ->
            "Email ou mot de passe incorrect."
        m.contains("already registered", true) || m.contains("already been registered", true) ||
            m.contains("user_already_exists", true) ->
            "Un compte existe déjà avec cet email."
        m.contains("Email not confirmed", true) ->
            "Confirmez d'abord votre adresse email, puis reconnectez-vous."
        m.contains("rate limit", true) || m.contains("too many", true) ->
            "Trop de tentatives. Patientez quelques minutes."
        m.contains("weak", true) || m.contains("at least", true) ->
            "Mot de passe trop faible."
        else -> friendlyNetworkMessage(m)
    }
}

/** Etat de connexion de l'application : suit la session Supabase et charge le profil. */
class AuthController(
    private val repo: AuthRepository,
    private val scope: CoroutineScope
) {
    var phase by mutableStateOf(AuthPhase.Loading)
        private set
    var profile by mutableStateOf<UserProfile?>(null)
        private set
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var info by mutableStateOf<String?>(null)
        private set

    private var pendingUsername: String? = null

    init {
        scope.launch {
            repo.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        phase = AuthPhase.SignedIn
                        loadProfile()
                    }
                    is SessionStatus.Initializing -> phase = AuthPhase.Loading
                    else -> {
                        phase = AuthPhase.SignedOut
                        profile = null
                    }
                }
            }
        }
    }

    private suspend fun loadProfile() {
        val email = repo.currentEmail()
        val appUser = if (email != null) safeCall { repo.findAppUserByEmail(email) }.getOrNull() else null
        val roleName = appUser?.roleId?.takeIf { it.isNotBlank() }
            ?.let { id -> safeCall { repo.findRoleName(id) }.getOrNull() }
        profile = UserProfile(
            userId = appUser?.id?.takeIf { it.isNotBlank() },
            username = appUser?.username
                ?: pendingUsername
                ?: email?.substringBefore('@')
                ?: "Utilisateur",
            email = email,
            roleName = roleName
        )
    }

    fun clearMessages() {
        error = null
        info = null
    }

    fun signIn(email: String, password: String) {
        if (busy) return
        scope.launch {
            busy = true
            clearMessages()
            safeCall { repo.signIn(email.trim(), password) }
                .onFailure { error = friendlyAuthError(it.message) }
            busy = false
        }
    }

    fun signUp(email: String, username: String, password: String) {
        if (busy) return
        scope.launch {
            busy = true
            clearMessages()
            pendingUsername = username.trim()
            safeCall { repo.signUp(email.trim(), username.trim(), password) }
                .onSuccess { hasSession ->
                    if (!hasSession) {
                        info = "Compte créé. Confirmez votre adresse email via le message reçu, puis connectez-vous."
                    }
                }
                .onFailure { error = friendlyAuthError(it.message) }
            busy = false
        }
    }

    fun signOut() {
        scope.launch {
            safeCall { repo.signOut() }
        }
    }
}
