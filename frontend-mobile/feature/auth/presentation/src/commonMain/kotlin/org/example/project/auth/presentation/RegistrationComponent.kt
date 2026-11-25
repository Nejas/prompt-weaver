package org.example.project.auth.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.auth.domain.AuthException
import org.example.project.auth.domain.AuthRepository
import org.example.project.auth.domain.Registration

class RegistrationComponent(
    componentContext: ComponentContext,
    private val repository: AuthRepository,
    mainDispatcher: CoroutineDispatcher,
    private val onRegistered: () -> Unit,
    private val onBack: () -> Unit,
    private val onLogin: () -> Unit = {},
) : ComponentContext by componentContext {
    private val scope = CoroutineScope(SupervisorJob() + mainDispatcher)
    private val mutableState = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = mutableState.asStateFlow()

    init { lifecycle.doOnDestroy { scope.cancel() } }

    fun onIntent(intent: RegistrationIntent) {
        when (intent) {
            is RegistrationIntent.UpdateFirstName -> updateFirstName(intent.value)
            is RegistrationIntent.UpdateLastName -> updateLastName(intent.value)
            is RegistrationIntent.UpdateEmail -> updateEmail(intent.value)
            is RegistrationIntent.UpdatePassword -> updatePassword(intent.value)
            RegistrationIntent.Submit -> submit()
            RegistrationIntent.Back -> back()
            RegistrationIntent.Login -> login()
        }
    }

    fun updateFirstName(value: String) = update { copy(firstName = value, error = null) }
    fun updateLastName(value: String) = update { copy(lastName = value, error = null) }
    fun updateEmail(value: String) = update { copy(email = value, error = null) }
    fun updatePassword(value: String) = update { copy(password = value, error = null) }
    fun cancelRequests() { scope.cancel() }
    fun back() = onBack()
    fun login() = onLogin()

    fun submit() {
        val current = state.value
        if (current.isSubmitting) return
        val validationError = validate(current)
        if (validationError != null) {
            update { copy(error = validationError) }
            return
        }
        mutableState.value = current.copy(isSubmitting = true, error = null)
        scope.launch {
            try {
                repository.register(Registration(current.firstName, current.lastName, current.email, current.password))
                onRegistered()
            } catch (error: CancellationException) {
                throw error
            } catch (error: AuthException.InvalidRegistration) {
                update { copy(isSubmitting = false, error = AuthUiMessage.InvalidRegistration) }
            } catch (error: AuthException.EmailAlreadyRegistered) {
                update { copy(isSubmitting = false, error = AuthUiMessage.EmailAlreadyRegistered) }
            } catch (error: AuthException.RegistrationSessionNotSaved) {
                update { copy(isSubmitting = false, error = AuthUiMessage.RegistrationSessionNotSaved) }
            } catch (error: AuthException.Unavailable) {
                update { copy(isSubmitting = false, error = AuthUiMessage.ServiceUnavailable) }
            } catch (error: AuthException) {
                update { copy(isSubmitting = false, error = AuthUiMessage.RegistrationFailed) }
            } finally {
                update { copy(password = "") }
            }
        }
    }

    private fun validate(current: RegistrationState): AuthUiMessage? = when {
        current.firstName.isBlank() -> AuthUiMessage.FirstNameRequired
        current.lastName.isBlank() -> AuthUiMessage.LastNameRequired
        current.email.isBlank() || !current.email.contains('@') -> AuthUiMessage.InvalidEmail
        current.password.length < 8 -> AuthUiMessage.PasswordTooShort
        else -> null
    }

    private fun update(transform: RegistrationState.() -> RegistrationState) {
        mutableState.value = mutableState.value.transform()
    }
}
