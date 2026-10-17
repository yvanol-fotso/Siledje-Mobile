package com.siledje.mobile.ui.stock

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
import androidx.compose.material3.MaterialTheme
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
import com.siledje.mobile.ui.components.SelectField
import com.siledje.mobile.ui.components.SelectOption
import com.siledje.mobile.ui.components.formatFcfa
import com.siledje.mobile.ui.state.CatalogStore
import com.siledje.mobile.ui.state.ProductInput

private fun parseMoney(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }

private fun cleanMoney(text: String): String =
    text.filter { it.isDigit() || it == '.' || it == ',' }

/** Formulaire de creation / modification d'un produit (la validation est complete, l'ecriture est controlee par FeatureFlags). */
@Composable
fun ProductFormScreen(catalog: CatalogStore, onClose: () -> Unit) {
    val editing = catalog.formProduct

    var name by remember { mutableStateOf(editing?.name ?: "") }
    var sku by remember { mutableStateOf(editing?.sku ?: "") }
    var description by remember { mutableStateOf(editing?.description ?: "") }
    var categoryId by remember { mutableStateOf(editing?.categoryId) }
    var supplierId by remember { mutableStateOf(editing?.supplierId) }
    var location by remember { mutableStateOf(editing?.location ?: "") }
    var buyText by remember { mutableStateOf(editing?.let { trimNumber(it.buyPrice) } ?: "") }
    var sellText by remember { mutableStateOf(editing?.let { trimNumber(it.sellPrice) } ?: "") }
    var qtyText by remember { mutableStateOf(editing?.stockQuantity?.toString() ?: "0") }
    var thresholdText by remember { mutableStateOf(editing?.minStockThreshold?.toString() ?: "10") }
    var submitted by remember { mutableStateOf(false) }

    val buy = parseMoney(buyText)
    val sell = parseMoney(sellText)
    val qty = qtyText.toIntOrNull()?.takeIf { it >= 0 }
    val threshold = thresholdText.toIntOrNull()?.takeIf { it >= 0 }

    val nameError = if (name.isBlank()) "Le nom est obligatoire" else null
    val buyError = if (buyText.isNotBlank() && buy == null) "Montant invalide" else null
    val sellError = if (sell == null) "Le prix de vente est obligatoire" else null
    val qtyError = if (editing == null && qty == null) "Quantité invalide" else null
    val thresholdError = if (threshold == null) "Seuil invalide" else null
    val valid = listOf(nameError, buyError, sellError, qtyError, thresholdError).all { it == null }

    val categoryOptions = listOf(SelectOption(null, "Aucune")) +
        catalog.categories.map { SelectOption(it.id, it.name) }
    val supplierOptions = listOf(SelectOption(null, "Aucun")) +
        catalog.suppliers.map { SelectOption(it.id, it.name) }

    val save = {
        submitted = true
        if (valid) {
            catalog.saveProduct(
                input = ProductInput(
                    name = name,
                    description = description,
                    categoryId = categoryId,
                    supplierId = supplierId,
                    buyPrice = buy ?: 0.0,
                    sellPrice = sell ?: 0.0,
                    stockQuantity = qty ?: (editing?.stockQuantity ?: 0),
                    minStockThreshold = threshold ?: 10,
                    sku = sku,
                    location = location
                ),
                existing = editing,
                onDone = onClose
            )
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = if (editing == null) "Nouveau produit" else "Modifier le produit",
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
            SectionCard("Informations") {
                FormTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nom du produit",
                    error = if (submitted) nameError else null
                )
                FormTextField(value = sku, onValueChange = { sku = it }, label = "Référence (SKU)")
                FormTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    singleLine = false,
                    minLines = 2
                )
            }

            SectionCard("Classement") {
                SelectField(
                    label = "Catégorie",
                    valueLabel = catalog.categoryNames[categoryId] ?: "Aucune",
                    options = categoryOptions,
                    onSelect = { categoryId = it }
                )
                SelectField(
                    label = "Fournisseur",
                    valueLabel = catalog.supplierNames[supplierId] ?: "Aucun",
                    options = supplierOptions,
                    onSelect = { supplierId = it }
                )
                FormTextField(value = location, onValueChange = { location = it }, label = "Emplacement en rayon")
            }

            SectionCard("Prix et stock") {
                FormTextField(
                    value = buyText,
                    onValueChange = { buyText = cleanMoney(it) },
                    label = "Prix d'achat (FCFA)",
                    keyboardType = KeyboardType.Decimal,
                    error = if (submitted) buyError else null
                )
                FormTextField(
                    value = sellText,
                    onValueChange = { sellText = cleanMoney(it) },
                    label = "Prix de vente (FCFA)",
                    keyboardType = KeyboardType.Decimal,
                    error = if (submitted) sellError else null
                )
                if (buy != null && sell != null && sell > 0) {
                    val margin = sell - buy
                    val percent = (margin / sell * 100).toInt()
                    Text(
                        "Marge : ${formatFcfa(margin)} ($percent %)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (editing == null) {
                    FormTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it.filter { c -> c.isDigit() } },
                        label = "Quantité initiale",
                        keyboardType = KeyboardType.Number,
                        error = if (submitted) qtyError else null
                    )
                } else {
                    Text(
                        "Pour modifier la quantité, utilisez « Ajuster le stock » sur la fiche du produit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FormTextField(
                    value = thresholdText,
                    onValueChange = { thresholdText = it.filter { c -> c.isDigit() } },
                    label = "Seuil d'alerte de stock",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    error = if (submitted) thresholdError else null
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
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.weight(1f).height(52.dp)
            ) { Text("Annuler") }
            LoadingButton(
                text = "Enregistrer",
                onClick = save,
                loading = catalog.saving,
                icon = Icons.Filled.Check,
                modifier = Modifier.weight(1.4f)
            )
        }
    }
}

private fun trimNumber(value: Double): String =
    if (value == kotlin.math.floor(value)) value.toLong().toString() else value.toString()
