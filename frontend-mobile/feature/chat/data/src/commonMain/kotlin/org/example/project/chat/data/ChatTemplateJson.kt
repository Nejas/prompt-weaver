package org.example.project.chat.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.chat.domain.ChatTemplate

@Serializable
data class StoredChatTemplate(val id: String, val title: String, val description: String, val category: String)
fun List<ChatTemplate>.toStoredJson(): String = Json.encodeToString(map { StoredChatTemplate(it.id, it.title, it.description, it.category) })
fun String.toLocalTemplates(): List<ChatTemplate> = Json.decodeFromString<List<StoredChatTemplate>>(this).map { ChatTemplate(it.id, it.title, it.description, it.category, true) }
fun List<String>.toStoredCategoriesJson(): String = Json.encodeToString(this)
fun String.toLocalCategories(): List<String> = Json.decodeFromString(this)
