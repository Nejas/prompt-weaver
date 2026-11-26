package org.example.project.chat.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.chat.domain.ChatTemplateRepository
import org.example.project.chat.domain.ChatTemplateDraft

class ChatComponent(
    context: ComponentContext,
    private val onProfile: () -> Unit = {},
    private val onCatalog: () -> Unit = {},
    private val onSettings: () -> Unit = {},
    private val templates: ChatTemplateRepository? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val onUseTemplate: (String) -> Unit = {},
) : ComponentContext by context {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutableState = MutableStateFlow(ChatState())
    val state = mutableState.asStateFlow()
    private var templatesJob: Job? = null

    init { lifecycle.doOnDestroy { scope.cancel() }; loadTemplates() }

    fun onIntent(intent: ChatIntent) {
        if (state.value.templateSaving && (
            intent is ChatIntent.ChangeTemplateTitle || intent is ChatIntent.ChangeTemplateDescription ||
                intent is ChatIntent.ChangeTemplateCategory || intent is ChatIntent.SelectCategory ||
                intent == ChatIntent.ShowTemplateForm || intent == ChatIntent.HideTemplateForm ||
                intent == ChatIntent.CreateTemplate
            )) return
        when (intent) {
            is ChatIntent.ChangeDraft -> mutableState.value = state.value.copy(draft = intent.value)
            ChatIntent.Send -> send()
            ChatIntent.OpenProfile -> onProfile()
            ChatIntent.OpenCatalog -> onCatalog()
            ChatIntent.OpenSettings -> onSettings()
            ChatIntent.RetryTemplates -> loadTemplates()
            is ChatIntent.SelectCategory -> mutableState.value = state.value.copy(
                selectedCategory = intent.name,
                selectedTemplate = null,
                templateCategoryDraft = intent.name,
            )
            is ChatIntent.SelectTemplate -> mutableState.value = state.value.copy(
                selectedTemplate = state.value.templateCategories
                    .flatMap { it.templates }
                    .firstOrNull { it.id == intent.id },
            )
            is ChatIntent.ChangeCategoryDraft -> mutableState.value = state.value.copy(categoryDraft = intent.value)
            ChatIntent.CreateCategory -> createCategory()
            ChatIntent.ShowTemplateForm -> mutableState.value = state.value.copy(
                templateFormVisible = true,
                templateCategoryDraft = state.value.selectedCategory.orEmpty(),
                templatesError = null,
            )
            ChatIntent.HideTemplateForm -> mutableState.value = state.value.copy(templateFormVisible = false)
            is ChatIntent.ChangeTemplateTitle -> mutableState.value = state.value.copy(templateTitleDraft = intent.value)
            is ChatIntent.ChangeTemplateDescription -> mutableState.value = state.value.copy(templateDescriptionDraft = intent.value)
            is ChatIntent.ChangeTemplateCategory -> mutableState.value = state.value.copy(templateCategoryDraft = intent.value)
            ChatIntent.CreateTemplate -> createTemplate()
            ChatIntent.UseSelectedTemplate -> state.value.selectedTemplate
                ?.takeUnless { it.isLocal }
                ?.let { onUseTemplate(it.id) }
            ChatIntent.ConfirmDraftReplacement -> {
                val prompt = state.value.pendingPrompt ?: return
                mutableState.value = state.value.copy(draft = prompt, pendingPrompt = null)
            }
            ChatIntent.CancelDraftReplacement -> mutableState.value = state.value.copy(pendingPrompt = null)
        }
    }

    fun applyPrompt(text: String) {
        if (text.isBlank()) return
        mutableState.value = if (state.value.draft.isNotEmpty()) {
            state.value.copy(pendingPrompt = text)
        } else {
            state.value.copy(draft = text, pendingPrompt = null)
        }
    }

    private fun send() {
        val draft = state.value.draft.trim()
        if (draft.isEmpty()) return
        mutableState.value = state.value.copy(
            draft = "",
            messages = state.value.messages + ChatMessage.User(draft),
        )
    }

    private fun loadTemplates() {
        val repository = templates ?: return
        templatesJob?.cancel()
        mutableState.value = state.value.copy(templatesLoading = true, templatesError = null)
        templatesJob = scope.launch {
            try {
                val categories = repository.categories()
                val selectedCategory = state.value.selectedCategory?.takeIf { selected -> categories.any { it.name == selected } }
                mutableState.value = state.value.copy(
                    templateCategories = categories,
                    templatesLoading = false,
                    selectedCategory = selectedCategory,
                    selectedTemplate = state.value.selectedTemplate?.takeIf { selected ->
                        categories.any { category -> category.templates.any { it.id == selected.id } }
                    },
                )
            }
            catch (error: CancellationException) { throw error }
            catch (_: Exception) { mutableState.value = state.value.copy(templatesLoading = false, templatesError = "Не удалось загрузить шаблоны. Повторите попытку.") }
        }
    }

    private fun createCategory() {
        val repository = templates ?: return
        if (state.value.categorySaving) return
        val draft = state.value.categoryDraft
        val name = state.value.categoryDraft.trim()
        if (name.isEmpty()) {
            mutableState.value = state.value.copy(templatesError = "Введите название категории.")
            return
        }
        mutableState.value = state.value.copy(categorySaving = true, templatesError = null)
        scope.launch {
            try {
                repository.createCategory(name)
                mutableState.value = state.value.copy(
                    categoryDraft = if (state.value.categoryDraft == draft) "" else state.value.categoryDraft,
                    selectedCategory = name,
                    selectedTemplate = null,
                    categorySaving = false,
                    templatesError = null,
                )
                loadTemplates()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.value = state.value.copy(categorySaving = false, templatesError = "Не удалось сохранить категорию.")
            }
        }
    }

    private fun createTemplate() {
        val repository = templates ?: return
        val current = state.value
        if (current.templateTitleDraft.isBlank() || current.templateCategoryDraft.isBlank()) {
            mutableState.value = current.copy(templatesError = "Укажите название шаблона и категорию.")
            return
        }
        mutableState.value = current.copy(templateSaving = true, templatesError = null)
        scope.launch {
            try {
                repository.createTemplate(ChatTemplateDraft(
                    title = current.templateTitleDraft,
                    description = current.templateDescriptionDraft,
                    category = current.templateCategoryDraft,
                ))
                mutableState.value = state.value.copy(
                    templateTitleDraft = "",
                    templateDescriptionDraft = "",
                    templateCategoryDraft = "",
                    templateFormVisible = false,
                    templateSaving = false,
                    selectedCategory = current.templateCategoryDraft.trim(),
                    selectedTemplate = null,
                )
                loadTemplates()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.value = state.value.copy(templateSaving = false, templatesError = "Не удалось сохранить шаблон.")
            }
        }
    }
}
