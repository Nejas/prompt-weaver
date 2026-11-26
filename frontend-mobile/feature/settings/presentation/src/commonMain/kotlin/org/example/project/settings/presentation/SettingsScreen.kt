package org.example.project.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.ui.components.actions.CompliButtonDefaults
import org.example.project.ui.components.actions.CompliIconButton
import org.example.project.ui.components.containers.CompliSurface
import org.example.project.ui.components.inputs.CompliSwitchRow
import org.example.project.ui.theme.CompliTheme
import org.example.project.settings.presentation.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable fun SettingsScreen(component: SettingsComponent) { val state by component.state.collectAsState(); SettingsScreenContent(state, component::onIntent) }
@Composable fun SettingsScreenContent(state: SettingsState, onIntent: (SettingsIntent) -> Unit) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row { CompliIconButton({ onIntent(SettingsIntent.Back) }, stringResource(Res.string.settings_back), colors = CompliButtonDefaults.textColors(), size = 40.dp) { Text("‹", style = CompliTheme.typography.titleLarge) }; Spacer(Modifier.width(12.dp)); Text(stringResource(Res.string.settings_title), style = CompliTheme.typography.titleLarge) }
        SettingsSection(stringResource(Res.string.settings_appearance)) { ThemeChoices(state.theme) { onIntent(SettingsIntent.SelectTheme(it)) }; Text(stringResource(Res.string.settings_font), style = CompliTheme.typography.titleSmall); Text(stringResource(Res.string.settings_font_value), color = CompliTheme.colors.onSurfaceVariant); Text(stringResource(Res.string.settings_text_size, state.textSize), style = CompliTheme.typography.titleSmall); Slider(state.textSize.toFloat(), { onIntent(SettingsIntent.ChangeTextSize(it.toInt())) }, valueRange = 12f..22f); Text(stringResource(Res.string.settings_preview), style = CompliTheme.typography.bodyMedium.copy(fontSize = state.textSize.sp)) }
        SettingsSection(stringResource(Res.string.settings_templates)) { SettingRow(stringResource(Res.string.settings_ready_templates), stringResource(Res.string.settings_ready_templates_subtitle)); SettingRow(stringResource(Res.string.settings_my_templates), stringResource(Res.string.settings_my_templates_subtitle)); SettingRow(stringResource(Res.string.settings_my_categories), stringResource(Res.string.settings_my_categories_subtitle)) }
        SettingsSection(stringResource(Res.string.settings_chat)) { CompliSwitchRow(state.saveHistory, { onIntent(SettingsIntent.SetSaveHistory(it)) }, stringResource(Res.string.settings_save_history)) }
    }
}
@Composable private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(title, style = CompliTheme.typography.titleSmall, color = CompliTheme.colors.onSurfaceVariant); CompliSurface(Modifier.fillMaxWidth(), content = { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }) } }
@Composable private fun ThemeChoices(selected: AppTheme, select: (AppTheme) -> Unit) { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { AppTheme.entries.forEach { theme -> androidx.compose.material3.FilterChip(selected = selected == theme, onClick = { select(theme) }, label = { Text(stringResource(when (theme) { AppTheme.Light -> Res.string.settings_theme_light; AppTheme.Dark -> Res.string.settings_theme_dark; AppTheme.System -> Res.string.settings_theme_system })) }) } } }
@Composable private fun SettingRow(title: String, subtitle: String) { Column { Text(title, style = CompliTheme.typography.titleSmall); Text(subtitle, style = CompliTheme.typography.bodySmall, color = CompliTheme.colors.onSurfaceVariant) } }
