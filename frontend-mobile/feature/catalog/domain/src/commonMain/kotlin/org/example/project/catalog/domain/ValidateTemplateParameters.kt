package org.example.project.catalog.domain

/** Предварительная проверка формы; окончательное решение принимает сервер. */
fun validateTemplateParameters(parameters: List<TemplateParameter>, values: Map<String, String>): List<ParameterValidationError> =
    parameters.mapNotNull { parameter ->
        val value = values[parameter.name].orEmpty()
        when {
            value.isBlank() && parameter.required -> ParameterValidationError.Required(parameter.name)
            value.isBlank() -> null
            parameter.type == "choice" && value !in parameter.options -> ParameterValidationError.InvalidChoice(parameter.name)
            else -> null
        }
    }
