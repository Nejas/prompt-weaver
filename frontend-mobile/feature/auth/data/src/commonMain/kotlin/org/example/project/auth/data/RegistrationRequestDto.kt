package org.example.project.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.auth.domain.Registration

@Serializable
internal class RegistrationRequestDto(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String,
    val password: String,
) {
    companion object {
        fun from(registration: Registration) = RegistrationRequestDto(
            firstName = registration.firstName.trim(),
            lastName = registration.lastName.trim(),
            email = registration.email.trim(),
            password = registration.password,
        )
    }
}
