package org.example.project.catalog.presentation

import org.example.project.catalog.domain.ParameterValidationError
import org.example.project.catalog.domain.TemplateDetails

sealed interface DetailsState {
    data object Loading : DetailsState
    data object Error : DetailsState
    data class Loaded(
        val template: TemplateDetails,
        val values: Map<String, String> = emptyMap(),
        val errors: List<ParameterValidationError> = emptyList(),
        val validated: Boolean = false,
    ) : DetailsState
}
