package org.example.project.catalog.domain

/** Идентификатор шаблона в каталоге соответствует опубликованной версии. */
data class TemplateSummary(
    val versionId: String,
    val title: String,
    val description: String,
    val category: String,
    val slug: String,
)
