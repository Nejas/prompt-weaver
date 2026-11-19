package org.example.project.catalog.domain

data class TemplateParameter(
    val name: String,
    val label: String,
    val type: String,
    val required: Boolean,
    val maxLength: Int?,
    val options: List<String>,
)
