package org.example.project

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.Serializable
import org.example.project.auth.domain.AuthRepository
import org.example.project.auth.domain.SessionRepository
import org.example.project.auth.domain.AuthException
import org.example.project.auth.presentation.LoginComponent
import org.example.project.auth.presentation.SessionComponent
import org.example.project.auth.presentation.RegistrationComponent
import org.example.project.catalog.domain.CatalogRepository
import org.example.project.catalog.presentation.CatalogRoot

class AppRoot(
    context: ComponentContext,
    catalogRepository: CatalogRepository,
    authRepository: AuthRepository,
    sessionRepository: SessionRepository,
    dispatcher: CoroutineDispatcher,
) : ComponentContext by context {
    private val navigation = StackNavigation<Config>()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    val stack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Catalog,
        handleBackButton = true,
    ) { config, child ->
        when (config) {
            Config.Catalog -> Child.Catalog(CatalogRoot(child, catalogRepository, dispatcher,
                onRegister = { navigation.pushNew(Config.Registration) }, onLogin = { navigation.pushNew(Config.Login) }))
            Config.AuthCatalog -> Child.Catalog(CatalogRoot(child, catalogRepository, dispatcher,
                onAccount = { navigation.pop() }))
            Config.Registration -> Child.Registration(
                RegistrationComponent(child, authRepository, dispatcher, onRegistered = { navigation.replaceAll(Config.Session) },
                    onBack = { navigation.pop() }, onLogin = { navigation.pushNew(Config.Login) }),
            )
            Config.Login -> Child.Login(LoginComponent(child, sessionRepository, dispatcher,
                onLoggedIn = { navigation.replaceAll(Config.Session) }, back = { navigation.pop() }))
            Config.Session -> Child.Session(SessionComponent(child, sessionRepository, dispatcher,
                onLoggedOut = { navigation.replaceAll(Config.Catalog) }, browseCatalog = { navigation.pushNew(Config.AuthCatalog) }))
        }
    }

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        scope.launch {
            try {
                if (sessionRepository.refresh() != null && stack.value.active.configuration == Config.Catalog)
                    navigation.replaceAll(Config.Session)
            } catch (_: AuthException) { /* Guest can still browse the catalog. */ }
            catch (_: Exception) { /* Storage can be temporarily unavailable. */ }
        }
    }

    @Serializable
    sealed interface Config {
        @Serializable data object Catalog : Config
        @Serializable data object AuthCatalog : Config
        @Serializable data object Registration : Config
        @Serializable data object Login : Config
        @Serializable data object Session : Config
    }

    sealed interface Child {
        data class Catalog(val component: CatalogRoot) : Child
        data class Registration(val component: RegistrationComponent) : Child
        data class Login(val component: LoginComponent) : Child
        data class Session(val component: SessionComponent) : Child
    }
}
