package org.example.project

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import org.example.project.auth.domain.AuthException
import org.example.project.auth.domain.AuthRepository
import org.example.project.auth.domain.SessionRepository
import org.example.project.auth.presentation.SessionComponent
import org.example.project.catalog.domain.CatalogRepository
import org.example.project.catalog.presentation.CatalogRoot
import org.example.project.chat.presentation.ChatComponent
import org.example.project.profile.presentation.ProfileComponent
import org.example.project.settings.presentation.SettingsComponent
import org.example.project.settings.presentation.SettingsState

class AppRoot(
    context: ComponentContext,
    catalogRepository: CatalogRepository,
    authRepository: AuthRepository,
    sessionRepository: SessionRepository,
    dispatcher: CoroutineDispatcher,
) : ComponentContext by context {
    private val navigation = StackNavigation<Config>()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutableSettings = kotlinx.coroutines.flow.MutableStateFlow(SettingsState())
    val settings = mutableSettings.asStateFlow()
    val stack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Loading,
        handleBackButton = true,
    ) { config, child ->
        when (config) {
            Config.Loading -> Child.Loading
            Config.Auth -> Child.Auth(
                AuthFlowComponent(
                    context = child,
                    authRepository = authRepository,
                    sessionRepository = sessionRepository,
                    dispatcher = dispatcher,
                    onAuthenticated = { navigation.replaceAll(Config.Catalog) },
                ),
            )
            Config.Catalog -> Child.Catalog(
                CatalogRoot(
                    context = child,
                    repository = catalogRepository,
                    dispatcher = dispatcher,
                    onRegister = { navigation.replaceAll(Config.Auth) },
                    onLogin = { navigation.replaceAll(Config.Auth) },
                    onAccount = { navigation.pushNew(Config.Profile) },
                ),
            )
            Config.Chat -> Child.Chat(
                ChatComponent(
                    context = child,
                    onProfile = { navigation.pushNew(Config.Profile) },
                    onCatalog = { navigation.pushNew(Config.Catalog) },
                    onSettings = { navigation.pushNew(Config.Settings) },
                ),
            )
            Config.Settings -> Child.Settings(SettingsComponent(child, mutableSettings, onBack = { navigation.pop() }))
            Config.Profile -> Child.Profile(
                ProfileComponent(
                    context = child,
                    fetchAccessToken = { sessionRepository.refresh()?.accessToken },
                    dispatcher = dispatcher,
                    onBack = { navigation.pop() },
                    onCatalog = { navigation.pop() },
                    onLogout = {
                        scope.launch {
                            try {
                                sessionRepository.logout()
                            } catch (error: CancellationException) {
                                throw error
                            } catch (_: Exception) {
                                // Local session removal is attempted by the repository even if revocation fails.
                            }
                            navigation.replaceAll(Config.Auth)
                        }
                    },
                    onSettings = { navigation.pushNew(Config.Settings) },
                ),
            )
            Config.Session -> Child.Session(
                SessionComponent(
                    context = child,
                    sessions = sessionRepository,
                    dispatcher = dispatcher,
                    onLoggedOut = { navigation.replaceAll(Config.Auth) },
                    browseCatalog = { navigation.replaceAll(Config.Catalog) },
                ),
            )
        }
    }

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        scope.launch {
            try {
                if (sessionRepository.refresh() != null) navigation.replaceAll(Config.Catalog)
                else navigation.replaceAll(Config.Auth)
            } catch (error: CancellationException) {
                throw error
            } catch (_: AuthException) {
                navigation.replaceAll(Config.Auth)
            } catch (_: Exception) {
                navigation.replaceAll(Config.Auth)
            }
        }
    }

    @Serializable
    sealed interface Config {
        @Serializable data object Loading : Config
        @Serializable data object Auth : Config
        @Serializable data object Catalog : Config
        @Serializable data object Chat : Config
        @Serializable data object Profile : Config
        @Serializable data object Session : Config
        @Serializable data object Settings : Config
    }

    sealed interface Child {
        data object Loading : Child
        data class Auth(val component: AuthFlowComponent) : Child
        data class Catalog(val component: CatalogRoot) : Child
        data class Chat(val component: ChatComponent) : Child
        data class Profile(val component: ProfileComponent) : Child
        data class Session(val component: SessionComponent) : Child
        data class Settings(val component: SettingsComponent) : Child
    }
}
