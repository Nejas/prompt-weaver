package org.example.project.ui.components.containers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.example.project.ui.theme.CompliTheme

/**
 * Модальный диалог с произвольными слотами содержимого и действий.
 * [onDismissRequest] только сообщает о запросе закрытия: владелец состояния должен убрать диалог из композиции.
 */
@Composable
fun CompliDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CompliTheme.colors.surface,
    contentPadding: PaddingValues = PaddingValues(CompliTheme.spacing.extraLarge),
    iconToContentSpacing: Dp = CompliTheme.spacing.large,
    contentToActionsSpacing: Dp = CompliTheme.spacing.extraLarge,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    body: (@Composable () -> Unit)? = null,
    confirm: (@Composable () -> Unit)? = null,
    dismiss: (@Composable () -> Unit)? = null,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        CompliSurface(
            modifier = modifier
                .widthIn(max = 304.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(CompliTheme.radii.dialog),
            color = containerColor,
            contentColor = CompliTheme.colors.onSurface,
            shadowElevation = CompliTheme.elevations.dialog,
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (icon != null) {
                    icon()
                    if (title != null || body != null || confirm != null || dismiss != null) {
                        Spacer(Modifier.height(iconToContentSpacing))
                    }
                }
                if (title != null) {
                    ProvideTextStyle(
                        value = CompliTheme.typography.titleLarge,
                        content = title,
                    )
                }
                if (body != null) {
                    if (title != null) {
                        Spacer(Modifier.height(CompliTheme.spacing.small))
                    }
                    CompositionLocalProvider(
                        LocalContentColor provides CompliTheme.colors.onSurfaceVariant,
                    ) {
                        ProvideTextStyle(
                            value = CompliTheme.typography.bodyMedium,
                            content = body,
                        )
                    }
                }
                if (confirm != null || dismiss != null) {
                    if (icon != null || title != null || body != null) {
                        Spacer(Modifier.height(contentToActionsSpacing))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (dismiss != null) {
                            Box(Modifier.weight(1f)) {
                                dismiss()
                            }
                            if (confirm != null) {
                                Spacer(Modifier.width(CompliTheme.spacing.small))
                            }
                        }
                        if (confirm != null) {
                            Box(Modifier.weight(1f)) {
                                confirm()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Диалог подтверждения для короткого решения с основной и необязательной отменяющей кнопкой.
 * Обработчики не закрывают диалог автоматически: состояние показа остаётся у вызывающего кода.
 */
@Composable
fun CompliConfirmationDialog(
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    body: String? = null,
    dismissLabel: String? = null,
    icon: (@Composable () -> Unit)? = null,
    destructive: Boolean = false,
) {
    CompliDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        icon = icon,
        title = {
            Text(text = title, textAlign = TextAlign.Center)
        },
        body = body?.let {
            {
                Text(
                    text = it,
                    color = CompliTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
        dismiss = dismissLabel?.let {
            {
                CompliDialogAction(
                    label = it,
                    onClick = onDismissRequest,
                    primary = false,
                )
            }
        },
        confirm = {
            CompliDialogAction(
                label = confirmLabel,
                onClick = onConfirm,
                primary = true,
                destructive = destructive,
            )
        },
    )
}

@Composable
private fun CompliDialogAction(
    label: String,
    onClick: () -> Unit,
    primary: Boolean,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
) {
    CompliClickableSurface(
        onClick = onClick,
        modifier = modifier,
        role = Role.Button,
        shape = RoundedCornerShape(CompliTheme.radii.medium),
        color = when {
            destructive -> CompliTheme.colors.rejected
            primary -> CompliTheme.colors.primary
            else -> Color.Transparent
        },
        contentColor = when {
            destructive -> CompliTheme.colors.onRejected
            primary -> CompliTheme.colors.onPrimary
            else -> CompliTheme.colors.onSurface
        },
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CompliTheme.spacing.medium, vertical = CompliTheme.spacing.medium),
            style = CompliTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
}
