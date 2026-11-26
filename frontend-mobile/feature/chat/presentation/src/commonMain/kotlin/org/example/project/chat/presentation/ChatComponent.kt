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
        if ((state.value.templateSaving || state.value.categorySaving) && (
            intent is ChatIntent.ChangeTemplateTitle || intent is ChatIntent.ChangeTemplateDescription ||
                intent is ChatIntent.ChangeTemplateCategory || intent is ChatIntent.SelectCategory ||
                intent is ChatIntent.ChangeCategoryDraft || intent is ChatIntent.SetCreatingCategory ||
                intent == ChatIntent.ShowTemplateForm || intent == ChatIntent.HideTemplateForm ||
                intent == ChatIntent.CreateTemplate || intent == ChatIntent.CreateCategory
            )) return
        when (intent) {
            is ChatIntent.ChangeDraft -> mutableState.value = state.value.copy(draft = intent.value)
            ChatIntent.Send -> send()
            ChatIntent.OpenProfile -> onProfile()
            ChatIntent.OpenSettings -> onSettings()
            ChatIntent.RetryTemplates -> loadTemplates()
            is ChatIntent.SelectCategory -> mutableState.value = state.value.copy(
                selectedCategory = intent.name,
                selectedTemplate = null,
            )
            is ChatIntent.SelectTemplate -> mutableState.value = state.value.copy(
                selectedTemplate = state.value.templateCategories
                    .flatMap { it.templates }
                    .firstOrNull { it.id == intent.id },
            )
            is ChatIntent.ChangeCategoryDraft -> mutableState.value = state.value.copy(categoryDraft = intent.value, templateFormError = null)
            ChatIntent.CreateCategory -> createCategory()
            ChatIntent.ShowTemplateForm -> showTemplateForm()
            ChatIntent.HideTemplateForm -> mutableState.value = state.value.copy(templateFormVisible = false)
            is ChatIntent.ChangeTemplateTitle -> mutableState.value = state.value.copy(templateTitleDraft = intent.value, templateFormError = null)
            is ChatIntent.ChangeTemplateDescription -> mutableState.value = state.value.copy(templateDescriptionDraft = intent.value, templateFormError = null)
            is ChatIntent.ChangeTemplateCategory -> mutableState.value = state.value.copy(
                templateCategoryDraft = intent.value, creatingCategory = false, templateFormError = null,
            )
            is ChatIntent.SetCreatingCategory -> mutableState.value = state.value.copy(creatingCategory = intent.value, templateFormError = null)
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

    private fun showTemplateForm() {
        val current = state.value
        val category = current.templateCategoryDraft.ifBlank {
            current.selectedCategory ?: current.templateCategories.firstOrNull()?.name.orEmpty()
        }
        mutableState.value = current.copy(
            templateFormVisible = true,
            templateCategoryDraft = category,
            creatingCategory = current.creatingCategory || current.templateCategories.isEmpty(),
            templateFormError = null,
        )
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
        val name = state.value.categoryDraft.trim()
        if (name.isEmpty()) {
            mutableState.value = state.value.copy(templateFormError = "Введите название категории.")
            return
        }
        mutableState.value = state.value.copy(categorySaving = true, templateFormError = null)
        scope.launch {
            try {
                repository.createCategory(name)
                mutableState.value = state.value.copy(
                    categoryDraft = "",
                    templateCategoryDraft = name,
                    creatingCategory = false,
                    selectedCategory = name,
                    selectedTemplate = null,
                    categorySaving = false,
                    templateFormError = null,
                )
                loadTemplates()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.value = state.value.copy(categorySaving = false, templateFormError = "Не удалось сохранить категорию.")
            }
        }
    }

    private fun createTemplate() {
        val repository = templates ?: return
        val current = state.value
        val category = (if (current.creatingCategory) current.categoryDraft else current.templateCategoryDraft).trim()
        if (current.templateTitleDraft.isBlank() || category.isBlank()) {
            mutableState.value = current.copy(templateFormError = "Укажите название шаблона и категорию.")
            return
        }
        mutableState.value = current.copy(templateSaving = true, templateFormError = null)
        scope.launch {
            try {
                repository.createTemplate(ChatTemplateDraft(
                    title = current.templateTitleDraft,
                    description = current.templateDescriptionDraft,
                    category = category,
                ))
                mutableState.value = state.value.copy(
                    templateTitleDraft = "",
                    templateDescriptionDraft = "",
                    templateCategoryDraft = "",
                    categoryDraft = "",
                    creatingCategory = false,
                    templateFormError = null,
                    templateFormVisible = false,
                    templateSaving = false,
                    selectedCategory = category,
                    selectedTemplate = null,
                )
                loadTemplates()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.value = state.value.copy(templateSaving = false, templateFormError = "Не удалось сохранить шаблон.")
            }
        }
    }
}
