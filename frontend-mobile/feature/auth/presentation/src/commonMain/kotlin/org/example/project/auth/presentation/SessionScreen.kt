package org.example.project.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.example.project.ui.components.actions.CompliPrimaryButton
import org.example.project.ui.components.actions.CompliTextButton
import org.example.project.ui.theme.CompliTheme

@Composable
fun SessionScreen(component: SessionComponent) {
    val state by component.state.collectAsState()
    Column(Modifier.fillMaxSize().safeContentPadding().padding(CompliTheme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.medium)) {
        Text(if (state.loggedOut) "Вы вышли" else "Вы вошли в аккаунт", style = CompliTheme.typography.headlineSmall)
        if (!state.loggedOut) Text("Ваша сессия сохранена на устройстве")
        state.notice?.let { Text(it) }
        if (state.loggedOut) CompliPrimaryButton("Продолжить", component::continueAfterLogout, Modifier.fillMaxWidth())
        else {
            CompliTextButton("Открыть каталог", component.browseCatalog, Modifier.fillMaxWidth())
            CompliPrimaryButton("Выйти", component::logout, Modifier.fillMaxWidth(), loading = state.leaving)
        }
    }
}
