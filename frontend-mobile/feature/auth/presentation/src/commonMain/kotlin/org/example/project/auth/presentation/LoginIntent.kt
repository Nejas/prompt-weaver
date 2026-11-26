package org.example.project.auth.presentation

sealed interface LoginIntent {
    data class UpdateEmail(val value: String) : LoginIntent
    data class UpdatePassword(val value: String) : LoginIntent
    data object Submit : LoginIntent
    data object Back : LoginIntent
    data object Register : LoginIntent
}
