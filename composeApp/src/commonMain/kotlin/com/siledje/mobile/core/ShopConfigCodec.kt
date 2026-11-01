package com.siledje.mobile.core

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** Configuration Supabase d'une boutique (un client = un projet Supabase). */
data class ShopConfig(
    val url: String,
    val anonKey: String,
    val name: String = ""
)

data class ShopParseResult(
    val config: ShopConfig? = null,
    val error: String? = null
)

/**
 * "Code boutique" : une seule chaine a coller au premier lancement.
 *
 *   SLD1-<base64url( url \n cle_publique \n nom_boutique )>.<controle>
 *
 * <controle> = CRC16 (4 caracteres hexa) du texte base64 : il detecte un code
 * tronque ou mal copie (une cle coupee en plein milieu resterait sinon "valide").
 *
 * Genere cote vendeur par scripts/make_client_env.py. Ne contient QUE la cle
 * publique (anon / publishable), jamais une cle secrete.
 * Ce fichier n'utilise que la bibliotheque standard Kotlin.
 */
object ShopConfigCodec {

    const val PREFIX = "SLD1-"

    private const val SECRET_MESSAGE =
        "Cette cle est une cle secrete. N'utilisez jamais une cle secrete dans l'application."

    fun normalizeUrl(raw: String): String = raw.trim().trimEnd('/')

    /** Retourne un message d'erreur, ou null si l'URL et la cle sont acceptables. */
    fun validate(rawUrl: String, rawKey: String): String? {
        val url = normalizeUrl(rawUrl)
        val key = rawKey.trim()

        val host = url.removePrefix("https://").substringBefore('/')
        val urlOk = url.startsWith("https://") &&
            host.endsWith(".supabase.co") &&
            host.length > ".supabase.co".length &&
            url.none { it.isWhitespace() }
        if (!urlOk) return "Adresse du projet invalide (attendu : https://xxxx.supabase.co)."

        if (key.isEmpty()) return "La cle publique est manquante."
        if (key.any { it.isWhitespace() }) return "La cle ne doit pas contenir d'espace."
        if (key.startsWith("sb_secret_") || isServiceRoleJwt(key)) return SECRET_MESSAGE
        if (!(key.startsWith("sb_publishable_") || key.startsWith("eyJ"))) {
            return "Cle publique attendue (elle commence par sb_publishable_)."
        }
        return null
    }

    /** Decode et valide un code boutique colle par l'utilisateur. */
    @OptIn(ExperimentalEncodingApi::class)
    fun parseCode(code: String): ShopParseResult {
        val cleaned = code.filterNot { it.isWhitespace() }
        if (!cleaned.startsWith(PREFIX)) {
            return ShopParseResult(error = "Code boutique invalide : il doit commencer par $PREFIX")
        }
        val body = cleaned.removePrefix(PREFIX).substringBeforeLast('.')
        val check = cleaned.substringAfterLast('.', missingDelimiterValue = "")
        if (check.isEmpty() || !cleaned.contains('.') || !check.equals(checksum(body), ignoreCase = true)) {
            return ShopParseResult(error = "Code boutique incomplet ou mal copie. Copiez-le en entier.")
        }
        val text = try {
            Base64.UrlSafe.decode(pad(body)).decodeToString()
        } catch (e: IllegalArgumentException) {
            return ShopParseResult(error = "Code boutique incomplet ou mal copie. Copiez-le en entier.")
        }
        val parts = text.split('\n')
        if (parts.size < 2) return ShopParseResult(error = "Code boutique invalide.")

        val url = normalizeUrl(parts[0])
        val key = parts[1].trim()
        validate(url, key)?.let { return ShopParseResult(error = it) }
        return ShopParseResult(config = ShopConfig(url, key, parts.getOrNull(2)?.trim().orEmpty()))
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun encode(config: ShopConfig): String {
        val payload = listOf(config.url, config.anonKey, config.name).joinToString("\n")
        val body = Base64.UrlSafe.encode(payload.encodeToByteArray()).trimEnd('=')
        return PREFIX + body + "." + checksum(body)
    }

    /** CRC16-CCITT (init 0xFFFF) en 4 caracteres hexa. Identique au script Python. */
    private fun checksum(text: String): String {
        var crc = 0xFFFF
        for (b in text.encodeToByteArray()) {
            crc = crc xor ((b.toInt() and 0xFF) shl 8)
            repeat(8) {
                crc = if (crc and 0x8000 != 0) (crc shl 1) xor 0x1021 else crc shl 1
                crc = crc and 0xFFFF
            }
        }
        return crc.toString(16).padStart(4, '0')
    }

    private fun pad(s: String): String = s + "=".repeat((4 - s.length % 4) % 4)

    /** Ancienne cle "anon" ou "service_role" au format JWT : on refuse service_role. */
    @OptIn(ExperimentalEncodingApi::class)
    private fun isServiceRoleJwt(key: String): Boolean {
        val parts = key.split('.')
        if (parts.size < 2) return false
        return try {
            val payload = Base64.UrlSafe.decode(pad(parts[1])).decodeToString()
            payload.replace(" ", "").contains("\"role\":\"service_role\"")
        } catch (e: Exception) {
            false
        }
    }
}
