package org.example.project

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.builtins.serializer
import org.example.project.auth.domain.AuthRepository
import org.example.project.auth.domain.RegisteredUser
import org.example.project.auth.domain.Registration
import org.example.project.auth.domain.SessionRepository
import org.example.project.auth.domain.SessionTokens
import org.example.project.catalog.domain.CatalogRepository
import org.example.project.catalog.domain.ComposedPrompt
import org.example.project.catalog.domain.TemplateDetails
import org.example.project.catalog.domain.TemplatePage
import org.example.project.auth.presentation.LoginIntent
import org.example.project.auth.presentation.RegistrationIntent
import org.example.project.chat.presentation.ChatIntent
import org.example.project.profile.presentation.ProfileIntent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class SharedCommonTest {
    @Test
    fun legacyCatalogStackIsIgnoredAndSessionIsCheckedAgain() {
        for (hasSession in listOf(true, false)) {
            val legacyKeeper = StateKeeperDispatcher()
            val legacyContext = DefaultComponentContext(LifecycleRegistry(), stateKeeper = legacyKeeper)
            legacyContext.childStack(
                source = StackNavigation<String>(),
                serializer = String.serializer(),
                initialConfiguration = "Catalog",
                key = "DefaultChildStack",
            ) { _, _ -> Unit }
            val restoredContext = DefaultComponentContext(
                LifecycleRegistry(), stateKeeper = StateKeeperDispatcher(legacyKeeper.save()),
            )

            val root = createRoot(
                sessions = FakeSessionRepository(tokens = if (hasSession) SessionTokens("a", "r", 900) else null),
                context = restoredContext,
            )

            if (hasSession) assertIs<AppRoot.Child.Chat>(root.stack.value.active.instance)
            else assertIs<AppRoot.Child.Auth>(root.stack.value.active.instance)
            assertEquals(1, root.stack.value.items.size)
        }
    }

    @Test
    fun restoredSessionOpensChatAsTheOnlyScreen() {
        val root = createRoot()

        assertIs<AppRoot.Child.Chat>(root.stack.value.active.instance)
        assertEquals(1, root.stack.value.items.size)
    }

    @Test
    fun absentSessionOpensLogin() {
        val root = createRoot(FakeSessionRepository(tokens = null))

        val auth = assertIs<AppRoot.Child.Auth>(root.stack.value.active.instance)
        assertIs<AuthFlowState.Login>(auth.component.state.value)
        assertEquals(1, root.stack.value.items.size)
    }

    @Test
    fun failedSessionRestoreOpensLogin() {
        val root = createRoot(FakeSessionRepository(failRefresh = true))

        assertIs<AppRoot.Child.Auth>(root.stack.value.active.instance)
    }

    @Test
    fun loadingScreenRemainsUntilSessionCheckFinishes() {
        val gate = CompletableDeferred<Unit>()
        val root = createRoot(FakeSessionRepository(refreshGate = gate))

        assertIs<AppRoot.Child.Loading>(root.stack.value.active.instance)
        gate.complete(Unit)

        assertIs<AppRoot.Child.Chat>(root.stack.value.active.instance)
    }

    @Test
    fun successfulLoginReplacesAuthWithChat() {
        val root = createRoot(FakeSessionRepository(tokens = null))
        val auth = assertIs<AppRoot.Child.Auth>(root.stack.value.active.instance)
        val login = assertIs<AuthFlowState.Login>(auth.component.state.value).component

        login.onIntent(LoginIntent.UpdateEmail("user@example.com"))
        login.onIntent(LoginIntent.UpdatePassword("password"))
        login.onIntent(LoginIntent.Submit)

        assertIs<AppRoot.Child.Chat>(root.stack.value.active.instance)
        assertEquals(1, root.stack.value.items.size)
    }

    @Test
    fun registrationWithoutSessionReturnsToLogin() {
        val root = createRoot(FakeSessionRepository(tokens = null))

        register(root)

        val auth = assertIs<AppRoot.Child.Auth>(root.stack.value.active.instance)
        assertIs<AuthFlowState.Login>(auth.component.state.value)
    }

    @Test
    fun registrationWithSavedSessionOpensChat() {
        val root = createRoot(FakeSessionRepository(tokens = null), registrationCreatesSession = true)

        register(root)

        assertIs<AppRoot.Child.Chat>(root.stack.value.active.instance)
        assertEquals(1, root.stack.value.items.size)
    }

    @Test
    fun returningFromProfileRestoresTheExistingChat() {
        val root = createRoot()
        val chat = assertIs<AppRoot.Child.Chat>(root.stack.value.active.instance)
        chat.component.onIntent(ChatIntent.ChangeDraft("Keep this draft"))
        chat.component.onIntent(ChatIntent.OpenProfile)

        val profile = assertIs<AppRoot.Child.Profile>(root.stack.value.active.instance)
        profile.component.onIntent(ProfileIntent.OpenChat)

        assertSame(chat, root.stack.value.active.instance)
        assertEquals("Keep this draft", chat.component.state.value.draft)
        assertEquals(1, root.stack.value.items.size)
    }

    @Test
    fun logoutClearsChatAndProfileFromBackStack() {
        val sessions = FakeSessionRepository()
        val root = createRoot(sessions)
        val chat = assertIs<AppRoot.Child.Chat>(root.stack.value.active.instance).component
        chat.onIntent(ChatIntent.OpenProfile)
        val profile = assertIs<AppRoot.Child.Profile>(root.stack.value.active.instance).component

        profile.onIntent(ProfileIntent.Logout)

        val auth = assertIs<AppRoot.Child.Auth>(root.stack.value.active.instance)
        assertIs<AuthFlowState.Login>(auth.component.state.value)
        assertEquals(1, root.stack.value.items.size)
        assertEquals(null, sessions.tokens)
    }

    private fun register(root: AppRoot) {
        val auth = assertIs<AppRoot.Child.Auth>(root.stack.value.active.instance).component
        assertIs<AuthFlowState.Login>(auth.state.value).component.onIntent(LoginIntent.Register)
        val registration = assertIs<AuthFlowState.Registration>(auth.state.value).component
        registration.onIntent(RegistrationIntent.UpdateFirstName("Test"))
        registration.onIntent(RegistrationIntent.UpdateLastName("User"))
        registration.onIntent(RegistrationIntent.UpdateEmail("user@example.com"))
        registration.onIntent(RegistrationIntent.UpdatePassword("password"))
        registration.onIntent(RegistrationIntent.Submit)
    }

    private fun createRoot(
        sessions: FakeSessionRepository = FakeSessionRepository(),
        registrationCreatesSession: Boolean = false,
        context: ComponentContext = DefaultComponentContext(LifecycleRegistry()),
    ) = AppRoot(
        context = context,
        catalogRepository = FakeCatalogRepository(),
        authRepository = FakeAuthRepository {
            if (registrationCreatesSession) sessions.tokens = SessionTokens("access", "refresh", 900)
        },
        sessionRepository = sessions,
        dispatcher = Dispatchers.Unconfined,
    )
}

