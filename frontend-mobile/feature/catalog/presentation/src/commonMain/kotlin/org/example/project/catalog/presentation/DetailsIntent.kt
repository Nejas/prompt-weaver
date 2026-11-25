package org.example.project.catalog.presentation

sealed interface DetailsIntent {
    data object Reload : DetailsIntent
    data class SetValue(val name: String, val value: String) : DetailsIntent
    data object Validate : DetailsIntent
    data object Compose : DetailsIntent
    data object Back : DetailsIntent
}
