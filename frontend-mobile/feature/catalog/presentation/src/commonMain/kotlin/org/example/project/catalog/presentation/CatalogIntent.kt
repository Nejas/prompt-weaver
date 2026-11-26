package org.example.project.catalog.presentation

sealed interface CatalogIntent {
    data object Reload : CatalogIntent
    data class SelectTemplate(val versionId: String) : CatalogIntent
    data object Register : CatalogIntent
    data object Login : CatalogIntent
    data object Account : CatalogIntent
}
