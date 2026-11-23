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

        component.updateFirstName("Ada")
        component.updateLastName(" ")
        component.updateEmail("ada@example.test")
        component.updatePassword("short")
        component.submit()

        assertEquals("Введите фамилию", component.state.value.errorMessage)
        assertFalse(repository.wasCalled)
    }

    @Test
    fun `shows conflict error returned by the repository`() = runTest {
        val repository = FakeAuthRepository(error = AuthException.EmailAlreadyRegistered())
        val component = component(repository, StandardTestDispatcher(testScheduler))

        component.updateFirstName("Ada")
        component.updateLastName("Lovelace")
        component.updateEmail("ada@example.test")
        component.updatePassword("long-password")
        component.submit()
        advanceUntilIdle()

        assertEquals("Этот email уже зарегистрирован", component.state.value.errorMessage)
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
