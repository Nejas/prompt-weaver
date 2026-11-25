package org.example.project.auth.presentation

import androidx.compose.runtime.Composable
import org.example.project.auth.presentation.generated.resources.Res
import org.example.project.auth.presentation.generated.resources.auth_error_email_registered
import org.example.project.auth.presentation.generated.resources.auth_error_empty_credentials
import org.example.project.auth.presentation.generated.resources.auth_error_first_name_required
import org.example.project.auth.presentation.generated.resources.auth_error_invalid_credentials
import org.example.project.auth.presentation.generated.resources.auth_error_invalid_email
import org.example.project.auth.presentation.generated.resources.auth_error_invalid_registration
import org.example.project.auth.presentation.generated.resources.auth_error_last_name_required
import org.example.project.auth.presentation.generated.resources.auth_error_login_failed
import org.example.project.auth.presentation.generated.resources.auth_error_logout_failed
import org.example.project.auth.presentation.generated.resources.auth_error_password_too_short
import org.example.project.auth.presentation.generated.resources.auth_error_registration_failed
import org.example.project.auth.presentation.generated.resources.auth_error_registration_session_not_saved
import org.example.project.auth.presentation.generated.resources.auth_error_service_unavailable
import org.example.project.auth.presentation.generated.resources.auth_notice_logout_not_confirmed
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthUiMessage.asText(): String = stringResource(
    when (this) {
        AuthUiMessage.EmptyCredentials -> Res.string.auth_error_empty_credentials
        AuthUiMessage.InvalidCredentials -> Res.string.auth_error_invalid_credentials
        AuthUiMessage.ServiceUnavailable -> Res.string.auth_error_service_unavailable
        AuthUiMessage.LoginFailed -> Res.string.auth_error_login_failed
        AuthUiMessage.FirstNameRequired -> Res.string.auth_error_first_name_required
        AuthUiMessage.LastNameRequired -> Res.string.auth_error_last_name_required
        AuthUiMessage.InvalidEmail -> Res.string.auth_error_invalid_email
        AuthUiMessage.PasswordTooShort -> Res.string.auth_error_password_too_short
        AuthUiMessage.InvalidRegistration -> Res.string.auth_error_invalid_registration
        AuthUiMessage.EmailAlreadyRegistered -> Res.string.auth_error_email_registered
        AuthUiMessage.RegistrationSessionNotSaved -> Res.string.auth_error_registration_session_not_saved
        AuthUiMessage.RegistrationFailed -> Res.string.auth_error_registration_failed
        AuthUiMessage.LogoutNotConfirmed -> Res.string.auth_notice_logout_not_confirmed
        AuthUiMessage.LogoutFailed -> Res.string.auth_error_logout_failed
    },
)
