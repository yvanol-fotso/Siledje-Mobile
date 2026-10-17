package com.siledje.mobile.core

/**
 * Interrupteurs de fonctionnalites.
 *
 * crudEnabled = false : tout le CRUD (creer / modifier / desactiver un produit
 * ou un fournisseur, ajuster le stock) est entierement branche, formulaires et
 * validations compris, mais l'action finale vers Supabase est BLOQUEE : l'utilisateur
 * voit un toast "fonctionnalite bientot disponible".
 *
 * Pour ouvrir la fonctionnalite au marche, il suffit de passer cette valeur a true.
 */
object FeatureFlags {
    val crudEnabled: Boolean = false

    const val COMING_SOON_MESSAGE = "Cette fonctionnalité sera bientôt disponible."
}
