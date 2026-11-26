package org.example.project.chat.presentation

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatComponent(
    context: ComponentContext,
    private val onProfile: () -> Unit = {},
    private val onCatalog: () -> Unit = {},
) : ComponentContext by context {
    private val mutableState = MutableStateFlow(ChatState())
    val state = mutableState.asStateFlow()

    fun onIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.ChangeDraft -> mutableState.value = state.value.copy(draft = intent.value)
            ChatIntent.Send -> send()
            ChatIntent.OpenProfile -> onProfile()
            ChatIntent.OpenCatalog -> onCatalog()
        }
    }

    private fun send() {
        val draft = state.value.draft.trim()
        if (draft.isEmpty()) return
        mutableState.value = state.value.copy(
            draft = "",
            messages = state.value.messages + ChatMessage.User(draft),
        )
    }
}
