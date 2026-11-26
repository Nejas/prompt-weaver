package org.example.project.auth.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.example.project.auth.domain.SessionRepository
import org.example.project.auth.domain.SessionTokens
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionComponentTest {
    @Test fun unconfirmedRevocationIsShownBeforeLeaving() = runTest {
        var left = false
        val component = SessionComponent(DefaultComponentContext(LifecycleRegistry()), FakeSessions(),
            StandardTestDispatcher(testScheduler), { left = true }, {})
        component.onIntent(SessionIntent.Logout)
        testScheduler.runCurrent()
        assertEquals(false, left)
        assertEquals(true, component.state.value.loggedOut)
        component.onIntent(SessionIntent.ContinueAfterLogout)
        assertEquals(true, left)
    }

    private class FakeSessions : SessionRepository {
        override suspend fun login(email: String, password: String) = SessionTokens("a", "r", 900)
        override suspend fun refresh(): SessionTokens? = null
        override suspend fun logout() = false
    }
}
