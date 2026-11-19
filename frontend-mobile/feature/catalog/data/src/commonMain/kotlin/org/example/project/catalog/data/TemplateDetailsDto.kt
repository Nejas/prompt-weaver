package org.example.project.catalog.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.example.project.catalog.domain.*

@Serializable
internal data class TemplateDetailsDto(val id: String, val title: String, val description: String, val category: String, val slug: String, val body: String, val parameters: List<TemplateParameterDto>) {
    fun toDomain() = TemplateDetails(TemplateSummary(id, title, description, category, slug), body, parameters.map { it.toDomain() })
}
