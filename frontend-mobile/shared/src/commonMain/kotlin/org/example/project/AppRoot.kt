package org.example.project

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.pop
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.Serializable
import org.example.project.auth.domain.AuthRepository
import org.example.project.auth.presentation.RegistrationComponent
import org.example.project.catalog.domain.CatalogRepository
import org.example.project.catalog.presentation.CatalogRoot

class AppRoot(
    context: ComponentContext,
    catalogRepository: CatalogRepository,
    authRepository: AuthRepository,
    dispatcher: CoroutineDispatcher,
) : ComponentContext by context {
    private val navigation = StackNavigation<Config>()
    val stack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Catalog,
        handleBackButton = true,
    ) { config, child ->
        when (config) {
            Config.Catalog -> Child.Catalog(CatalogRoot(child, catalogRepository, dispatcher) { navigation.pushNew(Config.Registration) })
            Config.Registration -> Child.Registration(
                RegistrationComponent(child, authRepository, dispatcher, onRegistered = { navigation.pop() }, onBack = { navigation.pop() }),
            )
        }
    }

    @Serializable
    sealed interface Config {
        @Serializable data object Catalog : Config
        @Serializable data object Registration : Config
    }

    sealed interface Child {
        data class Catalog(val component: CatalogRoot) : Child
        data class Registration(val component: RegistrationComponent) : Child
    }
}
