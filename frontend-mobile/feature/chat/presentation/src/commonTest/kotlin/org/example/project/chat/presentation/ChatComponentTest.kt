package org.example.project.chat.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.example.project.chat.domain.ChatTemplate
import org.example.project.chat.domain.ChatTemplateCategory
import org.example.project.chat.domain.ChatTemplateDraft
import org.example.project.chat.domain.ChatTemplateRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatComponentTest {
    @Test
    fun sendAddsLocalUserMessageAndClearsDraft() {
        val component = ChatComponent(DefaultComponentContext(LifecycleRegistry()))

        component.onIntent(ChatIntent.ChangeDraft("Привет"))
        component.onIntent(ChatIntent.Send)

        assertEquals("", component.state.value.draft)
        assertEquals(ChatMessage.User("Привет"), component.state.value.messages.last())
    }

    @Test
    fun selectingCategoryAndTemplateUpdatesState() = runBlocking {
        val component = ChatComponent(
            DefaultComponentContext(LifecycleRegistry()),
            templates = FakeTemplateRepository,
            dispatcher = Dispatchers.Unconfined,
        )

        component.onIntent(ChatIntent.SelectCategory("Ideas"))
        component.onIntent(ChatIntent.SelectTemplate("idea"))

        assertEquals("Ideas", component.state.value.selectedCategory)
        assertEquals("idea", component.state.value.selectedTemplate?.id)
    }

    @Test
    fun blankCategoryShowsValidationError() {
        val component = ChatComponent(
            DefaultComponentContext(LifecycleRegistry()),
            templates = FakeTemplateRepository,
            dispatcher = Dispatchers.Unconfined,
        )

        component.onIntent(ChatIntent.CreateCategory)

        assertEquals("Введите название категории.", component.state.value.templatesError)
    }

    @Test
    fun creatingCategorySelectsItAfterReload() = runBlocking {
        val repository = RecordingTemplateRepository()
        val component = ChatComponent(
            DefaultComponentContext(LifecycleRegistry()),
            templates = repository,
            dispatcher = Dispatchers.Unconfined,
        )

        component.onIntent(ChatIntent.ChangeCategoryDraft("Personal"))
        component.onIntent(ChatIntent.CreateCategory)

        assertEquals(listOf("Personal"), repository.createdCategories)
        assertEquals("Personal", component.state.value.selectedCategory)
    }

    @Test
    fun retryClearsLoadErrorWhenRepositoryRecovers() = runBlocking {
        val repository = RecordingTemplateRepository(failNextLoad = true)
        val component = ChatComponent(
            DefaultComponentContext(LifecycleRegistry()),
            templates = repository,
            dispatcher = Dispatchers.Unconfined,
        )

        assertTrue(component.state.value.templatesError != null)
        component.onIntent(ChatIntent.RetryTemplates)

        assertEquals(null, component.state.value.templatesError)
        assertEquals("Ideas", component.state.value.templateCategories.single().name)
    }

    @Test
    fun categorySaveKeepsNewInputAndIgnoresRepeatedSubmit() {
        val gate = CompletableDeferred<Unit>()
        val repository = RecordingTemplateRepository(saveGate = gate)
        val component = ChatComponent(DefaultComponentContext(LifecycleRegistry()), templates = repository, dispatcher = Dispatchers.Unconfined)

        component.onIntent(ChatIntent.ChangeCategoryDraft("First"))
        component.onIntent(ChatIntent.CreateCategory)
        component.onIntent(ChatIntent.ChangeCategoryDraft("Next"))
        component.onIntent(ChatIntent.CreateCategory)
        gate.complete(Unit)

        assertEquals(listOf("First"), repository.createdCategories)
        assertEquals("Next", component.state.value.categoryDraft)
        assertEquals(false, component.state.value.categorySaving)
    }

    @Test
    fun templateSaveBlocksDuplicateSubmitAndFormEdits() {
        val gate = CompletableDeferred<Unit>()
        val repository = RecordingTemplateRepository(saveGate = gate)
        val component = ChatComponent(DefaultComponentContext(LifecycleRegistry()), templates = repository, dispatcher = Dispatchers.Unconfined)
        component.onIntent(ChatIntent.SelectCategory("Ideas"))
        component.onIntent(ChatIntent.SelectTemplate("idea"))
        assertEquals("idea", component.state.value.selectedTemplate?.id)
        component.onIntent(ChatIntent.ShowTemplateForm)
        component.onIntent(ChatIntent.ChangeTemplateTitle("Idea"))
        component.onIntent(ChatIntent.ChangeTemplateCategory("Notes"))
        component.onIntent(ChatIntent.CreateTemplate)

        component.onIntent(ChatIntent.ChangeTemplateTitle("Changed"))
        component.onIntent(ChatIntent.HideTemplateForm)
        component.onIntent(ChatIntent.CreateTemplate)
        assertEquals("Idea", component.state.value.templateTitleDraft)
        assertTrue(component.state.value.templateFormVisible)
        gate.complete(Unit)

        assertEquals(listOf(ChatTemplateDraft("Idea", "", "Notes")), repository.createdTemplates)
        assertEquals(false, component.state.value.templateFormVisible)
        assertEquals(false, component.state.value.templateSaving)
        assertEquals("Notes", component.state.value.selectedCategory)
        assertEquals(null, component.state.value.selectedTemplate)
    }

    @Test
    fun templateSaveFailurePreservesDraftForRetry() {
        val repository = RecordingTemplateRepository(failTemplateSave = true)
        val component = ChatComponent(DefaultComponentContext(LifecycleRegistry()), templates = repository, dispatcher = Dispatchers.Unconfined)
        component.onIntent(ChatIntent.ShowTemplateForm)
        component.onIntent(ChatIntent.ChangeTemplateTitle("Idea"))
        component.onIntent(ChatIntent.ChangeTemplateCategory("Notes"))

        component.onIntent(ChatIntent.CreateTemplate)

        assertEquals("Idea", component.state.value.templateTitleDraft)
        assertTrue(component.state.value.templateFormVisible)
        assertEquals(false, component.state.value.templateSaving)
        assertEquals("Не удалось сохранить шаблон.", component.state.value.templatesError)
    }
}

