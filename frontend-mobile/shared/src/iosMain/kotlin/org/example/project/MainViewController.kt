package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import org.example.project.di.initializeDependencies
import org.example.project.di.createAppComponent
import platform.Foundation.NSBundle

private val rootComponent by lazy {
    val gatewayUrl = NSBundle.mainBundle.objectForInfoDictionaryKey("GatewayBaseUrl") as? String
        ?: error("GatewayBaseUrl is not configured")
    val dependencies = initializeDependencies(gatewayUrl, IosTokenStore())
    createAppComponent(dependencies, DefaultComponentContext(ApplicationLifecycle()))
}

fun MainViewController(): platform.UIKit.UIViewController {
    val component = rootComponent
    return ComposeUIViewController { App(component) }
}
