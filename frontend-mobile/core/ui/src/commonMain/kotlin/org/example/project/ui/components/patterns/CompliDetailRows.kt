package org.example.project.ui.components.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.CompliTheme

/** Строка поля формы с меткой, значением и необязательным завершающим содержимым. */
@Composable
fun CompliFormRow(
    label: @Composable () -> Unit,
    value: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CompliTheme.spacing.large, vertical = CompliTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.extraSmall),
        ) {
            CompositionLocalProvider(LocalContentColor provides CompliTheme.colors.onSurfaceVariant) {
                ProvideTextStyle(CompliTheme.typography.bodySmall) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        label()
                        if (required) Text(" *", color = CompliTheme.colors.rejected)
                    }
                }
            }
            ProvideTextStyle(CompliTheme.typography.bodyMedium, value)
        }
        if (trailing != null) {
            Spacer(Modifier.width(CompliTheme.spacing.medium))
            ProvideTextStyle(CompliTheme.typography.labelMedium, trailing)
        }
    }
}

/** Строка требования с описанием, маркером и необязательным статусом. */
@Composable
fun CompliRequirementRow(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    subtitle: (@Composable () -> Unit)? = null,
    statusChip: (@Composable () -> Unit)? = null,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = CompliTheme.spacing.extraSmall),
    ) {
        val stackStatus = maxWidth < 320.dp || LocalDensity.current.fontScale > 1.2f

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
                ProvideTextStyle(CompliTheme.typography.titleMedium, title)
                subtitle?.let {
                    CompositionLocalProvider(LocalContentColor provides CompliTheme.colors.onSurfaceVariant) {
                        ProvideTextStyle(CompliTheme.typography.bodySmall, it)
                    }
                }
                if (stackStatus && statusChip != null) {
                    ProvideTextStyle(CompliTheme.typography.labelMedium, statusChip)
                }
            }
            if (!stackStatus && statusChip != null) {
                Spacer(Modifier.width(CompliTheme.spacing.medium))
                ProvideTextStyle(CompliTheme.typography.labelMedium, statusChip)
            }
        }
    }
}

/** Строка для компактного отображения пары «ключ — значение». */
@Composable
fun CompliKeyValueRow(
    key: @Composable () -> Unit,
    value: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CompliTheme.spacing.large, vertical = CompliTheme.spacing.small),
    ) {
        val stackValues = maxWidth < 280.dp || LocalDensity.current.fontScale > 1.2f

        if (stackValues) {
            Column(verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.extraSmall)) {
                CompositionLocalProvider(LocalContentColor provides CompliTheme.colors.onSurfaceVariant) {
                    ProvideTextStyle(CompliTheme.typography.bodySmall, key)
                }
                ProvideTextStyle(CompliTheme.typography.bodyMedium, value)
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CompliTheme.spacing.large),
                verticalAlignment = Alignment.Top,
            ) {
                Box(Modifier.weight(1f)) {
                    CompositionLocalProvider(LocalContentColor provides CompliTheme.colors.onSurfaceVariant) {
                        ProvideTextStyle(CompliTheme.typography.bodySmall, key)
                    }
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.TopEnd) {
                    ProvideTextStyle(CompliTheme.typography.bodyMedium, value)
                }
            }
        }
    }
}

/** Контейнер последовательных [CompliTimelineItem] без разрывов соединительной линии. */
@Composable
fun CompliTimeline(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier, content = content)
}

/**
 * Элемент вертикальной временной шкалы.
 * Для последнего элемента передайте `isLast = true`, чтобы скрыть продолжение линии.
 */
@Composable
fun CompliTimelineItem(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    subtitle: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    isLast: Boolean = false,
) {
    val density = LocalDensity.current
    val stackTrailing = density.fontScale > 1.2f
    val scaledLabelHeight = with(density) { CompliTheme.typography.labelSmall.lineHeight.toDp() }
    val markerContentSize = if (density.fontScale > 1f) {
        scaledLabelHeight + 2.dp
    } else {
        CompliTheme.iconSizes.small
    }
    val markerSize = markerContentSize.coerceAtLeast(CompliTheme.iconSizes.medium)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = CompliTheme.spacing.large),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(markerSize)
                    .clip(CircleShape)
                    .background(CompliTheme.colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.size(markerContentSize),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides CompliTheme.colors.onPrimaryContainer,
                    ) {
                        ProvideTextStyle(CompliTheme.typography.labelSmall) {
                            leading?.invoke()
                        }
                    }
                }
            }
            if (!isLast) {
                Box(
                    Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(CompliTheme.colors.outlineVariant),
                )
            }
        }
        Spacer(Modifier.width(CompliTheme.spacing.medium))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = CompliTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.extraSmall),
        ) {
            ProvideTextStyle(CompliTheme.typography.titleSmall, title)
            subtitle?.let {
                CompositionLocalProvider(LocalContentColor provides CompliTheme.colors.onSurfaceVariant) {
                    ProvideTextStyle(CompliTheme.typography.bodySmall, it)
                }
            }
            if (stackTrailing && trailing != null) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    CompositionLocalProvider(LocalContentColor provides CompliTheme.colors.onSurfaceVariant) {
                        ProvideTextStyle(CompliTheme.typography.labelMedium, trailing)
                    }
                }
            }
        }
        if (!stackTrailing && trailing != null) {
            Spacer(Modifier.width(CompliTheme.spacing.medium))
            CompositionLocalProvider(LocalContentColor provides CompliTheme.colors.onSurfaceVariant) {
                ProvideTextStyle(CompliTheme.typography.labelMedium, trailing)
            }
        }
    }
}
