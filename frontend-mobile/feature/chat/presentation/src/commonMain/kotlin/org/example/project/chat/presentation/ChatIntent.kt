package org.example.project.chat.presentation

sealed interface ChatIntent {
    data class ChangeDraft(val value: String) : ChatIntent
    data object Send : ChatIntent
    data object OpenProfile : ChatIntent
    data object OpenSettings : ChatIntent
    data object RetryTemplates : ChatIntent
    data class SelectCategory(val name: String) : ChatIntent
    data class SelectTemplate(val id: String) : ChatIntent
    data class ChangeCategoryDraft(val value: String) : ChatIntent
    data object CreateCategory : ChatIntent
    data object ShowTemplateForm : ChatIntent
    data object HideTemplateForm : ChatIntent
    data class ChangeTemplateTitle(val value: String) : ChatIntent
    data class ChangeTemplateDescription(val value: String) : ChatIntent
    data class ChangeTemplateCategory(val value: String) : ChatIntent
    data class SetCreatingCategory(val value: Boolean) : ChatIntent
    data object CreateTemplate : ChatIntent
    data object UseSelectedTemplate : ChatIntent
    data object ConfirmDraftReplacement : ChatIntent
    data object CancelDraftReplacement : ChatIntent
}
