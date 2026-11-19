package org.example.project

import org.example.project.ui.theme.CompliTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import org.example.project.catalog.presentation.CatalogRoot
import org.example.project.catalog.presentation.CatalogRootScreen
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

fun createCatalogComponent(context: ComponentContext): CatalogRoot =
    GlobalContext.get().get { parametersOf(context) }

@Composable
fun App(component: CatalogRoot) {
    CompliTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = CompliTheme.colors.background) {
            CatalogRootScreen(component)
        }
    }
}
