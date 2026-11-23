package org.example.project.catalog.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.example.project.catalog.domain.*

class DetailsComponent(
    context: ComponentContext,
    private val versionId: String,
    private val repository: CatalogRepository,
    dispatcher: CoroutineDispatcher,
    val back: () -> Unit,
) : ComponentContext by context {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutableState = MutableStateFlow<DetailsState>(DetailsState.Loading)
    val state: StateFlow<DetailsState> = mutableState.asStateFlow()
    private var job: Job? = null
    private var composeJob: Job? = null
    init { lifecycle.doOnDestroy { scope.cancel() }; reload() }
    fun reload() {
        composeJob?.cancel()
        job?.cancel()
        mutableState.value = DetailsState.Loading
        job = scope.launch {
            try { mutableState.value = DetailsState.Loaded(repository.getTemplate(versionId)) }
            catch (e: CancellationException) { throw e }
            catch (e: CatalogException) { mutableState.value = DetailsState.Error }
        }
    }
    fun setValue(name: String, value: String) {
        val current = mutableState.value as? DetailsState.Loaded ?: return
        composeJob?.cancel()
        mutableState.value = current.copy(values = current.values + (name to value), errors = current.errors.filterNot { it.parameterName == name }, validated = false, composing = false, result = null, composeError = false)
    }
    fun validate() {
        val current = mutableState.value as? DetailsState.Loaded ?: return
        mutableState.value = current.copy(errors = validateTemplateParameters(current.template.parameters, current.values), validated = true)
    }
    fun compose() {
        val current = mutableState.value as? DetailsState.Loaded ?: return
        if (current.composing) return
        val errors = validateTemplateParameters(current.template.parameters, current.values)
        mutableState.value = current.copy(errors = errors, validated = true, result = null, composeError = false)
        if (errors.isNotEmpty()) return
        mutableState.value = (mutableState.value as DetailsState.Loaded).copy(composing = true)
        composeJob = scope.launch {
            try {
                val result = repository.composePrompt(versionId, current.values)
                val loaded = mutableState.value as? DetailsState.Loaded ?: return@launch
                mutableState.value = loaded.copy(composing = false, result = result.text)
            } catch (e: CancellationException) { throw e }
            catch (e: CatalogException) {
                val loaded = mutableState.value as? DetailsState.Loaded ?: return@launch
                mutableState.value = loaded.copy(composing = false, composeError = true)
            }
        }
    }
}
