package org.example.project.network

/**
 * Платформенный журнал сетевого слоя.
 * Сообщения намеренно не содержат заголовки, query-параметры или тела запросов.
 */
internal expect object NetworkLog {
    fun debug(message: String)
    fun error(message: String)
}
