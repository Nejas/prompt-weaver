package org.example.project.ui.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.ui.theme.CompliTheme

enum class CompliContentStateType {
    Empty,
    Error,
    Loading,
}

/**
 * Заполняет область представлением пустого, ошибочного или загружаемого контента.
 * Для локального сообщения используйте [CompliAlert], для статуса используйте [CompliStatusBadge],
 * для категории используйте [CompliChip]. Слот [action] не кликабелен сам по себе: передайте кнопку.
 */
@Composable
fun CompliContentState(
    state: CompliContentStateType,
    modifier: Modifier = Modifier,
    visual: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    description: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(CompliTheme.spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.medium),
    ) {
        when {
            visual != null -> visual()
            state == CompliContentStateType.Loading -> CircularProgressIndicator(
                modifier = Modifier.size(CompliTheme.iconSizes.large),
                color = CompliTheme.colors.primary,
            )
        }
        title?.let {
            CompositionLocalProvider(LocalContentColor provides CompliTheme.colors.onSurface) {
                ProvideTextStyle(CompliTheme.typography.titleMedium, it)
            }
        }
        description?.let {
            CompositionLocalProvider(LocalContentColor provides CompliTheme.colors.onSurfaceVariant) {
                ProvideTextStyle(CompliTheme.typography.bodyMedium, it)
            }
        }
        action?.let {
            CompositionLocalProvider(LocalContentColor provides CompliTheme.colors.info) {
                ProvideTextStyle(CompliTheme.typography.labelLarge, it)
            }
        }
    }
}
