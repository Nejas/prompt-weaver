package org.example.project.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.auth.domain.RegisteredUser

@Serializable
internal class RegisteredUserDto(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("token_type") val tokenType: String,
) {
    fun toDomain() = RegisteredUser(id, firstName, lastName, email)
    fun tokens(): org.example.project.auth.domain.SessionTokens {
        require(tokenType.equals("bearer", ignoreCase = true)) { "Unsupported token type" }
        require(accessToken.isNotBlank() && refreshToken.isNotBlank() && expiresIn >= 0) { "Invalid token pair" }
        return org.example.project.auth.domain.SessionTokens(accessToken, refreshToken, expiresIn)
    }
}
