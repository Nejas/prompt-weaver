package org.example.project.ui.components.actions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.containers.CompliClickableSurface
import org.example.project.ui.theme.CompliTheme

/**
 * Компактная кнопка для действия, представленного только значком.
 * В отличие от текстовой кнопки требует непустого описания для доступности.
 * @param onClickLabel уточнённое описание выполняемого нажатия
 */
@Composable
fun CompliIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClickLabel: String = contentDescription,
    enabled: Boolean = true,
    colors: CompliButtonColors = CompliButtonDefaults.tonalColors(),
    shape: Shape = CircleShape,
    size: Dp = 48.dp,
    content: @Composable () -> Unit,
) {
    CompliClickableSurface(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .semantics { this.contentDescription = contentDescription },
        enabled = enabled,
        role = Role.Button,
        onClickLabel = onClickLabel,
        shape = shape,
        color = if (enabled) colors.containerColor else colors.disabledContainerColor,
        contentColor = if (enabled) colors.contentColor else colors.disabledContentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}
