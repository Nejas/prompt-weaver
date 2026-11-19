package org.example.project.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.interaction.compliClickable
import org.example.project.ui.theme.CompliTheme

enum class CompliSelectionIndicator {
    Radio,
    Check,
}

/**
 * Интерактивная строка управляемого выбора с радио-индикатором или флажком.
 * Формированием группы и правилами одиночного или множественного выбора владеет вызывающий код.
 */
@Composable
fun CompliSelectionRow(
    selected: Boolean,
    onClick: () -> Unit,
    headline: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    supporting: (@Composable () -> Unit)? = null,
    indicator: CompliSelectionIndicator = CompliSelectionIndicator.Radio,
) {
    val shape = RoundedCornerShape(CompliTheme.radii.large)
    val borderColor = when {
        !enabled -> CompliTheme.colors.outlineVariant
        selected -> CompliTheme.colors.primary
        else -> CompliTheme.colors.outlineVariant
    }
    val backgroundColor = if (selected) {
        CompliTheme.colors.primary.copy(alpha = 0.10f)
    } else {
        CompliTheme.colors.surfaceContainer
    }
    val role = when (indicator) {
        CompliSelectionIndicator.Radio -> Role.RadioButton
        CompliSelectionIndicator.Check -> Role.Checkbox
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor, shape)
            .border(1.dp, borderColor, shape)
            .semantics(mergeDescendants = true) {
                when (indicator) {
                    CompliSelectionIndicator.Radio -> this.selected = selected
                    CompliSelectionIndicator.Check -> {
                        toggleableState = if (selected) ToggleableState.On else ToggleableState.Off
                    }
                }
            }
            .compliClickable(
                enabled = enabled,
                role = role,
                onClick = onClick,
            )
            .padding(
                PaddingValues(
                    horizontal = CompliTheme.spacing.large,
                    vertical = CompliTheme.spacing.medium,
                ),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(CompliTheme.spacing.medium))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.extraSmall),
        ) {
            ProvideTextStyle(CompliTheme.typography.titleSmall, headline)
            supporting?.let {
                CompositionLocalProvider(LocalContentColor provides CompliTheme.colors.onSurfaceVariant) {
                    ProvideTextStyle(CompliTheme.typography.bodySmall, it)
                }
            }
        }
        Spacer(Modifier.width(CompliTheme.spacing.medium))
        SelectionIndicator(
            selected = selected,
            enabled = enabled,
            style = indicator,
        )
    }
}

@Composable
private fun SelectionIndicator(
    selected: Boolean,
    enabled: Boolean,
    style: CompliSelectionIndicator,
) {
    when (style) {
        CompliSelectionIndicator.Radio -> CompliRadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
            modifier = Modifier.clearAndSetSemantics {},
        )

        CompliSelectionIndicator.Check -> CompliCheckbox(
            checked = selected,
            onCheckedChange = null,
            enabled = enabled,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}
