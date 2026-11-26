package org.example.project.chat.data

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import org.example.project.chat.domain.ChatTemplate

class DataStoreLocalChatTemplateStore(private val dataStore: DataStore<Preferences>) : LocalChatTemplateStore {
    override suspend fun read(): List<ChatTemplate> = withContext(Dispatchers.IO) {
        dataStore.data.first()[templatesKey]?.toLocalTemplates().orEmpty()
    }

    override suspend fun save(templates: List<ChatTemplate>): Unit = withContext(Dispatchers.IO) {
        val json = templates.toStoredJson()
        dataStore.edit { it[templatesKey] = json }
    }

    override suspend fun readCategories(): List<String> = withContext(Dispatchers.IO) {
        dataStore.data.first()[categoriesKey]?.toLocalCategories().orEmpty()
    }

    override suspend fun saveCategories(categories: List<String>): Unit = withContext(Dispatchers.IO) {
        dataStore.edit { it[categoriesKey] = categories.toStoredCategoriesJson() }
    }

    companion object {
        private val templatesKey = stringPreferencesKey("templates")
        private val categoriesKey = stringPreferencesKey("categories")

        /** Retain one instance per file for the application lifetime. */
        fun create(
            path: String,
            readLegacy: suspend () -> String?,
            clearLegacy: suspend () -> Unit,
            scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        ): DataStoreLocalChatTemplateStore {
            return DataStoreLocalChatTemplateStore(
                PreferenceDataStoreFactory.createWithPath(
                    migrations = listOf(legacyMigration(readLegacy, clearLegacy)),
                    scope = scope,
                ) { path.toPath() },
            )
        }

        internal fun legacyMigration(
            readLegacy: suspend () -> String?,
            clearLegacy: suspend () -> Unit,
        ): DataMigration<Preferences> = object : DataMigration<Preferences> {
            override suspend fun shouldMigrate(currentData: Preferences) = readLegacy() != null

            override suspend fun migrate(currentData: Preferences): Preferences {
                if (currentData[templatesKey] != null) return currentData
                val json = readLegacy() ?: return currentData
                json.toLocalTemplates()
                return currentData.toMutablePreferences().apply { this[templatesKey] = json }
            }

            override suspend fun cleanUp() = clearLegacy()
        }
    }
}
