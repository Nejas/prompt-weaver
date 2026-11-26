package org.example.project.chat.data

import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.example.project.chat.domain.ChatTemplate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith
import kotlinx.serialization.SerializationException
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.PreferencesSerializer
import okio.FileSystem
import okio.Path.Companion.toPath

class DataStoreLocalChatTemplateStoreTest {
    @Test
    fun invalidLegacyJsonIsNotDeleted() = runBlocking {
        val directory = Files.createTempDirectory("chat-template-migration").toFile()
        val job = SupervisorJob()
        var legacy: String? = "invalid json"
        val store = createTestStore(
            directory.resolve("chat.preferences_pb").absolutePath,
            { legacy }, { legacy = null }, CoroutineScope(job + Dispatchers.IO),
        )
        try {
            assertFailsWith<SerializationException> { store.read() }
            assertEquals("invalid json", legacy)
        } finally {
            job.cancelAndJoin()
            directory.deleteRecursively()
        }
    }

    @Test
    fun migratesLegacyDataAndPersistsReplacementAcrossReopening() = runBlocking {
        val directory = Files.createTempDirectory("chat-template-store").toFile()
        val path = directory.resolve("chat.preferences_pb").absolutePath
        val original = listOf(ChatTemplate("old", "Old", "", "", true))
        val replacement = listOf(ChatTemplate("new", "New", "", "", true))
        var legacy: String? = original.toStoredJson()
        suspend fun useStore(block: suspend (LocalChatTemplateStore) -> Unit) {
            val job = SupervisorJob()
            val store = createTestStore(
                path, { legacy }, { legacy = null }, CoroutineScope(job + Dispatchers.IO),
            )
            try { block(store) } finally { job.cancelAndJoin() }
        }
        try {
            useStore {
                assertEquals(original, it.read())
                assertNull(legacy)
                it.save(replacement)
                it.saveCategories(listOf("Personal"))
            }
            // A stale legacy copy must not overwrite the already persisted data.
            legacy = original.toStoredJson()
            useStore {
                assertEquals(replacement, it.read())
                assertEquals(listOf("Personal"), it.readCategories())
                assertNull(legacy)
                it.save(emptyList())
            }
            useStore { assertEquals(emptyList(), it.read()) }
        } finally {
            directory.deleteRecursively()
        }
    }

    // Android FileStorage relies on Android's file replacement API, unavailable in Windows host tests.
    // Keep real on-disk serialization and the production migration, using Okio's JVM file replacement.
    private fun createTestStore(
        path: String,
        readLegacy: suspend () -> String?,
        clearLegacy: suspend () -> Unit,
        scope: CoroutineScope,
    ) = DataStoreLocalChatTemplateStore(
        PreferenceDataStoreFactory.create(
            storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer) { path.toPath() },
            migrations = listOf(DataStoreLocalChatTemplateStore.legacyMigration(readLegacy, clearLegacy)),
            scope = scope,
        ),
    )
}
