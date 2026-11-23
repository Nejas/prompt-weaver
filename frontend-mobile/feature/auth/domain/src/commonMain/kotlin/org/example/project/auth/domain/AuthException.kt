package org.example.project.auth.domain

sealed class AuthException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    class InvalidRegistration : AuthException()
    class EmailAlreadyRegistered : AuthException()
    class Unauthorized : AuthException()
    class RegistrationSessionNotSaved(cause: Throwable) : AuthException(cause = cause)
    class Unavailable(cause: Throwable? = null) : AuthException(cause = cause)
    class Unexpected(cause: Throwable? = null) : AuthException(cause = cause)
}
