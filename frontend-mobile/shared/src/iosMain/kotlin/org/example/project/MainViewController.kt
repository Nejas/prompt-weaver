package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import org.example.project.di.initializeDependencies

private val rootComponent by lazy {
    initializeDependencies("http://localhost:8000/")
    createCatalogComponent(DefaultComponentContext(ApplicationLifecycle()))
}

fun MainViewController(): platform.UIKit.UIViewController {
    val component = rootComponent
    return ComposeUIViewController { App(component) }
}
