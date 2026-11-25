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
import org.example.project.auth.presentation.generated.resources.Res
import org.example.project.auth.presentation.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun SessionScreen(component: SessionComponent) {
    val state by component.state.collectAsState()
    SessionScreenContent(
        state = state,
        onIntent = component::onIntent,
    )
}

@Composable
fun SessionScreenContent(
    state: SessionState,
    onIntent: (SessionIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding().padding(CompliTheme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.medium)
    ) {
        Text(if (state.loggedOut) stringResource(Res.string.auth_session_logged_out) else stringResource(Res.string.auth_session_logged_in), style = CompliTheme.typography.headlineSmall)
        if (!state.loggedOut) Text(stringResource(Res.string.auth_session_saved))
        state.notice?.let { Text(it.asText()) }
        if (state.loggedOut) {
            CompliPrimaryButton(
                text = stringResource(Res.string.auth_continue),
                onClick = { onIntent(SessionIntent.ContinueAfterLogout) },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            CompliTextButton(
                text = stringResource(Res.string.auth_open_catalog),
                onClick = { onIntent(SessionIntent.BrowseCatalog) },
                modifier = Modifier.fillMaxWidth()
            )
            CompliPrimaryButton(
                text = stringResource(Res.string.auth_logout),
                onClick = { onIntent(SessionIntent.Logout) },
                modifier = Modifier.fillMaxWidth(),
                loading = state.leaving
            )
        }
    }
}
