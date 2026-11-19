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
    init { lifecycle.doOnDestroy { scope.cancel() }; reload() }
    fun reload() {
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
        mutableState.value = current.copy(values = current.values + (name to value), errors = current.errors.filterNot { it.parameterName == name }, validated = false)
    }
    fun validate() {
        val current = mutableState.value as? DetailsState.Loaded ?: return
        mutableState.value = current.copy(errors = validateTemplateParameters(current.template.parameters, current.values), validated = true)
    }
}
