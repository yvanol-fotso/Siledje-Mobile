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

/** Message lisible pour l'utilisateur a partir d'une exception reseau. */
fun friendlyNetworkMessage(message: String?): String {
    val m = message ?: return "Une erreur est survenue."
    return when {
        m.contains("resolve host", true) ||
            m.contains("No address associated", true) ||
            m.contains("timeout", true) ||
            m.contains("connect", true) ||
            m.contains("network", true) -> "Impossible de joindre le serveur. Vérifiez votre connexion internet."
        else -> m
    }
}
