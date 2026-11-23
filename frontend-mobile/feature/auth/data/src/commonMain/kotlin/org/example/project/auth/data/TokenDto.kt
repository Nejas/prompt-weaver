package org.example.project.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.auth.domain.SessionTokens

@Serializable
internal class TokenDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("token_type") val tokenType: String,
) {
    fun toDomain(): SessionTokens {
        require(tokenType.equals("bearer", ignoreCase = true)) { "Unsupported token type" }
        require(accessToken.isNotBlank() && refreshToken.isNotBlank() && expiresIn >= 0) { "Invalid token pair" }
        return SessionTokens(accessToken, refreshToken, expiresIn)
    }
}

@Serializable
internal class RefreshRequest(@SerialName("refresh_token") val refreshToken: String)

@Serializable
internal class LoginRequest(val email: String, val password: String)