private object FakeTemplateRepository : ChatTemplateRepository {
    override suspend fun categories() = listOf(
        ChatTemplateCategory("Ideas", listOf(ChatTemplate("idea", "Idea", "", "Ideas", false)), false),
    )

    override suspend fun createCategory(name: String) = Unit
    override suspend fun createTemplate(draft: ChatTemplateDraft) = Unit
}

private class RecordingTemplateRepository(
    private var failNextLoad: Boolean = false,
    private val saveGate: CompletableDeferred<Unit>? = null,
    private val failTemplateSave: Boolean = false,
) : ChatTemplateRepository {
    val createdCategories = mutableListOf<String>()
    val createdTemplates = mutableListOf<ChatTemplateDraft>()

    override suspend fun categories(): List<ChatTemplateCategory> {
        if (failNextLoad) {
            failNextLoad = false
            error("Temporary failure")
        }
        return createdCategories.map { ChatTemplateCategory(it, emptyList(), true) } +
            createdTemplates.groupBy { it.category }.map { (category, drafts) ->
                ChatTemplateCategory(category, drafts.mapIndexed { index, draft ->
                    ChatTemplate("local-$index", draft.title, draft.description, category, true)
                }, true)
            } + FakeTemplateRepository.categories()
    }

    override suspend fun createCategory(name: String) {
        saveGate?.await()
        createdCategories += name
    }
    override suspend fun createTemplate(draft: ChatTemplateDraft) {
        saveGate?.await()
        check(!failTemplateSave)
        createdTemplates += draft
    }
}
