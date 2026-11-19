package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import org.example.project.di.initializeDependencies

private val dependenciesInitialized = lazy { initializeDependencies("http://localhost:8000/") }

fun MainViewController() = ComposeUIViewController {
    dependenciesInitialized.value
    App()
}
