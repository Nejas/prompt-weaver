package org.example.project.chat.domain
interface ChatTemplateRepository {
    suspend fun categories(): List<ChatTemplateCategory>

    suspend fun createCategory(name: String)

    suspend fun createTemplate(draft: ChatTemplateDraft)
}
