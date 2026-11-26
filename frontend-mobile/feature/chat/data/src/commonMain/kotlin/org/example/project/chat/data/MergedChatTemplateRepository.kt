package org.example.project.chat.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.project.catalog.domain.CatalogRepository
import org.example.project.chat.domain.ChatTemplate
import org.example.project.chat.domain.ChatTemplateCategory
import org.example.project.chat.domain.ChatTemplateDraft
import org.example.project.chat.domain.ChatTemplateRepository
import kotlin.random.Random

class MergedChatTemplateRepository(
    private val catalog: CatalogRepository,
    private val local: LocalChatTemplateStore,
) : ChatTemplateRepository {
    private val localMutex = Mutex()

    override suspend fun categories(): List<ChatTemplateCategory> {
        val remote = catalog.listTemplates(limit = 100).templates.take(100).map {
            ChatTemplate(it.versionId, it.title, it.description, it.category, false)
        }
        val (localTemplates, localCategories) = localMutex.withLock {
            local.read() to local.readCategories()
        }
        val grouped = (remote + localTemplates).groupBy { it.category }
        return (grouped.keys + localCategories).distinct().map { name ->
            val templates = grouped[name].orEmpty()
            ChatTemplateCategory(name, templates, templates.all { it.isLocal })
        }
    }

    override suspend fun createCategory(name: String) {
        val normalizedName = name.trim()
        require(normalizedName.isNotEmpty()) { "Category name must not be blank" }
        localMutex.withLock {
            val categories = local.readCategories()
            if (normalizedName !in categories) local.saveCategories(categories + normalizedName)
        }
    }

    override suspend fun createTemplate(draft: ChatTemplateDraft) {
        val normalized = draft.copy(
            title = draft.title.trim(),
            description = draft.description.trim(),
            category = draft.category.trim(),
        )
        require(normalized.title.isNotEmpty()) { "Template title must not be blank" }
        require(normalized.category.isNotEmpty()) { "Template category must not be blank" }
        localMutex.withLock {
            val templates = local.read()
            local.save(
                templates + ChatTemplate(
                    id = "local-${Random.nextLong()}",
                    title = normalized.title,
                    description = normalized.description,
                    category = normalized.category,
                    isLocal = true,
                ),
            )
        }
    }
}
