package com.siledje.mobile.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.siledje.mobile.ui.components.ToastState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Liste distante generique (clients, commandes, mouvements...) avec
 * etats de chargement et d'erreur, reutilisee par les ecrans secondaires.
 */
class RemoteList<T>(
    private val scope: CoroutineScope,
    private val toast: ToastState,
    private val fetch: suspend () -> List<T>
) {
    var items by mutableStateOf<List<T>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var loaded by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var job: Job? = null

    fun refresh() {
        job?.cancel()
        job = scope.launch {
            loading = true
            if (!loaded) error = null
            safeCall { fetch() }
                .onSuccess {
                    items = it
                    loaded = true
                    error = null
                }
                .onFailure { e ->
                    if (loaded) toast.error("Actualisation impossible. Vérifiez votre connexion.")
                    else error = e.message ?: "Erreur de chargement"
                }
            loading = false
        }
    }

    /** Charge une seule fois, a l'ouverture de l'ecran. */
    fun ensureLoaded() {
        if (!loaded && !loading) refresh()
    }
}
