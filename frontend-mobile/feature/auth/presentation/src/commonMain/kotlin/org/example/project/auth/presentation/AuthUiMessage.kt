package org.example.project.auth.presentation

sealed interface AuthUiMessage {
    data object EmptyCredentials : AuthUiMessage
    data object InvalidCredentials : AuthUiMessage
    data object ServiceUnavailable : AuthUiMessage
    data object LoginFailed : AuthUiMessage
    data object FirstNameRequired : AuthUiMessage
    data object LastNameRequired : AuthUiMessage
    data object InvalidEmail : AuthUiMessage
    data object PasswordTooShort : AuthUiMessage
    data object InvalidRegistration : AuthUiMessage
    data object EmailAlreadyRegistered : AuthUiMessage
    data object RegistrationSessionNotSaved : AuthUiMessage
    data object RegistrationFailed : AuthUiMessage
    data object LogoutNotConfirmed : AuthUiMessage
    data object LogoutFailed : AuthUiMessage
}
