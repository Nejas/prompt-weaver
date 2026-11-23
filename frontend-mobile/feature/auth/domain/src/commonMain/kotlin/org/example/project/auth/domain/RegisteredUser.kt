package org.example.project.auth.domain

data class RegisteredUser(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
)
