package org.example.project.ui.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.example.project.ui.theme.CompliTheme

enum class CompliAlertTone {
    Info,
    Warning,
    Error,
    Success,
}

/**
 * Заметное локальное сообщение, не заменяющее состояние всей области контента.
 * Для статуса используйте [CompliStatusBadge], для категории используйте [CompliChip], для пустого,
 * ошибочного или загружаемого контента используйте [CompliContentState]. Слот [action] не кликабелен сам
 * по себе: передайте в него кнопку или другой интерактивный компонент.
 */
@Composable
fun CompliAlert(
    tone: CompliAlertTone,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    body: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val color = tone.color()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(CompliTheme.radii.large),
        color = color.copy(alpha = 0.12f),
        contentColor = if (tone == CompliAlertTone.Success) CompliTheme.colors.onSurface else color,
    ) {
        Row(
            modifier = Modifier.padding(CompliTheme.spacing.large),
            horizontalArrangement = Arrangement.spacedBy(CompliTheme.spacing.medium),
            verticalAlignment = Alignment.Top,
        ) {
            icon?.invoke()
            Column(
                verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.small),
                modifier = Modifier.weight(1f),
            ) {
                title?.let { ProvideTextStyle(CompliTheme.typography.titleSmall, it) }
                body?.let {
                    CompositionLocalProvider(LocalContentColor provides CompliTheme.colors.onSurface) {
                        ProvideTextStyle(CompliTheme.typography.bodyMedium, it)
                    }
                }
                action?.let { ProvideTextStyle(CompliTheme.typography.labelLarge, it) }
            }
        }
    }
}

@Composable
private fun CompliAlertTone.color(): Color = when (this) {
    CompliAlertTone.Info -> CompliTheme.colors.info
    CompliAlertTone.Warning -> CompliTheme.colors.revision
    CompliAlertTone.Error -> CompliTheme.colors.rejected
    CompliAlertTone.Success -> CompliTheme.colors.approved
}
