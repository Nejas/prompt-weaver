package org.example.project

import android.content.Context
import org.example.project.chat.data.DataStoreLocalChatTemplateStore
import org.example.project.chat.data.LocalChatTemplateStore

class AndroidLocalChatTemplateStore(context: Context) : LocalChatTemplateStore by getStore(context) {
    companion object {
        private var instance: LocalChatTemplateStore? = null
        @Synchronized
        private fun getStore(context: Context): LocalChatTemplateStore =
            instance ?: run {
                val app = context.applicationContext
                val legacy = app.getSharedPreferences("chat_templates", Context.MODE_PRIVATE)
                DataStoreLocalChatTemplateStore.create(
                    path = app.filesDir.resolve("chat_templates.preferences_pb").absolutePath,
                    readLegacy = { legacy.getString("templates", null) },
                    clearLegacy = { check(legacy.edit().remove("templates").commit()) { "Template migration cleanup failed" } },
                ).also { instance = it }
            }
    }
}
