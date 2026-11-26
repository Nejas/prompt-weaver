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
import org.example.project.auth.domain.SessionRepository

data class SessionState(val leaving: Boolean = false, val loggedOut: Boolean = false, val notice: AuthUiMessage? = null)

class SessionComponent(
    context: ComponentContext,
    private val sessions: SessionRepository,
    dispatcher: CoroutineDispatcher,
    private val onLoggedOut: () -> Unit,
    private val openChat: () -> Unit,
) : ComponentContext by context {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutableState = MutableStateFlow(SessionState())
    val state = mutableState.asStateFlow()

    init { lifecycle.doOnDestroy { scope.cancel() } }

    fun onIntent(intent: SessionIntent) {
        when (intent) {
            SessionIntent.Logout -> logout()
            SessionIntent.ContinueAfterLogout -> continueAfterLogout()
            SessionIntent.OpenChat -> if (!state.value.leaving && !state.value.loggedOut) openChat()
        }
    }

    fun logout() {
        if (state.value.leaving) return
        mutableState.value = SessionState(leaving = true)
        scope.launch {
            try {
                val revoked = sessions.logout()
                if (revoked) onLoggedOut()
                else mutableState.value = SessionState(loggedOut = true, notice = AuthUiMessage.LogoutNotConfirmed)
            } catch (error: CancellationException) { throw error }
            catch (_: Exception) { mutableState.value = SessionState(notice = AuthUiMessage.LogoutFailed) }
        }
    }

    fun continueAfterLogout() { if (state.value.loggedOut) onLoggedOut() }
}
