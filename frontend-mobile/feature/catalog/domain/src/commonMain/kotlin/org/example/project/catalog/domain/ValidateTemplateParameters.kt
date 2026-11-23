package org.example.project.catalog.domain

/** Предварительная проверка формы; окончательное решение принимает сервер. */
fun validateTemplateParameters(parameters: List<TemplateParameter>, values: Map<String, String>): List<ParameterValidationError> =
    parameters.mapNotNull { parameter ->
        val value = values[parameter.name].orEmpty()
        val maxLength = minOf(parameter.maxLength?.takeUnless { it == 0 } ?: 10_000, 10_000)
        when {
            value.isBlank() && parameter.required -> ParameterValidationError.Required(parameter.name)
            value.isBlank() -> null
            value.codePointLength() > maxLength -> ParameterValidationError.TooLong(parameter.name, maxLength)
            parameter.type == "choice" && value !in parameter.options -> ParameterValidationError.InvalidChoice(parameter.name)
            else -> null
        }
    }

private fun String.codePointLength(): Int {
    var count = 0
    var index = 0
    while (index < length) {
        if (this[index].isHighSurrogate() && index + 1 < length && this[index + 1].isLowSurrogate()) index++
        index++
        count++
    }
    return count
}
