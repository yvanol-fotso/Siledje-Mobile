package com.siledje.mobile.ui.fournisseurs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.siledje.mobile.ui.components.FormTextField
import com.siledje.mobile.ui.components.LoadingButton
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SectionCard
import com.siledje.mobile.ui.state.AuthValidation
import com.siledje.mobile.ui.state.SupplierInput
import com.siledje.mobile.ui.state.SupplierStore

/** Formulaire de creation / modification d'un fournisseur (ecriture controlee par FeatureFlags). */
@Composable
fun SupplierFormScreen(store: SupplierStore, onClose: () -> Unit) {
    val editing = store.formSupplier

    var name by remember { mutableStateOf(editing?.name ?: "") }
    var contact by remember { mutableStateOf(editing?.contactName ?: "") }
    var phone by remember { mutableStateOf(editing?.phone ?: "") }
    var phone2 by remember { mutableStateOf(editing?.phone2 ?: "") }
    var email by remember { mutableStateOf(editing?.email ?: "") }
    var address by remember { mutableStateOf(editing?.address ?: "") }
    var city by remember { mutableStateOf(editing?.city ?: "") }
    var terms by remember { mutableStateOf(editing?.paymentTerms ?: "") }
    var notes by remember { mutableStateOf(editing?.notes ?: "") }
    var submitted by remember { mutableStateOf(false) }

    val nameError = if (name.isBlank()) "Le nom est obligatoire" else null
    val emailError = if (email.isNotBlank()) AuthValidation.emailError(email) else null
    val valid = nameError == null && emailError == null

    val save = {
        submitted = true
        if (valid) {
            store.saveSupplier(
                input = SupplierInput(
                    name = name,
                    contactName = contact,
                    email = email,
                    phone = phone,
                    phone2 = phone2,
                    address = address,
                    city = city,
                    paymentTerms = terms,
                    notes = notes
                ),
                existing = editing,
                onDone = onClose
            )
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = if (editing == null) "Nouveau fournisseur" else "Modifier le fournisseur",
            subtitle = editing?.name,
            onBack = onClose
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard("Entreprise") {
                FormTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nom du fournisseur",
                    error = if (submitted) nameError else null
                )
                FormTextField(value = contact, onValueChange = { contact = it }, label = "Personne à contacter")
            }

            SectionCard("Coordonnées") {
                FormTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Téléphone",
                    keyboardType = KeyboardType.Phone
                )
                FormTextField(
                    value = phone2,
                    onValueChange = { phone2 = it },
                    label = "Téléphone 2",
                    keyboardType = KeyboardType.Phone
                )
                FormTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    keyboardType = KeyboardType.Email,
                    error = if (submitted) emailError else null
                )
                FormTextField(value = address, onValueChange = { address = it }, label = "Adresse")
                FormTextField(value = city, onValueChange = { city = it }, label = "Ville")
            }

            SectionCard("Conditions") {
                FormTextField(
                    value = terms,
                    onValueChange = { terms = it },
                    label = "Conditions de paiement"
                )
                FormTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes",
                    singleLine = false,
                    minLines = 3,
                    imeAction = ImeAction.Done
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f).height(52.dp)) {
                Text("Annuler")
            }
            LoadingButton(
                text = "Enregistrer",
                onClick = save,
                loading = store.saving,
                icon = Icons.Filled.Check,
                modifier = Modifier.weight(1.4f)
            )
        }
    }
}
