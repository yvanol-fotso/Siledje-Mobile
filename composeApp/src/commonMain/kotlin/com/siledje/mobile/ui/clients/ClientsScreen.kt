package com.siledje.mobile.ui.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siledje.mobile.domain.model.Client
import com.siledje.mobile.domain.model.Sale
import com.siledje.mobile.ui.components.EmptyState
import com.siledje.mobile.ui.components.ErrorState
import com.siledje.mobile.ui.components.InfoRow
import com.siledje.mobile.ui.components.InitialAvatar
import com.siledje.mobile.ui.components.ListDivider
import com.siledje.mobile.ui.components.ScreenHeader
import com.siledje.mobile.ui.components.SearchField
import com.siledje.mobile.ui.components.SectionCard
import com.siledje.mobile.ui.components.SectionTitle
import com.siledje.mobile.ui.components.SiledjeCard
import com.siledje.mobile.ui.components.SkeletonList
import com.siledje.mobile.ui.components.StatusChip
import com.siledje.mobile.ui.components.formatFcfa
import com.siledje.mobile.ui.state.RemoteList
import com.siledje.mobile.ui.state.SalesStore
import com.siledje.mobile.ui.ventes.SaleCard

/** Fichier clients (lecture) avec recherche. */
@Composable
fun ClientsScreen(
    clients: RemoteList<Client>,
    onOpenClient: (Client) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) { clients.ensureLoaded() }
    var query by remember { mutableStateOf("") }
    val q = query.trim()
    val shown = if (q.isEmpty()) clients.items else clients.items.filter {
        it.name.contains(q, ignoreCase = true) ||
            (it.phone ?: "").contains(q, ignoreCase = true) ||
            (it.email ?: "").contains(q, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Clients",
            subtitle = if (clients.loaded) "${clients.items.size} client" + (if (clients.items.size > 1) "s" else "") else "Chargement",
            onBack = onBack,
            onRefresh = { clients.refresh() }
        )
        when {
            !clients.loaded && clients.error != null ->
                ErrorState(message = clients.error ?: "", onRetry = { clients.refresh() })
            !clients.loaded -> SkeletonList()
            clients.items.isEmpty() -> EmptyState(
                icon = Icons.Filled.People,
                title = "Aucun client",
                message = "Les clients enregistrés à la caisse apparaîtront ici."
            )
            else -> {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)) {
                    SearchField(query, { query = it }, "Rechercher par nom, téléphone ou email")
                }
                if (shown.isEmpty()) {
                    EmptyState(Icons.Filled.SearchOff, "Aucun résultat", "Essayez un autre mot-clé.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(shown) { client -> ClientCard(client) { onOpenClient(client) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientCard(client: Client, onClick: () -> Unit) {
    SiledjeCard(onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            InitialAvatar(client.name)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    client.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                (client.phone?.takeIf { it.isNotBlank() } ?: client.email?.takeIf { it.isNotBlank() })?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(formatFcfa(client.totalSpent), style = MaterialTheme.typography.titleSmall)
                if (client.loyaltyPoints > 0) {
                    Spacer(Modifier.height(4.dp))
                    StatusChip("${client.loyaltyPoints} pts", MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

/** Fiche client : coordonnees, fidelite et achats retrouves dans les ventes chargees. */
@Composable
fun ClientDetailScreen(
    client: Client?,
    sales: SalesStore,
    onOpenSale: (Sale) -> Unit,
    onBack: () -> Unit
) {
    if (client == null) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title = "Client", onBack = onBack)
            EmptyState(Icons.Filled.People, "Client introuvable", "Retournez à la liste des clients.")
        }
        return
    }
    val purchases = sales.sales.filter { it.clientId == client.id }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = client.name,
            subtitle = client.phone?.takeIf { it.isNotBlank() },
            onBack = onBack
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard("Fidélité") {
                InfoRow("Total dépensé", formatFcfa(client.totalSpent))
                ListDivider()
                InfoRow("Points de fidélité", client.loyaltyPoints.toString())
            }
            SectionCard("Coordonnées") {
                InfoRow("Téléphone", client.phone?.takeIf { it.isNotBlank() } ?: "Non renseigné")
                ListDivider()
                InfoRow("Email", client.email?.takeIf { it.isNotBlank() } ?: "Non renseigné")
                ListDivider()
                InfoRow("Adresse", client.address?.takeIf { it.isNotBlank() } ?: "Non renseignée")
                client.notes?.takeIf { it.isNotBlank() }?.let {
                    ListDivider()
                    InfoRow("Notes", it)
                }
            }
            SectionTitle("Achats récents", purchases.size)
            if (purchases.isEmpty()) {
                Text(
                    "Aucun achat trouvé parmi les dernières ventes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                purchases.forEach { sale -> SaleCard(sale, sales, onClick = { onOpenSale(sale) }) }
            }
        }
    }
}
