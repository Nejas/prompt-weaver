package org.example.project

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.Dispatchers
import org.example.project.auth.domain.AuthRepository
import org.example.project.auth.domain.RegisteredUser
import org.example.project.auth.domain.Registration
import org.example.project.auth.domain.SessionRepository
import org.example.project.auth.domain.SessionTokens
import org.example.project.catalog.domain.CatalogRepository
import org.example.project.catalog.domain.ComposedPrompt
import org.example.project.catalog.domain.TemplateDetails
import org.example.project.catalog.domain.TemplatePage
import org.example.project.catalog.domain.TemplateParameter
import org.example.project.catalog.domain.TemplateSummary
import org.example.project.catalog.presentation.DetailsIntent
import org.example.project.chat.domain.ChatTemplate
import org.example.project.chat.domain.ChatTemplateCategory
import org.example.project.chat.domain.ChatTemplateDraft
import org.example.project.chat.domain.ChatTemplateRepository
import org.example.project.chat.presentation.ChatComponent
import org.example.project.chat.presentation.ChatIntent
import org.example.project.profile.presentation.ProfileIntent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class ChatTemplateFlowTest {
    @Test
    fun composedPromptReturnsToExistingChatWithoutSending() {
        val root = createRoot()
        val chat = openChat(root)
        val originalMessages = chat.state.value.messages

        openTemplate(root, chat).onIntent(DetailsIntent.SetValue("topic", "Kotlin"))
        val details = assertIs<AppRoot.Child.TemplateDetails>(root.stack.value.active.instance).component
        details.onIntent(DetailsIntent.Compose)
        assertEquals("", chat.state.value.draft)
        details.onIntent(DetailsIntent.Apply)

        assertSame(chat, assertIs<AppRoot.Child.Chat>(root.stack.value.active.instance).component)
        assertEquals("Explain Kotlin", chat.state.value.draft)
        assertEquals(originalMessages, chat.state.value.messages)
        assertEquals("Learning", chat.state.value.selectedCategory)
    }

    @Test
    fun cancellingDetailsPreservesDraftAndProfileNavigationReusesChat() {
        val root = createRoot()
        val chat = openChat(root)
        chat.onIntent(ChatIntent.ChangeDraft("My draft"))

        val details = openTemplate(root, chat)
        details.onIntent(DetailsIntent.Back)
        assertSame(chat, assertIs<AppRoot.Child.Chat>(root.stack.value.active.instance).component)
        assertEquals("My draft", chat.state.value.draft)

        chat.onIntent(ChatIntent.OpenProfile)
        val profile = assertIs<AppRoot.Child.Profile>(root.stack.value.active.instance).component
        profile.onIntent(ProfileIntent.OpenChat)
        assertSame(chat, openChat(root))
        assertEquals("My draft", chat.state.value.draft)
    }

    @Test
    fun applyingToFilledChatOffersReplacementAndCancellationPreservesDraft() {
        val root = createRoot()
        val chat = openChat(root)
        chat.onIntent(ChatIntent.ChangeDraft("Original"))

        val details = openTemplate(root, chat)
        details.onIntent(DetailsIntent.Apply)
        assertIs<AppRoot.Child.TemplateDetails>(root.stack.value.active.instance)
        details.onIntent(DetailsIntent.SetValue("topic", "KMP"))
        details.onIntent(DetailsIntent.Compose)
        details.onIntent(DetailsIntent.Apply)

        assertIs<AppRoot.Child.Chat>(root.stack.value.active.instance)
        assertEquals("Original", chat.state.value.draft)
        assertEquals("Explain KMP", chat.state.value.pendingPrompt)
        chat.onIntent(ChatIntent.CancelDraftReplacement)
        assertEquals("Original", chat.state.value.draft)
        assertEquals(null, chat.state.value.pendingPrompt)
    }

    private fun openChat(root: AppRoot): ChatComponent {
        return assertIs<AppRoot.Child.Chat>(root.stack.value.active.instance).component
    }

    private fun openTemplate(root: AppRoot, chat: ChatComponent) = run {
        chat.onIntent(ChatIntent.SelectCategory("Learning"))
        chat.onIntent(ChatIntent.SelectTemplate("v1"))
        chat.onIntent(ChatIntent.UseSelectedTemplate)
        assertIs<AppRoot.Child.TemplateDetails>(root.stack.value.active.instance).component
    }

    private fun createRoot() = AppRoot(
        context = DefaultComponentContext(LifecycleRegistry()),
        catalogRepository = FlowCatalogRepository(),
        authRepository = object : AuthRepository {
            override suspend fun register(registration: Registration) = RegisteredUser(
                "user", registration.firstName, registration.lastName, registration.email,
            )
        },
        sessionRepository = object : SessionRepository {
            override suspend fun login(email: String, password: String) = SessionTokens("access", "refresh", 900)
            override suspend fun refresh() = SessionTokens("access", "refresh", 900)
            override suspend fun logout() = true
        },
        dispatcher = Dispatchers.Unconfined,
        chatTemplates = object : ChatTemplateRepository {
            override suspend fun categories() = listOf(ChatTemplateCategory(
                "Learning", listOf(ChatTemplate("v1", "Explain", "", "Learning", false)), false,
            ))
            override suspend fun createCategory(name: String) = Unit
            override suspend fun createTemplate(draft: ChatTemplateDraft) = Unit
        },
    )
}

private class FlowCatalogRepository : CatalogRepository {
    private val summary = TemplateSummary("v1", "Explain", "", "Learning", "explain")

    override suspend fun listTemplates(limit: Int, offset: Int) = TemplatePage(listOf(summary), limit, offset)
    override suspend fun getTemplate(versionId: String): TemplateDetails {
        check(versionId == "v1")
        return TemplateDetails(summary, "Explain {{topic}}", listOf(
            TemplateParameter("topic", "Topic", "text", true, null, emptyList()),
        ))
    }
    override suspend fun composePrompt(versionId: String, parameters: Map<String, String>): ComposedPrompt {
        check(versionId == "v1")
        return ComposedPrompt(versionId, "Explain ${parameters.getValue("topic")}")
    }
}
