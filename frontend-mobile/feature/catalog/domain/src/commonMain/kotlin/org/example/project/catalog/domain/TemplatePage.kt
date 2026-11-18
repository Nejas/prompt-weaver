package org.example.project.catalog.domain

data class TemplatePage(
    val templates: List<TemplateSummary>,
    val limit: Int,
    val offset: Int,
)
