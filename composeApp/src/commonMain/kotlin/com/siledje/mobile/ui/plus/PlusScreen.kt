package com.siledje.mobile.ui.plus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siledje.mobile.ui.components.FilterPill
import com.siledje.mobile.ui.components.InfoRow
import com.siledje.mobile.ui.components.InitialAvatar
import com.siledje.mobile.ui.components.ListDivider
import com.siledje.mobile.ui.components.MenuRow
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SectionCard
import com.siledje.mobile.ui.components.SectionTitle
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.StatusChip
import com.siledje.mobile.ui.navigation.Routes
import com.siledje.mobile.ui.state.UserProfile
import com.siledje.mobile.ui.theme.ThemeMode

/** Hub : rapports, clients, categories, commandes, mouvements, apparence et compte. */
@Composable
fun PlusScreen(
    profile: UserProfile?,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Plus", subtitle = "Outils, rapports et réglages")

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SiledjeCard {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    InitialAvatar(profile?.username ?: "?", size = 52.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(profile?.username ?: "Utilisateur", style = MaterialTheme.typography.titleMedium)
                        profile?.email?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    profile?.roleName?.let { StatusChip(it, MaterialTheme.colorScheme.primary) }
                }
            }

            SectionTitle("Gestion")
            MenuRow(
                Icons.Filled.BarChart, "Rapports",
                "Chiffre d'affaires, ventes par jour, paiements, stock par catégorie"
            ) { onNavigate(Routes.REPORTS) }
            MenuRow(
                Icons.Filled.People, "Clients",
                "Fichier clients, points de fidélité, achats"
            ) { onNavigate(Routes.CLIENTS) }
            MenuRow(
                Icons.Filled.Category, "Catégories",
                "Organiser les produits par catégorie"
            ) { onNavigate(Routes.CATEGORIES) }
            MenuRow(
                Icons.Filled.LocalShipping, "Commandes fournisseurs",
                "Suivi des commandes passées et reçues"
            ) { onNavigate(Routes.ORDERS) }
            MenuRow(
                Icons.Filled.History, "Mouvements de stock",
                "Entrées, sorties et corrections"
            ) { onNavigate(Routes.MOVEMENTS) }

            SectionTitle("Réglages")
            SectionCard("Apparence") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.values().forEach { mode ->
                        FilterPill(mode.label, selected = mode == themeMode, onClick = { onThemeChange(mode) })
                    }
                }
            }
            SectionCard("À propos") {
                InfoRow("Application", "Siledje Mobile")
                ListDivider()
                InfoRow("Version", "1.0.0")
            }

            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Se déconnecter")
            }
        }
    }
}
