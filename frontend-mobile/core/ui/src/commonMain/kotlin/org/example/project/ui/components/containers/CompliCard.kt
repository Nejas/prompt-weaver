package org.example.project.ui.components.containers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.CompliTheme

/**
 * Статичная карточка для визуальной группировки связанного содержимого.
 * Для кликабельной карточки используйте [CompliClickableSurface].
 */
@Composable
fun CompliCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(CompliTheme.radii.card),
    color: Color = CompliTheme.colors.surfaceContainer,
    contentColor: Color = CompliTheme.colors.onSurface,
    border: BorderStroke? = BorderStroke(1.dp, CompliTheme.colors.outlineVariant),
    content: @Composable () -> Unit,
) {
    CompliSurface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        border = border,
        content = content,
    )
}
