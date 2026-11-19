package org.example.project.catalog.domain

sealed interface ParameterValidationError {
    val parameterName: String
    data class Required(override val parameterName: String) : ParameterValidationError
    data class InvalidChoice(override val parameterName: String) : ParameterValidationError
}
