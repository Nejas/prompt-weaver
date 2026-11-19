package org.example.project.catalog.presentation

import androidx.compose.runtime.*
import com.arkivanov.decompose.value.subscribe

@Composable
fun CatalogRootScreen(root: CatalogRoot) {
    var stack by remember(root) { mutableStateOf(root.stack.value) }
    DisposableEffect(root) {
        val subscription = root.stack.subscribe { stack = it }
        onDispose { subscription.cancel() }
    }
    when (val child = stack.active.instance) {
        is CatalogRoot.Child.List -> CatalogScreen(child.component)
        is CatalogRoot.Child.Details -> DetailsScreen(child.component)
    }
}
