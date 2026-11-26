package org.example.project.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.profile.presentation.generated.resources.*
import org.example.project.ui.components.actions.*
import org.example.project.ui.components.containers.CompliSurface
import org.example.project.ui.theme.CompliTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileScreen(component: ProfileComponent) {
    val state by component.state.collectAsState()
    ProfileScreenContent(state, component::onIntent)
}

@Composable
fun ProfileScreenContent(state: ProfileState, onIntent: (ProfileIntent) -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = CompliTheme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.large)
    ) {
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
            CompliIconButton(
                onClick = { onIntent(ProfileIntent.Back) },
                contentDescription = stringResource(Res.string.profile_back),
                colors = CompliButtonDefaults.textColors(),
                size = 36.dp
            ) {
                Text("‹", style = CompliTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(16.dp))
            Text(stringResource(Res.string.profile_title), style = CompliTheme.typography.titleLarge)
        }

        CompliSurface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), CompliTheme.colors.surfaceContainer) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                CompliSurface(Modifier.size(56.dp), RoundedCornerShape(28.dp), CompliTheme.colors.surfaceContainerHigh) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(state.avatarInitial, style = CompliTheme.typography.titleLarge)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(state.displayName, style = CompliTheme.typography.titleLarge)
                    if (state.email.isNotBlank()) {
                        Text(state.email, style = CompliTheme.typography.labelMedium, color = CompliTheme.colors.onSurfaceVariant)
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(Res.string.profile_main_data), style = CompliTheme.typography.titleSmall, color = CompliTheme.colors.onSurfaceVariant)
            CompliSurface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), CompliTheme.colors.surfaceContainer) {
                Column {
                    ProfileRow(stringResource(Res.string.profile_first_name), state.firstName.ifBlank { "—" })
                    ProfileRow(stringResource(Res.string.profile_last_name_label), state.lastName.ifBlank { "—" })
                    ProfileRow(stringResource(Res.string.profile_email_label), state.email.ifBlank { "—" }, false)
                }
            }
        }

        Spacer(Modifier.height(CompliTheme.spacing.medium))

        CompliPrimaryButton(
            text = "Вернуться в чат",
            onClick = { onIntent(ProfileIntent.OpenChat) },
            modifier = Modifier.fillMaxWidth()
        )
        CompliTextButton(text = "Настройки", onClick = { onIntent(ProfileIntent.OpenSettings) }, modifier = Modifier.fillMaxWidth())

        CompliTextButton(
            text = "Выйти из аккаунта",
            onClick = { onIntent(ProfileIntent.Logout) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoggingOut
        )
    }
}

@Composable
private fun ProfileRow(label: String, value: String, divider: Boolean = true) {
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = CompliTheme.typography.bodySmall, color = CompliTheme.colors.onSurfaceVariant)
        Text(value, style = CompliTheme.typography.bodyLarge)
    }
    if (divider) {
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(CompliTheme.colors.outlineVariant))
    }
}
