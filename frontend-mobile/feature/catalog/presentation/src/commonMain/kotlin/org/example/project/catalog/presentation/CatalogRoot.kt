package org.example.project.catalog.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.Serializable
import org.example.project.catalog.domain.CatalogRepository

class CatalogRoot(context: ComponentContext, repository: CatalogRepository, dispatcher: CoroutineDispatcher) : ComponentContext by context {
    private val navigation = StackNavigation<Config>()
    val stack = childStack(source = navigation, serializer = Config.serializer(), initialConfiguration = Config.List, handleBackButton = true) { config, child ->
        when (config) {
            Config.List -> Child.List(CatalogComponent(child, repository, { navigation.pushNew(Config.Details(it)) }, dispatcher))
            is Config.Details -> Child.Details(DetailsComponent(child, config.id, repository, dispatcher) { navigation.pop() })
        }
    }
    @Serializable sealed interface Config {
        @Serializable data object List : Config
        @Serializable data class Details(val id: String) : Config
    }
    sealed interface Child {
        data class List(val component: CatalogComponent) : Child
        data class Details(val component: DetailsComponent) : Child
    }
}
