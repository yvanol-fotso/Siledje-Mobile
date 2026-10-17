package com.siledje.mobile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.siledje.mobile.ui.theme.semantic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ToastKind { Info, Success, Error }

class ToastMessage(val text: String, val kind: ToastKind)

/** Petit message flottant qui disparait seul (equivalent d'un toast, 100 % multiplateforme). */
class ToastState(private val scope: CoroutineScope) {

    var current by mutableStateOf<ToastMessage?>(null)
        private set

    /** Dernier message affiche : garde le texte visible pendant l'animation de sortie. */
    var last by mutableStateOf<ToastMessage?>(null)
        private set

    private var job: Job? = null

    fun show(text: String, kind: ToastKind = ToastKind.Info) {
        job?.cancel()
        val message = ToastMessage(text, kind)
        current = message
        last = message
        job = scope.launch {
            delay(3200)
            current = null
        }
    }

    fun info(text: String) = show(text, ToastKind.Info)
    fun success(text: String) = show(text, ToastKind.Success)
    fun error(text: String) = show(text, ToastKind.Error)
}

@Composable
fun ToastHost(state: ToastState, modifier: Modifier = Modifier) {
    val message = state.current ?: state.last
    AnimatedVisibility(
        visible = state.current != null,
        modifier = modifier,
        enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 2 },
        exit = fadeOut(tween(160)) + slideOutVertically(tween(180)) { it / 2 }
    ) {
        if (message != null) ToastBubble(message)
    }
}

@Composable
private fun ToastBubble(message: ToastMessage) {
    val semantic = MaterialTheme.semantic
    val icon = when (message.kind) {
        ToastKind.Info -> Icons.Filled.Info
        ToastKind.Success -> Icons.Filled.CheckCircle
        ToastKind.Error -> Icons.Filled.Warning
    }
    val tint = when (message.kind) {
        ToastKind.Info -> MaterialTheme.colorScheme.inversePrimary
        ToastKind.Success -> semantic.success
        ToastKind.Error -> MaterialTheme.colorScheme.error
    }
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.inverseSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            message.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.inverseOnSurface
        )
    }
}
