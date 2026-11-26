package org.example.project.auth.presentation

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.decompose.DefaultComponentContext
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.example.project.auth.domain.AuthException
import org.example.project.auth.domain.AuthRepository
import org.example.project.auth.domain.RegisteredUser
import org.example.project.auth.domain.Registration

class RegistrationComponentTest {
    @Test
    fun `requires name surname and an eight character password before request`() = runTest {
        val repository = FakeAuthRepository()
        val component = component(repository)

        component.onIntent(RegistrationIntent.UpdateFirstName("Ada"))
        component.onIntent(RegistrationIntent.UpdateLastName(" "))
        component.onIntent(RegistrationIntent.UpdateEmail("ada@example.test"))
        component.onIntent(RegistrationIntent.UpdatePassword("short"))
        component.onIntent(RegistrationIntent.Submit)

        assertEquals(AuthUiMessage.LastNameRequired, component.state.value.error)
        assertFalse(repository.wasCalled)
    }

    @Test
    fun `shows conflict error returned by the repository`() = runTest {
        val repository = FakeAuthRepository(error = AuthException.EmailAlreadyRegistered())
        val component = component(repository, StandardTestDispatcher(testScheduler))

        component.onIntent(RegistrationIntent.UpdateFirstName("Ada"))
        component.onIntent(RegistrationIntent.UpdateLastName("Lovelace"))
        component.onIntent(RegistrationIntent.UpdateEmail("ada@example.test"))
        component.onIntent(RegistrationIntent.UpdatePassword("long-password"))
        component.onIntent(RegistrationIntent.Submit)
        advanceUntilIdle()

        assertEquals(AuthUiMessage.EmailAlreadyRegistered, component.state.value.error)
    }

    private fun component(
        repository: AuthRepository,
        dispatcher: TestDispatcher = StandardTestDispatcher(),
    ) = RegistrationComponent(
        DefaultComponentContext(LifecycleRegistry()), repository, dispatcher,
        onRegistered = {}, onBack = {},
    )

    private class FakeAuthRepository(
        private val error: AuthException? = null,
    ) : AuthRepository {
        var wasCalled = false

        override suspend fun register(registration: Registration): RegisteredUser {
            wasCalled = true
            error?.let { throw it }
            return RegisteredUser("user-1", registration.firstName, registration.lastName, registration.email)
        }
    }
}
