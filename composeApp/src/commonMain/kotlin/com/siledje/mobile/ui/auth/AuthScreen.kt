package com.siledje.mobile.ui.auth

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.siledje.mobile.ui.components.FilterPill
import com.siledje.mobile.ui.components.FormTextField
import com.siledje.mobile.ui.components.LoadingButton
import com.siledje.mobile.ui.state.AuthController
import com.siledje.mobile.ui.state.AuthValidation
import com.siledje.mobile.ui.theme.LocalHeaderColors
import com.siledje.mobile.ui.theme.semantic

/** Ecran affiche pendant la verification de la session enregistree. */
@Composable
fun SplashScreen() {
    val colors = LocalHeaderColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(colors.start, colors.end))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BrandMark()
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun BrandMark() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
    }
}

/** Connexion et creation de compte entreprise (email, nom d'utilisateur, mot de passe). */
@Composable
fun AuthScreen(auth: AuthController) {
    var signUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    val emailError = AuthValidation.emailError(email)
    val usernameError = AuthValidation.usernameError(username)
    val passwordError = AuthValidation.passwordError(password)
    val confirmError = AuthValidation.confirmError(password, confirm)

    val formValid = if (signUpMode) {
        emailError == null && usernameError == null && passwordError == null && confirmError == null
    } else {
        emailError == null && password.isNotEmpty()
    }

    val submit = {
        submitted = true
        if (formValid) {
            if (signUpMode) auth.signUp(email, username, password) else auth.signIn(email, password)
        }
    }

    val colors = LocalHeaderColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        // Bandeau de marque
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(Brush.linearGradient(listOf(colors.start, colors.end)))
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandMark()
            Spacer(Modifier.height(16.dp))
            Text("Siledje", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(
                "Vos ventes, votre stock et vos fournisseurs, partout avec vous.",
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPill("Connexion", selected = !signUpMode, onClick = {
                    signUpMode = false
                    submitted = false
                    auth.clearMessages()
                })
                FilterPill("Créer un compte", selected = signUpMode, onClick = {
                    signUpMode = true
                    submitted = false
                    auth.clearMessages()
                })
            }

            Text(
                if (signUpMode) "Créez le compte de votre entreprise" else "Heureux de vous revoir",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                if (signUpMode) "Renseignez vos informations pour commencer à gérer votre activité."
                else "Connectez-vous pour accéder à votre activité.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            auth.error?.let { Banner(it, isError = true) }
            auth.info?.let { Banner(it, isError = false) }

            FormTextField(
                value = email,
                onValueChange = { email = it },
                label = "Adresse email",
                error = if (submitted) emailError else null,
                keyboardType = KeyboardType.Email,
                leadingIcon = Icons.Filled.Email
            )

            if (signUpMode) {
                FormTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Nom d'utilisateur",
                    error = if (submitted) usernameError else null,
                    leadingIcon = Icons.Filled.Person
                )
            }

            FormTextField(
                value = password,
                onValueChange = { password = it },
                label = "Mot de passe",
                error = if (submitted && (signUpMode || password.isEmpty())) {
                    if (signUpMode) passwordError else "Saisissez votre mot de passe"
                } else null,
                keyboardType = KeyboardType.Password,
                imeAction = if (signUpMode) ImeAction.Next else ImeAction.Done,
                leadingIcon = Icons.Filled.Lock,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailing = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPassword) "Masquer" else "Afficher"
                        )
                    }
                }
            )

            if (signUpMode) {
                StrengthMeter(AuthValidation.strength(password))
                FormTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = "Répéter le mot de passe",
                    error = if (submitted) confirmError else null,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    leadingIcon = Icons.Filled.Lock,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                )
            }

            Spacer(Modifier.height(4.dp))
            LoadingButton(
                text = if (signUpMode) "Créer mon compte" else "Se connecter",
                onClick = submit,
                loading = auth.busy,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (signUpMode) "Déjà un compte ?" else "Pas encore de compte ?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = {
                    signUpMode = !signUpMode
                    submitted = false
                    auth.clearMessages()
                }) {
                    Text(if (signUpMode) "Se connecter" else "Créer un compte")
                }
            }
        }
    }
}

@Composable
private fun Banner(text: String, isError: Boolean) {
    val tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.semantic.success
    val icon = if (isError) Icons.Filled.Info else Icons.Filled.CheckCircle
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(tint.copy(alpha = 0.12f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun StrengthMeter(level: Int) {
    val semantic = MaterialTheme.semantic
    val color = when (level) {
        1 -> semantic.warning
        2 -> MaterialTheme.colorScheme.primary
        3 -> semantic.success
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val label = when (level) {
        1 -> "Mot de passe faible"
        2 -> "Mot de passe moyen"
        3 -> "Mot de passe fort"
        else -> "Choisissez un mot de passe d'au moins 8 caractères"
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in 1..3) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (i <= level) color else MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
