package org.example.project.catalog.domain

data class TemplateDetails(
    val summary: TemplateSummary,
    val body: String,
    val parameters: List<TemplateParameter>,
)
