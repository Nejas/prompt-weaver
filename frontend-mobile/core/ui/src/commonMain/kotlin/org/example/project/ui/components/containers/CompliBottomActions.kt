package org.example.project.ui.components.containers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import org.example.project.ui.components.actions.CompliPrimaryButton
import org.example.project.ui.components.actions.CompliTonalButton
import org.example.project.ui.theme.CompliTheme

/**
 * Нижняя панель основных действий, обычно для `bottomBar` в `Scaffold`.
 * Панель учитывает navigation bar inset, а применение `innerPadding` к содержимому остаётся ответственностью экрана.
 */
@Composable
fun CompliBottomActions(
    primary: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    secondary: (@Composable ColumnScope.() -> Unit)? = null,
) {
    CompliSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = CompliTheme.colors.surface,
        contentColor = CompliTheme.colors.onSurface,
        shadowElevation = CompliTheme.elevations.floating,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(CompliTheme.spacing.large),
        ) {
            primary()
            if (secondary != null) {
                Spacer(Modifier.height(CompliTheme.spacing.small))
                secondary()
            }
        }
    }
}
