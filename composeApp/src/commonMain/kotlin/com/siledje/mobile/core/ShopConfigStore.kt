package com.siledje.mobile.core

import com.russhwolf.settings.Settings

/**
 * Enregistre la configuration de la boutique sur l'appareil (SharedPreferences
 * sur Android, NSUserDefaults sur iOS). Aucune recompilation par client :
 * l'APK est le meme pour tous, la configuration est saisie au premier lancement.
 */
object ShopConfigStore {
    private const val KEY_URL = "shop_url"
    private const val KEY_ANON = "shop_anon_key"
    private const val KEY_NAME = "shop_name"

    private val settings: Settings by lazy { Settings() }

    /** Configuration enregistree et valide, ou null (premier lancement). */
    fun load(): ShopConfig? {
        val url = settings.getStringOrNull(KEY_URL) ?: return null
        val key = settings.getStringOrNull(KEY_ANON) ?: return null
        if (ShopConfigCodec.validate(url, key) != null) return null
        return ShopConfig(url, key, settings.getStringOrNull(KEY_NAME).orEmpty())
    }

    fun save(config: ShopConfig) {
        settings.putString(KEY_URL, config.url)
        settings.putString(KEY_ANON, config.anonKey)
        settings.putString(KEY_NAME, config.name)
    }

    fun clear() {
        settings.remove(KEY_URL)
        settings.remove(KEY_ANON)
        settings.remove(KEY_NAME)
    }
}
