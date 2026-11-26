package org.example.project

import org.example.project.ui.theme.CompliTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun App(component: AppRoot) {
    val settings by component.settings.collectAsState()
    CompliTheme(darkTheme = when (settings.theme) {
        org.example.project.settings.presentation.AppTheme.Light -> false
        org.example.project.settings.presentation.AppTheme.Dark -> true
        org.example.project.settings.presentation.AppTheme.System -> isSystemInDarkTheme()
    }) {
        Surface(modifier = Modifier.fillMaxSize(), color = CompliTheme.colors.background) {
            AppRootScreen(component)
        }
    }
}
