package org.example.project.auth.presentation

sealed interface SessionIntent {
    data object Logout : SessionIntent
    data object ContinueAfterLogout : SessionIntent
    data object OpenChat : SessionIntent
}
