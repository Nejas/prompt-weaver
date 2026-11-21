package org.example.project.ui.components.containers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.actions.CompliPrimaryButton
import org.example.project.ui.theme.CompliTheme

/**
 * Модальный bottom sheet с необязательными заголовком и нижними действиями.
 * Владелец состояния должен обработать [onDismissRequest] и убрать sheet из композиции; системные отступы обрабатывает Material.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompliBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    horizontalPadding: Dp = CompliTheme.spacing.extraLarge,
    title: (@Composable () -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = CompliTheme.radii.sheet,
            topEnd = CompliTheme.radii.sheet,
        ),
        containerColor = CompliTheme.colors.surfaceContainer,
        contentColor = CompliTheme.colors.onSurface,
        tonalElevation = 0.dp,
        dragHandle = { CompliBottomSheetDragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = CompliTheme.spacing.extraLarge,
                ),
        ) {
            if (title != null) {
                ProvideTextStyle(CompliTheme.typography.titleLarge, title)
                Spacer(Modifier.height(CompliTheme.spacing.large))
            }

            content()

            if (footer != null) {
                Spacer(Modifier.height(CompliTheme.spacing.extraLarge))
                footer()
            }
        }
    }
}

@Composable
private fun CompliBottomSheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = CompliTheme.spacing.medium)
            .size(width = 32.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(CompliTheme.colors.outline),
    )
}
