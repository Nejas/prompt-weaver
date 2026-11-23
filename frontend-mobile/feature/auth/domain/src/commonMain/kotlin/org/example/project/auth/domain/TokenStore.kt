package org.example.project.auth.domain

/** Stores or replaces the complete token pair as one unit. */
interface TokenStore {
    suspend fun read(): SessionTokens?
    suspend fun write(tokens: SessionTokens)
    suspend fun clear()
}
