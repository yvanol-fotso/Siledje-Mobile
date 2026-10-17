package com.siledje.mobile.ui.state

import com.siledje.mobile.core.FeatureFlags
import com.siledje.mobile.ui.components.ToastState
import kotlinx.coroutines.CancellationException

/** Execute un appel reseau et renvoie un Result, sans jamais avaler une annulation de coroutine. */
inline fun <T> safeCall(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

/** Point de passage unique de toutes les ecritures : bloque tant que FeatureFlags.crudEnabled est false. */
object WriteGuard {
    fun run(toast: ToastState, action: () -> Unit) {
        if (!FeatureFlags.crudEnabled) {
            toast.info(FeatureFlags.COMING_SOON_MESSAGE)
            return
        }
        action()
    }
}

/**
 * Message lisible pour l'utilisateur. Le texte brut des erreurs Supabase contient l'URL et les
 * en-tetes de la requete (dont le jeton de connexion) : on ne garde que la premiere ligne,
 * et on ne l'affiche jamais en entier.
 */
fun friendlyNetworkMessage(message: String?): String {
    val m = message ?: return "Une erreur est survenue."
    return when {
        m.contains("resolve host", true) ||
            m.contains("No address associated", true) ||
            m.contains("timeout", true) ||
            m.contains("connect", true) ||
            m.contains("network", true) -> "Impossible de joindre le serveur. Vérifiez votre connexion internet."
        m.contains("Could not find the table", true) ->
            "Cette section n'est pas encore disponible : la table correspondante n'existe pas dans votre base Supabase."
        else -> {
            val firstLine = m.lineSequence().firstOrNull()?.trim().orEmpty()
            if (firstLine.isEmpty()) "Une erreur est survenue." else firstLine.take(140)
        }
    }
}
