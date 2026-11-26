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
import org.example.project.ui.components.actions.CompliTextButton
import org.example.project.ui.components.feedback.CompliContentState
import org.example.project.ui.components.feedback.CompliContentStateType

@Composable
fun CatalogScreen(component: CatalogComponent) {
    val state by component.state.collectAsState()
    CatalogScreenContent(
        state = state,
        hasAccount = component.hasAccount,
        canOpenChat = component.canOpenChat,
        onIntent = component::onIntent
    )
}

@Composable
fun CatalogScreenContent(
    state: CatalogState,
    hasAccount: Boolean,
    onIntent: (CatalogIntent) -> Unit,
    canOpenChat: Boolean = false,
) {
    Column(Modifier.fillMaxSize().safeContentPadding().padding(CompliTheme.spacing.large)) {
        Text("Каталог шаблонов", style = MaterialTheme.typography.headlineSmall)
        if (canOpenChat) {
            CompliTextButton(text = "Чат", onClick = { onIntent(CatalogIntent.OpenChat) })
        }
        if (hasAccount) {
            CompliTextButton(text = "Аккаунт", onClick = { onIntent(CatalogIntent.Account) })
        } else {
            CompliPrimaryButton(text = "Зарегистрироваться", onClick = { onIntent(CatalogIntent.Register) })
            CompliTextButton(text = "Войти", onClick = { onIntent(CatalogIntent.Login) })
        }
        Spacer(Modifier.height(CompliTheme.spacing.large))
        when (state) {
            CatalogState.Loading -> CompliContentState(CompliContentStateType.Loading)
            CatalogState.Empty -> {
                Text("Шаблонов пока нет")
                CompliPrimaryButton(text = "Обновить", onClick = { onIntent(CatalogIntent.Reload) })
            }
            CatalogState.Error -> {
                Text("Не удалось загрузить каталог")
                CompliPrimaryButton(text = "Повторить", onClick = { onIntent(CatalogIntent.Reload) })
            }
            is CatalogState.Loaded -> {
                CompliPrimaryButton(text = "Обновить", onClick = { onIntent(CatalogIntent.Reload) })
                if (state.templates.size >= 50) Text("Показаны первые 50 шаблонов")
                LazyColumn {
                    items(state.templates, key = { it.versionId }) { template ->
                        CompliClickableSurface(
                            onClick = { onIntent(CatalogIntent.SelectTemplate(template.versionId)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
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
