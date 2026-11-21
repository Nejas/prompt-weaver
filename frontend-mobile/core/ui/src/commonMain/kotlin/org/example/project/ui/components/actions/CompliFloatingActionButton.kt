package org.example.project.ui.components.actions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.containers.CompliClickableSurface
import org.example.project.ui.theme.CompliTheme

/**
 * Расширенная плавающая кнопка для главного действия поверх содержимого экрана.
 * В отличие от обычной кнопки имеет фиксированную минимальную высоту и тень.
 * @param leadingContent необязательный значок перед содержимым
 */
@Composable
fun CompliFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CompliButtonColors = CompliButtonDefaults.primaryColors(),
    shape: Shape = RoundedCornerShape(CompliTheme.radii.large),
    leadingContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    CompliClickableSurface(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 56.dp, minHeight = 56.dp),
        enabled = enabled,
        role = Role.Button,
        shape = shape,
        color = if (enabled) colors.containerColor else colors.disabledContainerColor,
        contentColor = if (enabled) colors.contentColor else colors.disabledContentColor,
        shadowElevation = CompliTheme.elevations.floating,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(CompliTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent?.invoke()
            ProvideTextStyle(CompliTheme.typography.labelLarge, content)
        }
    }
}

/**
 * Текстовая перегрузка расширенной плавающей кнопки.
 * Предназначена для одного главного действия поверх содержимого экрана.
 * @param leadingContent необязательный значок перед текстом
 */
@Composable
fun CompliFloatingActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CompliButtonColors = CompliButtonDefaults.primaryColors(),
    shape: Shape = RoundedCornerShape(CompliTheme.radii.large),
    leadingContent: (@Composable () -> Unit)? = null,
) = CompliFloatingActionButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    shape = shape,
    leadingContent = leadingContent,
) {
    Text(text)
}

/**
 * Плавающая кнопка с одним значком: квадратная, поэтому не занимает ширину списка.
 * Как и у [CompliIconButton], описание для доступности обязательно.
 * @param onClickLabel уточнённое описание выполняемого нажатия
 */
@Composable
fun CompliIconFloatingActionButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClickLabel: String = contentDescription,
    enabled: Boolean = true,
    colors: CompliButtonColors = CompliButtonDefaults.primaryColors(),
    shape: Shape = RoundedCornerShape(CompliTheme.radii.large),
    content: @Composable () -> Unit,
) {
    CompliClickableSurface(
        onClick = onClick,
        modifier = modifier
            .size(IconFabSize)
            .semantics { this.contentDescription = contentDescription },
        enabled = enabled,
        role = Role.Button,
        onClickLabel = onClickLabel,
        shape = shape,
        color = if (enabled) colors.containerColor else colors.disabledContainerColor,
        contentColor = if (enabled) colors.contentColor else colors.disabledContentColor,
        shadowElevation = CompliTheme.elevations.floating,
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

private val IconFabSize = 56.dp
