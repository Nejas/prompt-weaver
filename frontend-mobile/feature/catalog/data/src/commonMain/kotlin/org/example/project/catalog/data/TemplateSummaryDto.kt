package org.example.project.catalog.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.example.project.catalog.domain.*

@Serializable
internal data class TemplateSummaryDto(val id: String, val title: String, val description: String, val category: String, val slug: String) {
    fun toDomain() = TemplateSummary(id, title, description, category, slug)
}
