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
    fun applyingPromptToEmptyDraftDoesNotSendMessage() {
        val component = ChatComponent(DefaultComponentContext(LifecycleRegistry()))
        val messages = component.state.value.messages

        component.applyPrompt("A composed prompt")

        assertEquals("A composed prompt", component.state.value.draft)
        assertEquals(messages, component.state.value.messages)
        assertEquals(null, component.state.value.pendingPrompt)
    }

    @Test
    fun replacingExistingDraftRequiresConfirmationAndCanBeCancelled() {
        val component = ChatComponent(DefaultComponentContext(LifecycleRegistry()))
        component.onIntent(ChatIntent.ChangeDraft("Original"))
        val messages = component.state.value.messages

        component.applyPrompt("New prompt")
        assertEquals("Original", component.state.value.draft)
        assertEquals("New prompt", component.state.value.pendingPrompt)
        component.onIntent(ChatIntent.CancelDraftReplacement)
        assertEquals("Original", component.state.value.draft)
        assertEquals(null, component.state.value.pendingPrompt)

        component.applyPrompt("New prompt")
        component.onIntent(ChatIntent.ConfirmDraftReplacement)
        assertEquals("New prompt", component.state.value.draft)
        assertEquals(null, component.state.value.pendingPrompt)
        assertEquals(messages, component.state.value.messages)
    }

    @Test
    fun onlySelectedServerTemplatesCanOpenParameterForm() {
        val opened = mutableListOf<String>()
        val component = ChatComponent(
            DefaultComponentContext(LifecycleRegistry()),
            onUseTemplate = { opened += it },
            templates = object : ChatTemplateRepository {
                override suspend fun categories() = listOf(ChatTemplateCategory("Ideas", listOf(
                    ChatTemplate("remote", "Remote", "", "Ideas", false),
                    ChatTemplate("local", "Local", "", "Ideas", true),
                ), false))
                override suspend fun createCategory(name: String) = Unit
                override suspend fun createTemplate(draft: ChatTemplateDraft) = Unit
            },
            dispatcher = Dispatchers.Unconfined,
        )

        component.onIntent(ChatIntent.UseSelectedTemplate)
        component.onIntent(ChatIntent.SelectTemplate("local"))
        component.onIntent(ChatIntent.UseSelectedTemplate)
        assertTrue(opened.isEmpty())
        component.onIntent(ChatIntent.SelectTemplate("remote"))
        component.onIntent(ChatIntent.UseSelectedTemplate)
        assertEquals(listOf("remote"), opened)
    }

    @Test
    fun blankPromptDoesNotChangeDraft() {
        val component = ChatComponent(DefaultComponentContext(LifecycleRegistry()))
        component.onIntent(ChatIntent.ChangeDraft("Original"))

        component.applyPrompt("  ")

        assertEquals("Original", component.state.value.draft)
        assertEquals(null, component.state.value.pendingPrompt)
    }

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
        component.onIntent(ChatIntent.SelectCategory("Other"))
        assertEquals(null, component.state.value.selectedTemplate)
    }

    @Test
    fun blankCategoryShowsValidationError() {
        val component = ChatComponent(
            DefaultComponentContext(LifecycleRegistry()),
            templates = FakeTemplateRepository,
            dispatcher = Dispatchers.Unconfined,
        )

        component.onIntent(ChatIntent.CreateCategory)

        assertEquals("Введите название категории.", component.state.value.templateFormError)
        assertEquals(null, component.state.value.templatesError)
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
    fun categorySaveBlocksEditsAndRepeatedSubmit() {
        val gate = CompletableDeferred<Unit>()
        val repository = RecordingTemplateRepository(saveGate = gate)
        val component = ChatComponent(DefaultComponentContext(LifecycleRegistry()), templates = repository, dispatcher = Dispatchers.Unconfined)

        component.onIntent(ChatIntent.ChangeCategoryDraft("First"))
        component.onIntent(ChatIntent.CreateCategory)
        component.onIntent(ChatIntent.ChangeCategoryDraft("Next"))
        component.onIntent(ChatIntent.CreateCategory)
        assertEquals("First", component.state.value.categoryDraft)
        gate.complete(Unit)

        assertEquals(listOf("First"), repository.createdCategories)
        assertEquals("", component.state.value.categoryDraft)
        assertEquals("First", component.state.value.templateCategoryDraft)
        assertEquals(false, component.state.value.creatingCategory)
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
        component.onIntent(ChatIntent.SetCreatingCategory(true))
        component.onIntent(ChatIntent.HideTemplateForm)
        component.onIntent(ChatIntent.CreateTemplate)
        assertEquals("Idea", component.state.value.templateTitleDraft)
        assertEquals(false, component.state.value.creatingCategory)
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
        assertEquals("Не удалось сохранить шаблон.", component.state.value.templateFormError)
        assertEquals(null, component.state.value.templatesError)
    }

    @Test
    fun openingSheetUsesSelectedCategoryAndPreservesDraftWhenReopened() {
        val component = ChatComponent(DefaultComponentContext(LifecycleRegistry()), templates = FakeTemplateRepository, dispatcher = Dispatchers.Unconfined)
        component.onIntent(ChatIntent.SelectCategory("Ideas"))
        component.onIntent(ChatIntent.ShowTemplateForm)
        assertEquals("Ideas", component.state.value.templateCategoryDraft)
        assertEquals(false, component.state.value.creatingCategory)

        component.onIntent(ChatIntent.ChangeTemplateTitle("Draft"))
        component.onIntent(ChatIntent.ChangeTemplateDescription("Description"))
        component.onIntent(ChatIntent.SetCreatingCategory(true))
        component.onIntent(ChatIntent.ChangeCategoryDraft("Personal"))
        component.onIntent(ChatIntent.HideTemplateForm)
        assertEquals(false, component.state.value.templateFormVisible)
        component.onIntent(ChatIntent.ShowTemplateForm)

        assertEquals("Draft", component.state.value.templateTitleDraft)
        assertEquals("Description", component.state.value.templateDescriptionDraft)
        assertEquals("Personal", component.state.value.categoryDraft)
        assertEquals(true, component.state.value.creatingCategory)
    }

    @Test
    fun emptyCatalogStartsWithNewCategoryAndSavesTemplateInOneOperation() {
        val repository = RecordingTemplateRepository(includeServerTemplates = false)
        val component = ChatComponent(DefaultComponentContext(LifecycleRegistry()), templates = repository, dispatcher = Dispatchers.Unconfined)
        component.onIntent(ChatIntent.ShowTemplateForm)
        assertEquals(true, component.state.value.creatingCategory)
        component.onIntent(ChatIntent.ChangeTemplateTitle("First"))
        component.onIntent(ChatIntent.ChangeTemplateDescription("Description"))
        component.onIntent(ChatIntent.ChangeCategoryDraft(" Personal "))

        component.onIntent(ChatIntent.CreateTemplate)

        assertEquals(listOf(ChatTemplateDraft("First", "Description", "Personal")), repository.createdTemplates)
        assertTrue(repository.createdCategories.isEmpty())
        assertEquals("Personal", component.state.value.selectedCategory)
        assertEquals("Personal", component.state.value.templateCategories.single().name)
        assertEquals(false, component.state.value.templateFormVisible)
        assertEquals("", component.state.value.categoryDraft)
    }

    @Test
    fun creatingOnlyCategoryKeepsSheetAndTemplateDraftThenSavesToThatCategory() {
        val repository = RecordingTemplateRepository()
        val component = ChatComponent(DefaultComponentContext(LifecycleRegistry()), templates = repository, dispatcher = Dispatchers.Unconfined)
        component.onIntent(ChatIntent.ShowTemplateForm)
        component.onIntent(ChatIntent.ChangeTemplateTitle("First"))
        component.onIntent(ChatIntent.ChangeTemplateDescription("Description"))
        component.onIntent(ChatIntent.SetCreatingCategory(true))
        component.onIntent(ChatIntent.ChangeCategoryDraft("Personal"))

        component.onIntent(ChatIntent.CreateCategory)

        assertEquals(true, component.state.value.templateFormVisible)
        assertEquals("First", component.state.value.templateTitleDraft)
        assertEquals("Description", component.state.value.templateDescriptionDraft)
        assertEquals("Personal", component.state.value.templateCategoryDraft)
        assertEquals(false, component.state.value.creatingCategory)
        component.onIntent(ChatIntent.CreateTemplate)
        assertEquals(listOf(ChatTemplateDraft("First", "Description", "Personal")), repository.createdTemplates)
    }

    @Test
    fun formErrorsRemainSeparateFromCatalogLoadErrors() {
        val repository = RecordingTemplateRepository(failNextLoad = true, failTemplateSave = true)
        val component = ChatComponent(DefaultComponentContext(LifecycleRegistry()), templates = repository, dispatcher = Dispatchers.Unconfined)
        val loadError = component.state.value.templatesError
        assertTrue(loadError != null)
        component.onIntent(ChatIntent.ShowTemplateForm)
        component.onIntent(ChatIntent.ChangeTemplateTitle("First"))
        component.onIntent(ChatIntent.ChangeCategoryDraft("Personal"))
        component.onIntent(ChatIntent.CreateTemplate)
        assertEquals(loadError, component.state.value.templatesError)
        assertEquals("Не удалось сохранить шаблон.", component.state.value.templateFormError)

        component.onIntent(ChatIntent.RetryTemplates)
        assertEquals(null, component.state.value.templatesError)
        assertEquals("Не удалось сохранить шаблон.", component.state.value.templateFormError)
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
    private val includeServerTemplates: Boolean = true,
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
            } + if (includeServerTemplates) FakeTemplateRepository.categories() else emptyList()
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
