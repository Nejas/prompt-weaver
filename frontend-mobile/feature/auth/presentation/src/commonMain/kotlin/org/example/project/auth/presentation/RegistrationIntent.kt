package org.example.project.auth.presentation

sealed interface RegistrationIntent {
    data class UpdateFirstName(val value: String) : RegistrationIntent
    data class UpdateLastName(val value: String) : RegistrationIntent
    data class UpdateEmail(val value: String) : RegistrationIntent
    data class UpdatePassword(val value: String) : RegistrationIntent
    data object Submit : RegistrationIntent
    data object Back : RegistrationIntent
    data object Login : RegistrationIntent
}
