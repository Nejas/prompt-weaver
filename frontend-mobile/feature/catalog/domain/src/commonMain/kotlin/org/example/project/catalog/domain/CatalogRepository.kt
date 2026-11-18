package org.example.project.catalog.domain

/** Операции с опубликованными версиями шаблонов. */
interface CatalogRepository {
    suspend fun listTemplates(limit: Int = 50, offset: Int = 0): TemplatePage

    suspend fun getTemplate(versionId: String): TemplateDetails

    /** Собирает промпт без дополнительного набора правил. */
    suspend fun composePrompt(versionId: String, parameters: Map<String, String>): ComposedPrompt
}
