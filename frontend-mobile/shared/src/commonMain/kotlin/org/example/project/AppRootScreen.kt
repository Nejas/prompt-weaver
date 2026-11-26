package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.value.subscribe
import org.example.project.auth.presentation.LoginScreen
import org.example.project.auth.presentation.RegistrationScreen
import org.example.project.auth.presentation.SessionScreen
import org.example.project.catalog.presentation.CatalogRootScreen
import org.example.project.catalog.presentation.DetailsScreen
import org.example.project.chat.presentation.ChatScreen
import org.example.project.profile.presentation.ProfileScreen
import org.example.project.settings.presentation.SettingsScreen

@Composable
fun AppRootScreen(root: AppRoot) {
    var stack by remember(root) { mutableStateOf(root.stack.value) }
    DisposableEffect(root) {
        val subscription = root.stack.subscribe { stack = it }
        onDispose { subscription.cancel() }
    }
    when (val child = stack.active.instance) {
        AppRoot.Child.Loading -> AppLoadingScreen()
        is AppRoot.Child.Auth -> AuthFlowScreen(child.component)
        is AppRoot.Child.Catalog -> CatalogRootScreen(child.component)
        is AppRoot.Child.Chat -> ChatScreen(child.component)
        is AppRoot.Child.TemplateDetails -> DetailsScreen(child.component)
        is AppRoot.Child.Profile -> ProfileScreen(child.component)
        is AppRoot.Child.Session -> SessionScreen(child.component)
        is AppRoot.Child.Settings -> SettingsScreen(child.component)
    }
}

@Composable
private fun AuthFlowScreen(component: AuthFlowComponent) {
    when (val state = component.state.collectAsState().value) {
        is AuthFlowState.Login -> LoginScreen(state.component)
        is AuthFlowState.Registration -> RegistrationScreen(state.component)
    }
}
