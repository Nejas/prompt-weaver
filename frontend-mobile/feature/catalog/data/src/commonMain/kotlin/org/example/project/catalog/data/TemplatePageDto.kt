package org.example.project.catalog.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.example.project.catalog.domain.*

@Serializable
internal data class TemplatePageDto(val templates: List<TemplateSummaryDto>, val limit: Int, val offset: Int) {
    fun toDomain() = TemplatePage(templates.map { it.toDomain() }, limit, offset)
}
