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
import org.example.project.catalog.presentation.CatalogIntent
import org.example.project.catalog.presentation.CatalogRoot
import org.example.project.profile.presentation.ProfileIntent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class SharedCommonTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun returningFromProfileRestoresTheExistingCatalog() {
        val root = AppRoot(
            context = DefaultComponentContext(LifecycleRegistry()),
            catalogRepository = FakeCatalogRepository(),
            authRepository = FakeAuthRepository(),
            sessionRepository = FakeSessionRepository(),
            dispatcher = Dispatchers.Unconfined,
        )

        val catalog = assertIs<AppRoot.Child.Catalog>(root.stack.value.active.instance)
        val catalogComponent = (catalog.component.stack.value.active.instance as CatalogRoot.Child.List).component
        catalogComponent.onIntent(CatalogIntent.Account)

        val profile = assertIs<AppRoot.Child.Profile>(root.stack.value.active.instance)
        profile.component.onIntent(ProfileIntent.OpenCatalog)

        assertSame(catalog, root.stack.value.active.instance)
        assertEquals(1, root.stack.value.items.size)
    }
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

private class FakeAuthRepository : AuthRepository {
    override suspend fun register(registration: Registration) =
        RegisteredUser("user", registration.firstName, registration.lastName, registration.email)
}

private class FakeSessionRepository : SessionRepository {
    override suspend fun login(email: String, password: String) = SessionTokens("access", "refresh", 900)
    override suspend fun refresh() = SessionTokens("access", "refresh", 900)
    override suspend fun logout() = true
}
