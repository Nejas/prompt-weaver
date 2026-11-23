package org.example.project.auth.domain

interface SessionRepository {
    suspend fun login(email: String, password: String): SessionTokens
    suspend fun refresh(): SessionTokens?
    /** False means the local session was removed, but server revocation was not confirmed. */
    suspend fun logout(): Boolean
}
