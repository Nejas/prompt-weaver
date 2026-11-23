package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.value.subscribe
import org.example.project.auth.presentation.RegistrationScreen
import org.example.project.catalog.presentation.CatalogRootScreen

@Composable
fun AppRootScreen(root: AppRoot) {
    var stack by remember(root) { mutableStateOf(root.stack.value) }
    DisposableEffect(root) {
        val subscription = root.stack.subscribe { stack = it }
        onDispose { subscription.cancel() }
    }
    when (val child = stack.active.instance) {
        is AppRoot.Child.Catalog -> CatalogRootScreen(child.component)
        is AppRoot.Child.Registration -> RegistrationScreen(child.component)
    }
}
