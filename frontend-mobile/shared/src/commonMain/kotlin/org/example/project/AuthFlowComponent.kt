package org.example.project

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.CoroutineDispatcher
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
    private val mutableState = MutableStateFlow<AuthFlowState>(createLogin())
    val state = mutableState.asStateFlow()

    private fun showLogin() {
        cancelActiveRequest()
        mutableState.value = createLogin()
    }

    private fun showRegistration() {
        cancelActiveRequest()
        mutableState.value = createRegistration()
    }

    private fun cancelActiveRequest() {
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
            onRegistered = onAuthenticated,
            onBack = ::showLogin,
            onLogin = ::showLogin,
        ),
    )
}
