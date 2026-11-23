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

    fun updateFirstName(value: String) = update { copy(firstName = value, errorMessage = null) }
    fun updateLastName(value: String) = update { copy(lastName = value, errorMessage = null) }
    fun updateEmail(value: String) = update { copy(email = value, errorMessage = null) }
    fun updatePassword(value: String) = update { copy(password = value, errorMessage = null) }
    fun back() = onBack()
    fun login() = onLogin()

    fun submit() {
        val current = state.value
        if (current.isSubmitting) return
        val validationError = validate(current)
        if (validationError != null) {
            update { copy(errorMessage = validationError) }
            return
        }
        mutableState.value = current.copy(isSubmitting = true, errorMessage = null)
        scope.launch {
            try {
                repository.register(Registration(current.firstName, current.lastName, current.email, current.password))
                onRegistered()
            } catch (error: CancellationException) {
                throw error
            } catch (error: AuthException.InvalidRegistration) {
                update { copy(isSubmitting = false, errorMessage = "Проверьте введённые данные") }
            } catch (error: AuthException.EmailAlreadyRegistered) {
                update { copy(isSubmitting = false, errorMessage = "Этот email уже зарегистрирован") }
            } catch (error: AuthException.RegistrationSessionNotSaved) {
                update { copy(isSubmitting = false, errorMessage = "Аккаунт создан, но сессию сохранить не удалось. Войдите в аккаунт.") }
            } catch (error: AuthException.Unavailable) {
                update { copy(isSubmitting = false, errorMessage = "Сервер недоступен. Попробуйте позже") }
            } catch (error: AuthException) {
                update { copy(isSubmitting = false, errorMessage = "Не удалось зарегистрироваться") }
            } finally {
                update { copy(password = "") }
            }
        }
    }

    private fun validate(current: RegistrationState): String? = when {
        current.firstName.isBlank() -> "Введите имя"
        current.lastName.isBlank() -> "Введите фамилию"
        current.email.isBlank() || !current.email.contains('@') -> "Введите корректный email"
        current.password.length < 8 -> "Пароль должен содержать минимум 8 символов"
        else -> null
    }

    private fun update(transform: RegistrationState.() -> RegistrationState) {
        mutableState.value = mutableState.value.transform()
    }
}
