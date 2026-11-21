package org.example.project.ui.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.CompliTheme

enum class CompliChipStyle {
    Neutral,
    Info,
    Success,
    Warning,
    Error,
}

object CompliChipDefaults {
    val ContentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
}

/**
 * Компактная метка категории или свойства. Для состояния сущности используйте [CompliStatusBadge],
 * для заметного сообщения используйте [CompliAlert], для состояния всей области используйте [CompliContentState].
 * [leadingContent] только размещает контент и сам по себе не делает чип кликабельным.
 */
@Composable
fun CompliChip(
    text: String,
    modifier: Modifier = Modifier,
    style: CompliChipStyle = CompliChipStyle.Neutral,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    val colors = style.colors()
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = colors.first,
        contentColor = colors.second,
    ) {
        Row(
            modifier = Modifier.padding(CompliChipDefaults.ContentPadding),
            horizontalArrangement = Arrangement.spacedBy(CompliTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent?.invoke()
            Text(text = text, style = CompliTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun CompliChipStyle.colors(): Pair<Color, Color> = when (this) {
    CompliChipStyle.Neutral -> CompliTheme.colors.surfaceContainer to CompliTheme.colors.onSurfaceVariant
    CompliChipStyle.Info -> CompliTheme.colors.info.copy(alpha = 0.14f) to CompliTheme.colors.info
    CompliChipStyle.Success -> CompliTheme.colors.approved.copy(alpha = 0.10f) to CompliTheme.colors.approved
    CompliChipStyle.Warning -> CompliTheme.colors.primaryContainer to CompliTheme.colors.onPrimaryContainer
    CompliChipStyle.Error -> CompliTheme.colors.rejected.copy(alpha = 0.14f) to CompliTheme.colors.rejected
}
