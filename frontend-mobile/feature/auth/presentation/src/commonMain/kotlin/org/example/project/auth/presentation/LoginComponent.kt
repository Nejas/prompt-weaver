package org.example.project.auth.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.auth.domain.AuthException
import org.example.project.auth.domain.SessionRepository

data class LoginState(
    val email: String = "",
    val password: String = "",
    val submitting: Boolean = false,
    val error: AuthUiMessage? = null,
)

class LoginComponent(
    context: ComponentContext,
    private val sessions: SessionRepository,
    dispatcher: CoroutineDispatcher,
    private val onLoggedIn: () -> Unit,
    val back: () -> Unit,
    private val onRegister: () -> Unit = {},
) : ComponentContext by context {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutableState = MutableStateFlow(LoginState())
    val state = mutableState.asStateFlow()

    init { lifecycle.doOnDestroy { scope.cancel() } }

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UpdateEmail -> updateEmail(intent.value)
            is LoginIntent.UpdatePassword -> updatePassword(intent.value)
            LoginIntent.Submit -> submit()
            LoginIntent.Back -> back()
            LoginIntent.Register -> onRegister()
        }
    }

    fun updateEmail(value: String) { mutableState.value = mutableState.value.copy(email = value, error = null) }
    fun updatePassword(value: String) { mutableState.value = mutableState.value.copy(password = value, error = null) }
    fun cancelRequests() { scope.cancel() }

    fun submit() {
        val current = state.value
        if (current.submitting) return
        if (current.email.isBlank() || current.password.isBlank()) {
            mutableState.value = current.copy(error = AuthUiMessage.EmptyCredentials)
            return
        }
        mutableState.value = current.copy(submitting = true, error = null)
        scope.launch {
            try {
                sessions.login(current.email, current.password)
                mutableState.value = mutableState.value.copy(password = "", submitting = false)
                onLoggedIn()
            } catch (error: CancellationException) { throw error }
            catch (_: AuthException.Unauthorized) { fail(AuthUiMessage.InvalidCredentials) }
            catch (_: AuthException.Unavailable) { fail(AuthUiMessage.ServiceUnavailable) }
            catch (_: Exception) { fail(AuthUiMessage.LoginFailed) }
        }
    }

    private fun fail(message: AuthUiMessage) {
        mutableState.value = mutableState.value.copy(password = "", submitting = false, error = message)
    }
}
