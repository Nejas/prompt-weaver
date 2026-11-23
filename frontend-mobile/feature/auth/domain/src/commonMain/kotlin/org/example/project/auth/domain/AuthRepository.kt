package org.example.project.auth.domain

interface AuthRepository {
    suspend fun register(registration: Registration): RegisteredUser
}
