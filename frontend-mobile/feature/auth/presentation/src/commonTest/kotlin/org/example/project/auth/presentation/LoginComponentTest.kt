package org.example.project.auth.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.example.project.auth.domain.AuthException
import org.example.project.auth.domain.SessionRepository
import org.example.project.auth.domain.SessionTokens
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class LoginComponentTest {
    @Test fun successfulLoginClearsPasswordAndNavigates() = runTest {
        val sessions = FakeSessions()
        var navigated = false
        val component = LoginComponent(DefaultComponentContext(LifecycleRegistry()), sessions,
            StandardTestDispatcher(testScheduler), { navigated = true }, {})
        component.onIntent(LoginIntent.UpdateEmail("user@example.com"))
        component.onIntent(LoginIntent.UpdatePassword("secret"))
        component.onIntent(LoginIntent.Submit)
        testScheduler.runCurrent()
        assertEquals(1, sessions.calls)
        assertEquals("", component.state.value.password)
        assertEquals(true, navigated)
    }

    @Test fun failedLoginClearsPasswordAndAllowsRetry() = runTest {
        val sessions = FakeSessions().apply { fail = true }
        val component = LoginComponent(DefaultComponentContext(LifecycleRegistry()), sessions,
            StandardTestDispatcher(testScheduler), {}, {})
        component.onIntent(LoginIntent.UpdateEmail("user@example.com"))
        component.onIntent(LoginIntent.UpdatePassword("wrong"))
        component.onIntent(LoginIntent.Submit)
        testScheduler.runCurrent()
        assertEquals(AuthUiMessage.InvalidCredentials, component.state.value.error)
        assertEquals("", component.state.value.password)
        assertFalse(component.state.value.submitting)
    }

    @Test fun registerIntentNavigatesToRegistration() {
        var registerOpened = false
        val component = LoginComponent(
            DefaultComponentContext(LifecycleRegistry()),
            FakeSessions(),
            StandardTestDispatcher(),
            onLoggedIn = {},
            back = {},
            onRegister = { registerOpened = true },
        )

        component.onIntent(LoginIntent.Register)

        assertEquals(true, registerOpened)
    }
}

private class FakeSessions : SessionRepository {
    var calls = 0
    var fail = false
    override suspend fun login(email: String, password: String): SessionTokens {
        calls++
        if (fail) throw AuthException.Unauthorized()
        return SessionTokens("access", "refresh", 900)
    }
    override suspend fun refresh(): SessionTokens? = null
    override suspend fun logout() = true
}
