# Siledje Mobile

Application mobile de gestion commerciale pour boutique : ventes, stock, fournisseurs, clients et rapports, synchronisée avec la base Supabase de l'application desktop Siledje.

Écrite en **Kotlin Multiplatform** avec **Compose Multiplatform** : une seule interface partagée entre Android et iOS (testée sur Android).

## Fonctionnalités

**Connexion et compte**
- Connexion par email et mot de passe, création d'un compte entreprise (email, nom d'utilisateur, mot de passe confirmé).
- Session conservée entre deux lancements, déconnexion depuis l'onglet Plus.
- Thème clair, sombre ou automatique.

**Accueil** : chiffres clés (ventes récentes, produits, stock bas, fournisseurs, valeur du stock), alertes de réapprovisionnement, dernières ventes.

**Ventes** : historique groupé par jour, recherche par facture ou client, et détail complet d'une facture (client, paiement, chaque article avec quantité, prix et remise, sous-total, taxes, total).

**Stock** : produits regroupés par catégorie, recherche par nom ou SKU, filtres Tous, Stock bas et Rupture, filtre par catégorie. Fiche produit avec marge et historique des mouvements. Ajout, modification, désactivation et ajustement de stock (entrée, sortie, correction).

**Fournisseurs** : liste avec recherche, fiche avec appel et email en un geste, commandes passées. Ajout, modification et désactivation.

**Plus** : rapports (chiffre d'affaires, panier moyen, ventes par jour, répartition des paiements, produits par catégorie), clients et leurs achats, catégories (ajout, modification, désactivation), suivi des commandes fournisseurs, mouvements de stock.

## Technologies

| Domaine | Choix |
|---|---|
| Langage et interface | Kotlin Multiplatform, Compose Multiplatform, Material 3 |
| Navigation | Navigation Compose (barre à 5 onglets, écrans de détail et formulaires) |
| Backend | Supabase : Postgrest (données) et Auth (comptes) |
| Configuration | BuildKonfig, valeurs lues depuis `local.properties` |

## Installation

1. Cloner le dépôt et l'ouvrir avec Android Studio.
2. Créer ou compléter le fichier `local.properties` à la racine (il n'est pas versionné) :

```properties
SUPABASE_URL=https://xxxxxxxx.supabase.co
SUPABASE_ANON_KEY=votre_cle_publique_anon
```

3. Synchroniser Gradle puis lancer le module `composeApp` sur un émulateur ou un téléphone.

Ne mettez jamais la clé `service_role` dans l'application.

## Configuration de Supabase

1. **Règles d'accès (RLS).** Une fois connecté, l'application envoie le jeton de l'utilisateur (rôle `authenticated`). Exécutez `docs/supabase_rls.sql` dans le SQL Editor de Supabase pour autoriser la lecture à ce rôle. Sans cela, toutes les listes apparaissent vides sans message d'erreur.
2. **Confirmation par email.** Pendant le développement, désactivez **Confirm email** (Authentication, fournisseur Email). Pour valider un compte déjà créé :

```sql
update auth.users set email_confirmed_at = now() where email = 'votre-email@exemple.com';
```

3. **Inscription libre.** L'application permet de créer un compte. Le script SQL donne accès aux données à tout compte connecté : désactivez l'inscription libre (Allow new users to sign up) une fois vos comptes créés, ou restreignez l'accès à une liste d'adresses.

## Activer les écritures (CRUD)

Création, modification, désactivation et ajustement de stock sont entièrement implémentés, mais l'action finale est bloquée : l'utilisateur voit un message « Cette fonctionnalité sera bientôt disponible ».

Pour les ouvrir, dans `composeApp/src/commonMain/kotlin/com/siledje/mobile/core/FeatureFlags.kt` :

```kotlin
val crudEnabled: Boolean = true
```

Exécutez aussi le bloc d'écriture du fichier `docs/supabase_rls.sql`. Vérifiez enfin que les types de mouvement de stock (`in`, `out`, `adjustment`, dans `CatalogStore.kt`) correspondent à ceux de l'application desktop.

## Architecture

```
composeApp/src/commonMain/kotlin/com/siledje/mobile/
  App.kt                      point d'entrée : session, connexion, application
  core/FeatureFlags.kt        interrupteur des écritures
  data/
    remote/                   client Supabase unique
    repository/               accès aux tables (catalogue, ventes, commandes, auth)
  domain/model/               modèles sérialisables (produit, vente, client...)
  ui/
    theme/                    couleurs, typographie, thème clair et sombre
    components/               composants partagés, formulaires, toasts
    state/                    états et logique par domaine (catalogue, ventes, auth...)
    navigation/               onglets, routes, barre du bas
    auth/  accueil/  ventes/  stock/  fournisseurs/
    plus/  rapports/  clients/  categories/  profile/
```

Principes : les repositories ne font que parler à Supabase ; les états (`ui/state`) portent la logique et sont partagés entre écrans ; toutes les écritures passent par un point unique (`WriteGuard`) contrôlé par le drapeau `crudEnabled`.

## Dépannage

| Symptôme | Cause et solution |
|---|---|
| Après connexion, tout est à zéro | Règles RLS limitées au rôle `anon` : exécuter `docs/supabase_rls.sql` |
| Impossible de se connecter après inscription | Email non confirmé : désactiver Confirm email ou valider le compte en SQL |
| Mode de paiement ou client absent d'une vente | Liaison à vérifier entre `sales` et `payment_methods` ou `clients` |
| Message « Chargement impossible » | Pas de réseau ou URL Supabase incorrecte dans `local.properties` |

## Pistes d'évolution

- Création de ventes depuis le téléphone, avec scan de codes-barres.
- Isolation des données par entreprise (multi-boutiques) pour sécuriser les comptes.
- Mode hors ligne avec synchronisation.
- Export des factures en PDF.
