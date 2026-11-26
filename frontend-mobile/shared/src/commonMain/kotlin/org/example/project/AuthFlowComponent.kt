package org.example.project

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.project.auth.domain.AuthRepository
import org.example.project.auth.domain.SessionRepository
import org.example.project.auth.presentation.LoginComponent
import org.example.project.auth.presentation.RegistrationComponent

sealed interface AuthFlowState {
    data class Login(val component: LoginComponent) : AuthFlowState
    data class Registration(val component: RegistrationComponent) : AuthFlowState
}

class AuthFlowComponent(
    private val context: ComponentContext,
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val dispatcher: CoroutineDispatcher,
    private val onAuthenticated: () -> Unit,
) : ComponentContext by context {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var registrationCheck: Job? = null
    private val mutableState = MutableStateFlow<AuthFlowState>(createLogin())
    val state = mutableState.asStateFlow()

    init { lifecycle.doOnDestroy { scope.cancel() } }

    private fun finishRegistration() {
        registrationCheck = scope.launch {
            try {
                val tokens = sessionRepository.refresh()
                ensureActive()
                if (tokens != null) onAuthenticated()
                else showLogin()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                showLogin()
            }
        }
    }

    private fun showLogin() {
        cancelActiveRequest()
        mutableState.value = createLogin()
    }

    private fun showRegistration() {
        cancelActiveRequest()
        mutableState.value = createRegistration()
    }

    private fun cancelActiveRequest() {
        registrationCheck?.cancel()
        when (val current = mutableState.value) {
            is AuthFlowState.Login -> current.component.cancelRequests()
            is AuthFlowState.Registration -> current.component.cancelRequests()
        }
    }

    private fun createLogin(): AuthFlowState.Login = AuthFlowState.Login(
        LoginComponent(
            context = context,
            sessions = sessionRepository,
            dispatcher = dispatcher,
            onLoggedIn = onAuthenticated,
            back = {},
            onRegister = ::showRegistration,
        ),
    )

    private fun createRegistration(): AuthFlowState.Registration = AuthFlowState.Registration(
        RegistrationComponent(
            componentContext = context,
            repository = authRepository,
            mainDispatcher = dispatcher,
            onRegistered = ::finishRegistration,
            onBack = ::showLogin,
            onLogin = ::showLogin,
        ),
    )
}
