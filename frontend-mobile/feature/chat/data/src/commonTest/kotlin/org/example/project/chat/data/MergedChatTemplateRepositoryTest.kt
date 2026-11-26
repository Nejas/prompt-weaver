package org.example.project.chat.data

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.example.project.catalog.domain.CatalogRepository
import org.example.project.catalog.domain.ComposedPrompt
import org.example.project.catalog.domain.TemplateDetails
import org.example.project.catalog.domain.TemplatePage
import org.example.project.catalog.domain.TemplateSummary
import org.example.project.chat.domain.ChatTemplate
import org.example.project.chat.domain.ChatTemplateDraft
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class MergedChatTemplateRepositoryTest {
    @Test
    fun categoriesMergeFirstHundredRemoteTemplatesWithLocalTemplatesAndEmptyCategories() = runBlocking {
        val catalog = FakeCatalogRepository(listOf(template("remote", "Plans")))
        val repository = MergedChatTemplateRepository(
            catalog = catalog,
            local = FakeLocalStore(
                templates = listOf(ChatTemplate("local", "My plan", "", "Plans", true)),
                categories = listOf("Personal"),
            ),
        )

        val categories = repository.categories()

        assertEquals(listOf("Plans", "Personal"), categories.map { it.name })
        assertEquals(listOf("remote", "local"), categories.first().templates.map { it.id })
        assertFalse(categories.first().isLocal)
        assertTrue(categories.last().isLocal)
        assertTrue(categories.last().templates.isEmpty())
        assertEquals(100, catalog.requestedLimit)
    }

    @Test
    fun creatingTemplatePersistsItAndItsCategory() = runBlocking {
        val local = FakeLocalStore()
        val repository = MergedChatTemplateRepository(FakeCatalogRepository(), local)

        repository.createTemplate(ChatTemplateDraft("  Idea  ", " Description ", " Notes "))

        assertEquals(listOf("Notes"), repository.categories().map { it.name })
        assertEquals("Idea", local.templates.single().title)
        assertEquals("Description", local.templates.single().description)
        assertEquals("Notes", local.templates.single().category)
        assertTrue(local.templates.single().isLocal)
    }

    @Test
    fun concurrentWritesPreserveAllCategoriesAndTemplates() = runBlocking {
        val local = FakeLocalStore(yieldOnRead = true)
        val repository = MergedChatTemplateRepository(FakeCatalogRepository(), local)

        coroutineScope {
            repeat(10) { index ->
                launch { repository.createCategory("Category $index") }
                launch { repository.createTemplate(ChatTemplateDraft("Template $index", "", "Notes")) }
            }
        }

        assertEquals(10, local.categories.distinct().size)
        assertEquals(10, local.templates.map { it.title }.distinct().size)
    }

    @Test
    fun savingTemplateDoesNotRequireASecondCategoryWrite() = runBlocking {
        val local = FakeLocalStore(failCategorySave = true)
        val repository = MergedChatTemplateRepository(FakeCatalogRepository(), local)

        repository.createTemplate(ChatTemplateDraft("Idea", "", "Notes"))

        assertEquals(listOf("Notes"), repository.categories().map { it.name })
        assertEquals(1, local.templates.size)
    }

    @Test
    fun failedTemplateWriteLeavesExistingDataIntact() = runBlocking {
        val existing = ChatTemplate("old", "Old", "", "Notes", true)
        val local = FakeLocalStore(templates = listOf(existing), failTemplateSave = true)
        val repository = MergedChatTemplateRepository(FakeCatalogRepository(), local)

        assertFailsWith<IllegalStateException> {
            repository.createTemplate(ChatTemplateDraft("Idea", "", "Notes"))
        }

        assertEquals(listOf(existing), local.templates)
    }

    private fun template(id: String, category: String) = TemplateSummary(id, id, "", category, id)
}

private class FakeCatalogRepository(
    private val templates: List<TemplateSummary> = emptyList(),
) : CatalogRepository {
    var requestedLimit: Int? = null

    override suspend fun listTemplates(limit: Int, offset: Int): TemplatePage {
        requestedLimit = limit
        return TemplatePage(templates, limit, offset)
    }

    override suspend fun getTemplate(versionId: String): TemplateDetails = error("Not used")
    override suspend fun composePrompt(versionId: String, parameters: Map<String, String>): ComposedPrompt = error("Not used")
}

private class FakeLocalStore(
    var templates: List<ChatTemplate> = emptyList(),
    var categories: List<String> = emptyList(),
    val yieldOnRead: Boolean = false,
    val failCategorySave: Boolean = false,
    val failTemplateSave: Boolean = false,
) : LocalChatTemplateStore {
    override suspend fun read(): List<ChatTemplate> {
        val snapshot = templates
        if (yieldOnRead) yield()
        return snapshot
    }
    override suspend fun save(templates: List<ChatTemplate>) {
        check(!failTemplateSave)
        this.templates = templates
    }
    override suspend fun readCategories(): List<String> {
        val snapshot = categories
        if (yieldOnRead) yield()
        return snapshot
    }
    override suspend fun saveCategories(categories: List<String>) {
        check(!failCategorySave)
        this.categories = categories
    }
}
