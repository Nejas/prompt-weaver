package org.example.project.catalog.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidateTemplateParametersTest {
    private val parameter = TemplateParameter("topic", "Тема", "choice", true, null, listOf("A", "B"))

    @Test fun requiredAndOptionalEmpty() {
        assertEquals(listOf(ParameterValidationError.Required("topic")), validateTemplateParameters(listOf(parameter), mapOf("topic" to "  ")))
        assertTrue(validateTemplateParameters(listOf(parameter.copy(required = false)), emptyMap()).isEmpty())
    }

    @Test fun choiceMustMatchExactly() {
        assertEquals(listOf(ParameterValidationError.InvalidChoice("topic")), validateTemplateParameters(listOf(parameter), mapOf("topic" to " A ")))
        assertTrue(validateTemplateParameters(listOf(parameter), mapOf("topic" to "A")).isEmpty())
    }

    @Test fun lengthLimitIsChecked() {
        val text = parameter.copy(type = "text", maxLength = 3)
        assertTrue(validateTemplateParameters(listOf(text), mapOf("topic" to "abc")).isEmpty())
        assertEquals(listOf(ParameterValidationError.TooLong("topic", 3)), validateTemplateParameters(listOf(text), mapOf("topic" to "abcd")))
        assertTrue(validateTemplateParameters(listOf(text.copy(maxLength = 1)), mapOf("topic" to "😀")).isEmpty())
    }
}
