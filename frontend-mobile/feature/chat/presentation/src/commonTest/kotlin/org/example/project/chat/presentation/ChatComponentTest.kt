package org.example.project.chat.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals

class ChatComponentTest {
    @Test
    fun sendAddsLocalUserMessageAndClearsDraft() {
        val component = ChatComponent(DefaultComponentContext(LifecycleRegistry()))

        component.onIntent(ChatIntent.ChangeDraft("Привет"))
        component.onIntent(ChatIntent.Send)

        assertEquals("", component.state.value.draft)
        assertEquals(ChatMessage.User("Привет"), component.state.value.messages.last())
    }
}