private class FakeCatalogRepository : CatalogRepository {
    override suspend fun listTemplates(limit: Int, offset: Int) = TemplatePage(emptyList(), limit, offset)
    override suspend fun getTemplate(versionId: String) = TemplateDetails(
        summary = error("Not used"),
        body = "",
        parameters = emptyList(),
    )
    override suspend fun composePrompt(versionId: String, parameters: Map<String, String>) =
        ComposedPrompt(versionId, "")
}

private class FakeAuthRepository(private val onRegister: () -> Unit) : AuthRepository {
    override suspend fun register(registration: Registration): RegisteredUser {
        onRegister()
        return RegisteredUser("user", registration.firstName, registration.lastName, registration.email)
    }
}

private class FakeSessionRepository(
    var tokens: SessionTokens? = SessionTokens("access", "refresh", 900),
    private val failRefresh: Boolean = false,
    private val refreshGate: CompletableDeferred<Unit>? = null,
) : SessionRepository {
    override suspend fun login(email: String, password: String): SessionTokens =
        SessionTokens("access", "refresh", 900).also { tokens = it }

    override suspend fun refresh(): SessionTokens? {
        refreshGate?.await()
        check(!failRefresh)
        return tokens
    }

    override suspend fun logout(): Boolean {
        tokens = null
        return true
    }
}
