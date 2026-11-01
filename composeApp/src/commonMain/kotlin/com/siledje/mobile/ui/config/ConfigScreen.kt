package com.siledje.mobile.ui.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.siledje.mobile.core.ShopConfig
import com.siledje.mobile.core.ShopConfigCodec
import com.siledje.mobile.ui.components.FormTextField
import com.siledje.mobile.ui.components.LoadingButton
import com.siledje.mobile.ui.theme.LocalHeaderColors

/**
 * Premier lancement : l'utilisateur colle le "code boutique" remis par Siledje
 * (ou saisit l'URL et la cle publique a la main). Aucune recompilation par client.
 */
@Composable
fun ConfigScreen(onConfigured: (ShopConfig) -> Unit) {
    var code by remember { mutableStateOf("") }
    var manual by remember { mutableStateOf(false) }
    var url by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current
    val colors = LocalHeaderColors.current

    val submit = {
        if (manual) {
            val cleanUrl = ShopConfigCodec.normalizeUrl(url)
            val message = ShopConfigCodec.validate(cleanUrl, key)
            if (message == null) {
                error = null
                onConfigured(ShopConfig(cleanUrl, key.trim()))
            } else {
                error = message
            }
        } else {
            val result = ShopConfigCodec.parseCode(code)
            val config = result.config
            if (config != null) {
                error = null
                onConfigured(config)
            } else {
                error = result.error ?: "Code boutique invalide."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(Brush.linearGradient(listOf(colors.start, colors.end)))
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Siledje", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(
                "Connectez l'application à votre boutique.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Première configuration", style = MaterialTheme.typography.titleLarge)
            Text(
                if (manual) "Saisissez l'adresse du projet et la clé publique remises par Siledje."
                else "Collez le code boutique que Siledje vous a envoyé (par exemple par WhatsApp). Il commence par SLD1-.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            error?.let { message ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            if (manual) {
                FormTextField(
                    value = url,
                    onValueChange = { url = it; error = null },
                    label = "Adresse du projet (https://xxxx.supabase.co)",
                    keyboardType = KeyboardType.Uri,
                    leadingIcon = Icons.Filled.Storefront
                )
                FormTextField(
                    value = key,
                    onValueChange = { key = it; error = null },
                    label = "Clé publique (sb_publishable_...)",
                    imeAction = ImeAction.Done,
                    leadingIcon = Icons.Filled.Lock
                )
            } else {
                FormTextField(
                    value = code,
                    onValueChange = { code = it; error = null },
                    label = "Code boutique",
                    singleLine = false,
                    minLines = 3,
                    imeAction = ImeAction.Done
                )
                OutlinedButton(
                    onClick = {
                        val pasted = clipboard.getText()?.text.orEmpty()
                        if (pasted.isNotBlank()) {
                            code = pasted
                            error = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Coller le code")
                }
            }

            Spacer(Modifier.height(4.dp))
            LoadingButton(
                text = "Connecter ma boutique",
                onClick = submit,
                enabled = if (manual) url.isNotBlank() && key.isNotBlank() else code.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = { manual = !manual; error = null },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(if (manual) "Utiliser le code boutique" else "Saisie manuelle")
            }

            Text(
                "Le code ne contient que la clé publique de votre boutique. Besoin d'aide : support@siledje.cm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
