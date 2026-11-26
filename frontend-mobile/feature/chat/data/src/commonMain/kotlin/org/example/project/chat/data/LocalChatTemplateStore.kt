package org.example.project.chat.data
import org.example.project.chat.domain.ChatTemplate
interface LocalChatTemplateStore {
    suspend fun read(): List<ChatTemplate>
    suspend fun save(templates: List<ChatTemplate>)
    suspend fun readCategories(): List<String>
    suspend fun saveCategories(categories: List<String>)
}
