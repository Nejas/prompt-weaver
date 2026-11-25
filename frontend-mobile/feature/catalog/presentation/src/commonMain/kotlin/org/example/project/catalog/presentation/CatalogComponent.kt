package org.example.project.catalog.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.catalog.domain.CatalogRepository
import org.example.project.catalog.domain.CatalogException

class CatalogComponent(
    componentContext: ComponentContext,
    private val repository: CatalogRepository,
    private val onSelected: (String) -> Unit,
    mainDispatcher: CoroutineDispatcher,
    private val onRegister: () -> Unit = {},
    private val onLogin: () -> Unit = {},
    private val onAccount: (() -> Unit)? = null,
) : ComponentContext by componentContext {
    private val scope = CoroutineScope(SupervisorJob() + mainDispatcher)
    private val mutableState = MutableStateFlow<CatalogState>(CatalogState.Loading)
    val state: StateFlow<CatalogState> = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        reload()
    }

    fun onIntent(intent: CatalogIntent) {
        when (intent) {
            CatalogIntent.Reload -> reload()
            is CatalogIntent.SelectTemplate -> select(intent.versionId)
            CatalogIntent.Register -> register()
            CatalogIntent.Login -> login()
            CatalogIntent.Account -> account()
        }
    }

    fun reload() {
        loadJob?.cancel()
        mutableState.value = CatalogState.Loading
        loadJob = scope.launch {
            try {
                val templates = repository.listTemplates().templates
                mutableState.value = if (templates.isEmpty()) CatalogState.Empty else CatalogState.Loaded(templates)
            } catch (e: CancellationException) {
                throw e
            } catch (e: CatalogException) {
                mutableState.value = CatalogState.Error
            }
        }
    }

    fun select(versionId: String) { onSelected(versionId) }
    fun register() = onRegister()
    fun login() = onLogin()
    val hasAccount get() = onAccount != null
    fun account() = onAccount?.invoke()
}
