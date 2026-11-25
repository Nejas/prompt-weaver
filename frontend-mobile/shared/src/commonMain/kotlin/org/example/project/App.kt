package org.example.project

import org.example.project.ui.theme.CompliTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable

@Composable
fun App(component: AppRoot) {
    CompliTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize(), color = CompliTheme.colors.background) {
            AppRootScreen(component)
        }
    }
}
