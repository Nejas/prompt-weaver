package org.example.project.chat.presentation

import org.example.project.chat.domain.ChatTemplateCategory
import org.example.project.chat.domain.ChatTemplate

data class ChatState(
    val model: ChatModel = ChatModel.GeminiFlash,
    val draft: String = "",
    val messages: List<ChatMessage> = listOf(ChatMessage.DemoUser, ChatMessage.DemoAssistant),
    val templateCategories: List<ChatTemplateCategory> = emptyList(),
    val templatesLoading: Boolean = false,
    val templatesError: String? = null,
    val selectedCategory: String? = null,
    val selectedTemplate: ChatTemplate? = null,
    val categoryDraft: String = "",
    val templateTitleDraft: String = "",
    val templateDescriptionDraft: String = "",
    val templateCategoryDraft: String = "",
    val templateFormVisible: Boolean = false,
    val creatingCategory: Boolean = false,
    val templateFormError: String? = null,
    val templateSaving: Boolean = false,
    val categorySaving: Boolean = false,
    val pendingPrompt: String? = null,
)

enum class ChatModel { GeminiFlash }

sealed interface ChatMessage {
    data object DemoUser : ChatMessage
    data object DemoAssistant : ChatMessage
    data class User(val text: String) : ChatMessage
}
