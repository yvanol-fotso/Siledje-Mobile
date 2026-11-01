# Siledje Mobile

Application mobile de gestion commerciale pour boutique : ventes, stock, fournisseurs, clients et rapports, synchronisée avec la base Supabase de l'application desktop Siledje.

Écrite en **Kotlin Multiplatform** avec **Compose Multiplatform** : une seule interface partagée entre Android et iOS (testée sur Android).

**Un seul APK pour tous les clients.** L'URL et la clé publique Supabase ne sont plus figées à la compilation : le propriétaire colle un *code boutique* au premier lancement. Aucune recompilation par client.

## Fonctionnalités

**Première configuration**
- Au premier lancement, collage du *code boutique* remis par Siledje (ou saisie manuelle de l'URL et de la clé publique).
- Le code est vérifié (contrôle d'intégrité, refus des clés secrètes), puis enregistré sur l'appareil.
- « Changer de boutique » sur l'écran de connexion pour reconfigurer.

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
| Backend | Supabase : Postgrest (données) et Auth (comptes), un projet par client |
| Configuration | Code boutique saisi au premier lancement, enregistré avec `multiplatform-settings` (SharedPreferences / NSUserDefaults) |

## Installation

1. Cloner le dépôt et l'ouvrir avec Android Studio.
2. Synchroniser Gradle puis lancer le module `composeApp` sur un émulateur ou un téléphone.
3. Au premier lancement, coller un code boutique de test (voir « Générer le code boutique d'un client »).

`local.properties` ne contient plus que `sdk.dir` (créé par Android Studio). Les lignes `SUPABASE_URL` et `SUPABASE_ANON_KEY` ne sont plus utilisées : vous pouvez les supprimer.

Ne mettez jamais une clé secrète ni la clé `service_role` dans l'application.

## Générer le code boutique d'un client

Le code est produit côté vendeur par le script du projet desktop :

```bash
python scripts/make_client_env.py SIL-0001 https://xxxxxxxx.supabase.co sb_secret_xxx \
    --publishable sb_publishable_xxx --name "Librairie Exemple A"
```

Il écrit dans `clients/SIL-0001/` :
- `siledje-SIL-0001.env` : configuration **desktop** (contient la clé secrète, à transmettre en privé) ;
- `siledje-SIL-0001.mobile.txt` : le **code boutique mobile**, à envoyer au client par WhatsApp ou e-mail ;
- `siledje-SIL-0001.mobile.png` : le même code en QR (si `pip install qrcode[pil]`).

Le code boutique a la forme `SLD1-<contenu>.<contrôle>`. Il ne contient que l'URL, la **clé publique** et le nom de la boutique. Les 4 derniers caractères détectent un code tronqué ou mal copié.

## Livrer à un nouveau client

1. Créer son projet Supabase et exécuter `docs/supabase_rls.sql`.
2. **Désactiver l'inscription libre** (Authentication, *Allow new users to sign up*) : toute personne qui possède le code boutique pourrait sinon créer un compte et lire les données.
3. Créer le compte du propriétaire (ou l'autoriser à s'inscrire une seule fois avant de désactiver l'inscription).
4. Générer le code avec `make_client_env.py` et l'envoyer au client avec l'APK (identique pour tous).

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
  App.kt                      point d'entrée : configuration boutique, session, connexion, application
  core/
    FeatureFlags.kt           interrupteur des écritures
    ShopConfigCodec.kt        code boutique : décodage, validation, contrôle d'intégrité (Kotlin pur)
    ShopConfigStore.kt        enregistrement de la configuration sur l'appareil
  data/
    remote/                   client Supabase unique, configuré à l'exécution (configure())
    repository/               accès aux tables (catalogue, ventes, commandes, auth)
  domain/model/               modèles sérialisables (produit, vente, client...)
  ui/
    theme/                    couleurs, typographie, thème clair et sombre
    components/               composants partagés, formulaires, toasts
    state/                    états et logique par domaine (catalogue, ventes, auth...)
    navigation/               onglets, routes, barre du bas
    config/                   écran de première configuration (code boutique)
    auth/  accueil/  ventes/  stock/  fournisseurs/
    plus/  rapports/  clients/  categories/  profile/
```

Principes : les repositories ne font que parler à Supabase ; les états (`ui/state`) portent la logique et sont partagés entre écrans ; toutes les écritures passent par un point unique (`WriteGuard`) contrôlé par le drapeau `crudEnabled`. Le client Supabase n'existe qu'après la configuration de la boutique.

## Dépannage

| Symptôme | Cause et solution |
|---|---|
| Après connexion, tout est à zéro | Règles RLS limitées au rôle `anon` : exécuter `docs/supabase_rls.sql` |
| Impossible de se connecter après inscription | Email non confirmé : désactiver Confirm email ou valider le compte en SQL |
| Mode de paiement ou client absent d'une vente | Liaison à vérifier entre `sales` et `payment_methods` ou `clients` |
| Message « Chargement impossible » | Pas de réseau ou URL Supabase incorrecte dans le code boutique |
| « Code boutique incomplet ou mal copié » | Le code a été coupé : le copier en entier (il se termine par un point et 4 caractères) |
| « Cette clé est une clé secrète » | Mauvaise clé dans le code : régénérer avec `--publishable sb_publishable_...` |
| Mauvaise boutique sur un appareil | Écran de connexion, « Changer de boutique », puis coller le bon code |
| Erreur Gradle `Unresolved reference: BuildKonfig` | Reste de l'ancienne configuration : supprimer l'import et le plugin `buildkonfig` |

## Pistes d'évolution

- Scan d'un code QR à la place du collage du code (caméra : CameraX + ML Kit sur Android, AVFoundation sur iOS).
- Création de ventes depuis le téléphone, avec scan de codes-barres.
- Isolation des données par entreprise (multi-boutiques) pour sécuriser les comptes.
- Mode hors ligne avec synchronisation.
- Export des factures en PDF.
