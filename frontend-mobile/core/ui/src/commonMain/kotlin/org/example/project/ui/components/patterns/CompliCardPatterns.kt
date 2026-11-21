package org.example.project.ui.components.patterns

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.containers.CompliCard
import org.example.project.ui.components.containers.CompliClickableSurface
import org.example.project.ui.theme.CompliTheme

/**
 * Плитка вложения, целиком кликабельная при заданном `onClick`.
 * В этом режиме не помещайте интерактивные контролы во внутренние слоты.
 */
@Composable
fun CompliAttachmentTile(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    leadingContainerColor: Color? = null,
    leadingContainerShape: Shape? = null,
    supporting: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(CompliTheme.radii.large)
    val content: @Composable () -> Unit = {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    PaddingValues(
                        horizontal = CompliTheme.spacing.large,
                        vertical = CompliTheme.spacing.medium,
                    ),
                ),
        ) {
            val stackTrailing = maxWidth < 240.dp || LocalDensity.current.fontScale > 1.2f

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leading != null) {
                    Box(
                        modifier = Modifier
                            .size(CompliTheme.iconSizes.large)
                            .background(
                                leadingContainerColor ?: CompliTheme.colors.primaryContainer,
                                leadingContainerShape ?: CompliTheme.shapes.small,
                            ),
                        contentAlignment = Alignment.Center,
                    ) { leading() }
                    Spacer(Modifier.width(CompliTheme.spacing.medium))
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.extraSmall),
                ) {
                    ProvideTextStyle(CompliTheme.typography.titleSmall, title)
                    supporting?.let {
                        CompositionLocalProvider(LocalContentColor provides CompliTheme.colors.onSurfaceVariant) {
                            ProvideTextStyle(CompliTheme.typography.bodySmall, it)
                        }
                    }
                    if (stackTrailing && trailing != null) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
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

    if (onClick == null) {
        CompliCard(modifier = modifier, shape = shape, content = content)
    } else {
        CompliClickableSurface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = CompliTheme.colors.surfaceContainer,
            border = BorderStroke(1.dp, CompliTheme.colors.outlineVariant),
            content = content,
        )
    }
}

/**
 * Карточка сводки с заголовком, содержимым и подвалом.
 * При заданном `onClick` не размещайте внутри другие интерактивные контролы.
 */
@Composable
fun CompliSummaryCard(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(CompliTheme.radii.large)
    val cardContent: @Composable () -> Unit = {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    PaddingValues(
                        horizontal = CompliTheme.spacing.large,
                        vertical = CompliTheme.spacing.medium,
                    ),
                ),
        ) {
            val stackHeader = maxWidth < 280.dp || LocalDensity.current.fontScale > 1.2f

            Column(verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.small)) {
            if (title != null || trailing != null) {
                    if (stackHeader) {
                        title?.let { ProvideTextStyle(CompliTheme.typography.titleMedium, it) }
                        trailing?.let { ProvideTextStyle(CompliTheme.typography.labelMedium, it) }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (title != null) {
                                Box(Modifier.weight(1f)) {
                                    ProvideTextStyle(CompliTheme.typography.titleMedium, title)
                                }
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                            trailing?.let { ProvideTextStyle(CompliTheme.typography.labelMedium, it) }
                        }
                    }
                }
                ProvideTextStyle(CompliTheme.typography.bodyMedium, content)
                footer?.let {
                    CompositionLocalProvider(LocalContentColor provides CompliTheme.colors.onSurfaceVariant) {
                        ProvideTextStyle(CompliTheme.typography.bodySmall, it)
                    }
                }
            }
        }
    }

    if (onClick == null) {
        CompliCard(modifier = modifier, shape = shape, content = cardContent)
    } else {
        CompliClickableSurface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = CompliTheme.colors.surfaceContainer,
            border = BorderStroke(1.dp, CompliTheme.colors.outlineVariant),
            content = cardContent,
        )
    }
}
