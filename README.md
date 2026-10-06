# 📱 Siledje-Mobile

**Siledje-Mobile** est l'application mobile officielle du projet **Siledje**, développée en **Kotlin Multiplatform (KMP)** avec **Compose Multiplatform**. Elle offre une expérience moderne, fluide et réactive à la fois sur **Android** et **iOS** à partir d'un codebase partagé.

---

## 🚀 Fonctionnalités principales

- 📲 **Multiplateforme** : Code UI et logique métier partagés avec Compose Multiplatform.
- 🎨 **Interface moderne** : Design adaptatif et conforme aux derniers standards UX/UI.
- ⚡ **Performance** : Exécution native optimale sur Android et iOS.
- 🔄 **Codebase unifié** : Une seule base de code pour deux plateformes.

---

## 🛠️ Stack Technique

- **Langage** : Kotlin (100%)
- **UI Framework** : Compose Multiplatform
- **Architecture** : Kotlin Multiplatform (KMP)
- **Gestionnaire de build** : Gradle (`build.gradle.kts`, `settings.gradle.kts`)
- **Cibles** : Android & iOS

---

## 📁 Structure du Projet

```text
Siledje-Mobile/
├── composeApp/              # Module principal de l'application
│   ├── src/
│   │   ├── commonMain/      # Code partagé (UI + logique)
│   │   ├── androidMain/     # Code spécifique Android
│   │   └── iosMain/         # Code spécifique iOS
│   └── build.gradle.kts
├── gradle/
│   └── wrapper/             # Wrapper Gradle
├── build.gradle.kts         # Configuration Gradle principale
├── settings.gradle.kts      # Configuration des modules du projet
├── gradlew / gradlew.bat    # Scripts d'exécution Gradle
├── .gitignore               # Fichiers ignorés par Git
└── README.md                # Document de présentation
```

---

## ⚙️ Prérequis & Configuration

Avant de commencer, assurez-vous d'avoir installé :

- **JDK 17** ou supérieur
- **Android Studio** (dernière version recommandée, ex : Ladybug/Koala) avec le plugin **Kotlin Multiplatform**
- **Xcode** (obligatoire pour la compilation et l'exécution de la partie iOS, sur macOS uniquement)
- **Kotlin Multiplatform Plugin** installé dans l'IDE

---

## 🏃 Lancement du Projet

### 1. Cloner le dépôt

```bash
git clone https://github.com/yvanol-fotso/Siledje-Mobile.git
cd Siledje-Mobile
```

### 2. Exécuter sur Android

Vous pouvez exécuter l'application depuis **Android Studio** ou en ligne de commande :

```bash
./gradlew :composeApp:assembleDebug
```

Ou directement depuis Android Studio : sélectionner la configuration `composeApp`.

### 3. Exécuter sur iOS

Ouvrez le dossier du projet dans **Android Studio** et sélectionnez le runner iOS (Device/Simulateur), ou ouvrez le projet **Xcode** généré dans le sous-dossier iOS pour lancer l'application directement depuis Xcode.

```bash
./gradlew :composeApp:iosSimulatorArm64Test
```

---

## 🧪 Tests

```bash
./gradlew test
```

---

## 🤝 Contribution

Les contributions au projet sont les bienvenues ! Pour contribuer :

1. **Fork** le projet
2. Créer une branche pour votre fonctionnalité :
   ```bash
   git checkout -b feature/NouvelleFonctionnalite
   ```
3. Effectuer vos modifications et commiter :
   ```bash
   git commit -m 'feat: ajout de la nouvelle fonctionnalité'
   ```
4. Pusher vers votre branche :
   ```bash
   git push origin feature/NouvelleFonctionnalite
   ```
5. Ouvrir une **Pull Request**

---

## 📄 Licence

Ce projet est sous **licence privée / propriétaire** — tous droits réservés par l'équipe **Siledje**.

---

## 👤 Auteur

**yvanolfotso-work** — [@yvanolfotso-work](https://github.com/yvanolfotso-work)

---

## 📌 Statut du projet

🚧 **En cours de développement** — Première configuration initiale du projet.

---

<p align="center">
   Par l'équipe <strong>Siledje</strong>
</p>
