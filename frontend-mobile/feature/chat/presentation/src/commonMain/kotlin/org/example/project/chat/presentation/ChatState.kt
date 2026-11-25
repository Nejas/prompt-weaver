package org.example.project.chat.presentation

data class ChatState(
    val model: ChatModel = ChatModel.GeminiFlash,
    val draft: String = "",
    val messages: List<ChatMessage> = listOf(ChatMessage.DemoUser, ChatMessage.DemoAssistant),
)

enum class ChatModel { GeminiFlash }

sealed interface ChatMessage {
    data object DemoUser : ChatMessage
    data object DemoAssistant : ChatMessage
    data class User(val text: String) : ChatMessage
}
