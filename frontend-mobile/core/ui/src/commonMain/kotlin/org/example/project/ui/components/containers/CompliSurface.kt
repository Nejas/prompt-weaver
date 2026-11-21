package org.example.project.ui.components.containers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.interaction.compliClickable
import org.example.project.ui.theme.CompliTheme

/**
 * Статичная тематическая поверхность для группировки содержимого.
 * Для обработки нажатий используйте [CompliClickableSurface], не добавляйте к этой поверхности кликабельность.
 */
@Composable
fun CompliSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(CompliTheme.radii.large),
    color: Color = CompliTheme.colors.surface,
    contentColor: Color = CompliTheme.colors.onSurface,
    border: BorderStroke? = null,
    shadowElevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        border = border,
        shadowElevation = shadowElevation,
        content = content,
    )
}

/**
 * Кликабельная поверхность для единого действия со всем содержимым.
 * Передавайте корректные [role] и [onClickLabel], когда действие нельзя однозначно озвучить по содержимому.
 */
@Composable
fun CompliClickableSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    role: Role? = null,
    onClickLabel: String? = null,
    shape: Shape = RoundedCornerShape(CompliTheme.radii.large),
    color: Color = CompliTheme.colors.surface,
    contentColor: Color = CompliTheme.colors.onSurface,
    border: BorderStroke? = null,
    shadowElevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    CompliSurface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        border = border,
        shadowElevation = shadowElevation,
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .semantics(mergeDescendants = true) {}
                .compliClickable(
                enabled = enabled,
                role = role,
                onClickLabel = onClickLabel,
                onClick = onClick,
            ),
            propagateMinConstraints = true,
        ) {
            content()
        }
    }
}
