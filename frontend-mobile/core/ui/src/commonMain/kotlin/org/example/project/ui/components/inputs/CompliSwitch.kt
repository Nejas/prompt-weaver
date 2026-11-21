package org.example.project.ui.components.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.interaction.compliClickable
import org.example.project.ui.components.interaction.rememberCompliClick
import org.example.project.ui.theme.CompliTheme

/**
 * Переключатель для управляемого состояния [checked].
 * Передавайте `null` в [onCheckedChange] только когда взаимодействием владеет родитель.
 */
@Composable
fun CompliSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = CompliTheme.colors
    val handledChange = if (onCheckedChange != null) {
        rememberCompliClick { onCheckedChange(!checked) }
    } else {
        null
    }

    Switch(
        checked = checked,
        onCheckedChange = handledChange?.let { click -> { _ -> click() } },
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = colors.onPrimary,
            checkedTrackColor = colors.primary,
            checkedBorderColor = colors.primary,
            uncheckedThumbColor = colors.onSurfaceVariant,
            uncheckedTrackColor = colors.surfaceContainerHigh,
            uncheckedBorderColor = colors.outline,
            disabledCheckedThumbColor = colors.onSurfaceDisabled,
            disabledCheckedTrackColor = colors.surfaceContainerHighest,
            disabledCheckedBorderColor = colors.outlineVariant,
            disabledUncheckedThumbColor = colors.onSurfaceDisabled,
            disabledUncheckedTrackColor = colors.surfaceContainer,
            disabledUncheckedBorderColor = colors.outlineVariant,
        ),
    )
}

/**
 * Строка переключателя, у которой нажатие по всей строке меняет [checked].
 * Состоянием владеет вызывающий код и обновляет его через [onCheckedChange].
 */
@Composable
fun CompliSwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(CompliTheme.radii.small)

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .semantics(mergeDescendants = true) {
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
            }
            .compliClickable(
                enabled = enabled,
                role = Role.Switch,
                onClick = { onCheckedChange(!checked) },
            ),
        headlineContent = {
            Text(
                text = title,
                color = if (enabled) CompliTheme.colors.onSurface else CompliTheme.colors.onSurfaceDisabled,
                style = CompliTheme.typography.titleMedium,
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    color = if (enabled) CompliTheme.colors.onSurfaceVariant else CompliTheme.colors.onSurfaceDisabled,
                    style = CompliTheme.typography.bodyMedium,
                )
            }
        },
        leadingContent = leadingIcon,
        trailingContent = {
            CompliSwitch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
                modifier = Modifier.clearAndSetSemantics {},
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = CompliTheme.colors.surfaceContainerHigh,
            headlineColor = CompliTheme.colors.onSurface,
            supportingColor = CompliTheme.colors.onSurfaceVariant,
            leadingIconColor = CompliTheme.colors.onSurfaceVariant,
            trailingIconColor = CompliTheme.colors.onSurfaceVariant,
        ),
    )
}
