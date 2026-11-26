package org.example.project.chat.presentation

sealed interface ChatIntent {
    data class ChangeDraft(val value: String) : ChatIntent
    data object Send : ChatIntent
    data object OpenProfile : ChatIntent
    data object OpenCatalog : ChatIntent
}
