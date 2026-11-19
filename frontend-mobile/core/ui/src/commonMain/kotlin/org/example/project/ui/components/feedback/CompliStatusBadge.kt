package org.example.project.ui.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.example.project.ui.theme.CompliTheme

enum class CompliStatusTone {
    Offline,
    Pending,
    Revision,
    Approved,
    Rejected,
    Info,
}

/**
 * Компактно показывает текущий статус сущности или процесса. Для категории используйте
 * [CompliChip], для сообщения используйте [CompliAlert], для состояния всей области используйте [CompliContentState].
 */
@Composable
fun CompliStatusBadge(
    text: String,
    tone: CompliStatusTone,
    modifier: Modifier = Modifier,
) {
    val color = tone.color()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(CompliTheme.radii.small),
        color = color.copy(alpha = if (tone == CompliStatusTone.Approved) 0.10f else 0.14f),
        contentColor = color,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = CompliTheme.spacing.small,
                vertical = CompliTheme.spacing.extraSmall,
            ),
            style = CompliTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun CompliStatusTone.color(): Color = when (this) {
    CompliStatusTone.Offline -> CompliTheme.colors.offline
    CompliStatusTone.Pending -> CompliTheme.colors.pending
    CompliStatusTone.Revision -> CompliTheme.colors.revision
    CompliStatusTone.Approved -> CompliTheme.colors.approved
    CompliStatusTone.Rejected -> CompliTheme.colors.rejected
    CompliStatusTone.Info -> CompliTheme.colors.info
}
