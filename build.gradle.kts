plugins {
    // Un seul code source pour Android ET iOS — c'est le principe de KMP :
    // la logique métier (domain/data) est écrite une fois, l'UI Compose
    // aussi, et seule une fine couche par plateforme diffère (main iOS entry, etc.)
    kotlin("multiplatform") version "2.0.20" apply false
    kotlin("plugin.serialization") version "2.0.20" apply false
    id("com.android.application") version "8.5.2" apply false
    id("com.android.library") version "8.5.2" apply false
    id("org.jetbrains.compose") version "1.7.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
}
