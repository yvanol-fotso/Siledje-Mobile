package com.siledje.mobile.ui.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category as CategoryIcon
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siledje.mobile.domain.model.Category
import com.siledje.mobile.ui.components.ConfirmDialog
import com.siledje.mobile.ui.components.EmptyState
import com.siledje.mobile.ui.components.ErrorState
import com.siledje.mobile.ui.components.FormTextField
import com.siledje.mobile.ui.components.IconBadge
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.SkeletonList
import com.siledje.mobile.ui.components.StatusChip
import com.siledje.mobile.ui.state.CatalogStore
import com.siledje.mobile.ui.state.CategoryInput
import com.siledje.mobile.ui.state.UNCATEGORIZED_LABEL

/** Categories de produits : nombre de produits, creation, modification et desactivation. */
@Composable
fun CategoriesScreen(
    catalog: CatalogStore,
    onOpenCategory: (Category) -> Unit,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    var dialogCategory by remember { mutableStateOf<Category?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var toDeactivate by remember { mutableStateOf<Category?>(null) }
    val uncategorized = catalog.products.count { catalog.categoryLabel(it) == UNCATEGORIZED_LABEL }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "Catégories",
                subtitle = if (catalog.loaded) "${catalog.categories.size} catégorie" + (if (catalog.categories.size > 1) "s" else "") else "Chargement",
                onBack = onBack,
                onRefresh = onRefresh
            )
            when {
                !catalog.loaded && catalog.error != null -> ErrorState(message = catalog.error ?: "", onRetry = onRefresh)
                !catalog.loaded -> SkeletonList()
                catalog.categories.isEmpty() && uncategorized == 0 -> EmptyState(
                    icon = Icons.Filled.CategoryIcon,
                    title = "Aucune catégorie",
                    message = "Créez une catégorie pour organiser vos produits."
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(catalog.categories) { category ->
                        CategoryCard(
                            category = category,
                            count = catalog.productCountForCategory(category.id),
                            onClick = { onOpenCategory(category) },
                            onEdit = {
                                dialogCategory = category
                                showDialog = true
                            },
                            onDelete = { toDeactivate = category }
                        )
                    }
                    if (uncategorized > 0) {
                        item {
                            SiledjeCard {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    IconBadge(Icons.Filled.CategoryIcon, MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(UNCATEGORIZED_LABEL, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            "Produits à classer",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    StatusChip(uncategorized.toString(), MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (catalog.loaded) {
            ExtendedFloatingActionButton(
                onClick = {
                    dialogCategory = null
                    showDialog = true
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Ajouter une catégorie") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
    }

    if (showDialog) {
        CategoryDialog(
            existing = dialogCategory,
            saving = catalog.saving,
            onDismiss = { showDialog = false },
            onSave = { input ->
                catalog.saveCategory(input, dialogCategory, onDone = { showDialog = false })
            }
        )
    }

    toDeactivate?.let { category ->
        ConfirmDialog(
            title = "Désactiver cette catégorie ?",
            message = "La catégorie « ${category.name} » n'apparaîtra plus. Les produits sont conservés.",
            confirmText = "Désactiver",
            destructive = true,
            onConfirm = {
                toDeactivate = null
                catalog.deactivateCategory(category)
            },
            onDismiss = { toDeactivate = null }
        )
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    count: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    SiledjeCard(onClick = onClick) {
        Row(Modifier.padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Filled.CategoryIcon)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    category.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    category.description?.takeIf { it.isNotBlank() } ?: "$count produit" + (if (count > 1) "s" else ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StatusChip(count.toString(), MaterialTheme.colorScheme.primary)
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Modifier", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Désactiver", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CategoryDialog(
    existing: Category?,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (CategoryInput) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var submitted by remember { mutableStateOf(false) }
    val nameError = if (name.isBlank()) "Le nom est obligatoire" else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Nouvelle catégorie" else "Modifier la catégorie") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FormTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nom de la catégorie",
                    error = if (submitted) nameError else null
                )
                FormTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description (facultatif)"
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving,
                onClick = {
                    submitted = true
                    if (nameError == null) onSave(CategoryInput(name, description))
                }
            ) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
