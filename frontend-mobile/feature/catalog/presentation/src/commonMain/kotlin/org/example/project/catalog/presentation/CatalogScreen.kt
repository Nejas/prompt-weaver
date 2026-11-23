package org.example.project.catalog.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.example.project.ui.theme.CompliTheme
import org.example.project.ui.components.containers.CompliClickableSurface
import org.example.project.ui.components.actions.CompliPrimaryButton
import org.example.project.ui.components.feedback.CompliContentState
import org.example.project.ui.components.feedback.CompliContentStateType

@Composable
fun CatalogScreen(component: CatalogComponent) {
    val state by component.state.collectAsState()
    Column(Modifier.fillMaxSize().safeContentPadding().padding(CompliTheme.spacing.large)) {
        Text("Каталог шаблонов", style = MaterialTheme.typography.headlineSmall)
        if (component.hasAccount) {
            org.example.project.ui.components.actions.CompliTextButton(text = "Аккаунт", onClick = component::account)
        } else {
            CompliPrimaryButton(text = "Зарегистрироваться", onClick = component::register)
            org.example.project.ui.components.actions.CompliTextButton(text = "Войти", onClick = component::login)
        }
        Spacer(Modifier.height(CompliTheme.spacing.large))
        when (val current = state) {
            CatalogState.Loading -> CompliContentState(CompliContentStateType.Loading)
            CatalogState.Empty -> { Text("Шаблонов пока нет"); CompliPrimaryButton(text = "Обновить", onClick = component::reload) }
            CatalogState.Error -> { Text("Не удалось загрузить каталог"); CompliPrimaryButton(text = "Повторить", onClick = component::reload) }
            is CatalogState.Loaded -> {
                CompliPrimaryButton(text = "Обновить", onClick = component::reload)
                if (current.templates.size >= 50) Text("Показаны первые 50 шаблонов")
                LazyColumn {
                    items(current.templates, key = { it.versionId }) { template ->
                        CompliClickableSurface(onClick = { component.select(template.versionId) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth().padding(vertical = CompliTheme.spacing.medium)) {
                            Text(template.title, style = MaterialTheme.typography.titleMedium)
                            if (template.description.isNotBlank()) Text(template.description)
                        }
                        }
                    }
                }
            }
        }
    }
}
