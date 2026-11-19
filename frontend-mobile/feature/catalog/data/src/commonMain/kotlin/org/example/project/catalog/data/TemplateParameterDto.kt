package org.example.project.catalog.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.example.project.catalog.domain.*

@Serializable
internal data class TemplateParameterDto(val name: String, val label: String, val type: String, val required: Boolean, @SerialName("max_length") val maxLength: Int?, val options: List<String>) {
    fun toDomain() = TemplateParameter(name, label, type, required, maxLength, options)
}
