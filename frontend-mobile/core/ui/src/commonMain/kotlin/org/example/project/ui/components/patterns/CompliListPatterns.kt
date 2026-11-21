package org.example.project.ui.components.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.interaction.compliClickable
import org.example.project.ui.components.inputs.CompliRadioButton
import org.example.project.ui.theme.CompliTheme

/**
 * Универсальный элемент списка с необязательными областями и обработчиком на всю строку.
 * Состояние выбора хранит вызывающий код.
 */
@Composable
fun CompliListItem(
    headline: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    supporting: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    selected: Boolean? = null,
    onClick: (() -> Unit)? = null,
) {
    val interactionModifier = if (onClick == null) {
        Modifier
    } else {
        Modifier.compliClickable(onClick = onClick)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (selected == null) {
                    Modifier
                } else {
                    Modifier.semantics { this.selected = selected }
                },
            )
            .then(interactionModifier)
            .background(if (selected == true) CompliTheme.colors.primaryContainer else Color.Transparent)
            .padding(horizontal = CompliTheme.spacing.large, vertical = CompliTheme.spacing.medium)
            .heightIn(min = CompliTheme.iconSizes.large),
        contentAlignment = Alignment.CenterStart,
    ) {
        val stackTrailing = maxWidth < 240.dp || LocalDensity.current.fontScale > 1.2f

        Row(
            modifier = Modifier.fillMaxWidth(),
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
                if (stackTrailing && trailing != null) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        ProvideTextStyle(CompliTheme.typography.labelMedium, trailing)
                    }
                }
            }
            if (!stackTrailing && trailing != null) {
                Spacer(Modifier.width(CompliTheme.spacing.medium))
                ProvideTextStyle(CompliTheme.typography.labelMedium, trailing)
            }
        }
    }
}

/**
 * Строка одиночного выбора; `selected` и его изменение контролирует вызывающий код.
 * Не размещайте вложенные кликабельные элементы: вся строка уже является контролом.
 */
@Composable
fun CompliOptionRow(
    selected: Boolean,
    onClick: () -> Unit,
    headline: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    supporting: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .semantics { this.selected = selected }
            .compliClickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .background(if (selected) CompliTheme.colors.primary.copy(alpha = 0.10f) else Color.Transparent)
            .padding(horizontal = CompliTheme.spacing.large, vertical = CompliTheme.spacing.medium),
    ) {
        val stackTrailing = maxWidth < 240.dp || LocalDensity.current.fontScale > 1.2f

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompliRadioButton(
                selected = selected,
                onClick = null,
                enabled = enabled,
                modifier = Modifier.clearAndSetSemantics {},
            )
            Spacer(Modifier.width(CompliTheme.spacing.medium))
            leading?.invoke()
            if (leading != null) Spacer(Modifier.width(CompliTheme.spacing.medium))
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
                if (stackTrailing && trailing != null) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        ProvideTextStyle(CompliTheme.typography.labelMedium, trailing)
                    }
                }
            }
            if (!stackTrailing && trailing != null) {
                Spacer(Modifier.width(CompliTheme.spacing.medium))
                ProvideTextStyle(CompliTheme.typography.labelMedium, trailing)
            }
        }
    }
}

/**
 * Заголовок секции; слот `action` предназначен для необязательного действия справа.
 */
@Composable
fun CompliSectionHeader(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CompliTheme.spacing.large, vertical = CompliTheme.spacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            ProvideTextStyle(CompliTheme.typography.titleSmall, title)
        }
        if (action != null) {
            Spacer(Modifier.width(CompliTheme.spacing.medium))
            ProvideTextStyle(CompliTheme.typography.labelMedium, action)
        }
    }
}
