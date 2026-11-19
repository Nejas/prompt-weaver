package org.example.project.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.interaction.rememberCompliClick
import org.example.project.ui.theme.CompliTheme

/**
 * Нижняя навигация верхнего уровня, обычно для `bottomBar` в `Scaffold`.
 * Компонент учитывает системный navigation bar inset, а экран применяет `innerPadding` к своему содержимому.
 */
@Composable
fun CompliNavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = CompliTheme.colors.surface,
    contentColor: Color = CompliTheme.colors.onSurfaceVariant,
    content: @Composable RowScope.() -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        windowInsets = NavigationBarDefaults.windowInsets,
        content = content,
    )
}

/**
 * Управляемый пункт [CompliNavigationBar]: владелец навигации задаёт [selected] и меняет маршрут в [onClick].
 * Иконка должна иметь понятную подпись [label]; не храните состояние выбора внутри пункта.
 */
@Composable
fun RowScope.CompliNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = CompliTheme.colors
    val handledClick = rememberCompliClick(onClick)

    NavigationBarItem(
        selected = selected,
        onClick = handledClick,
        icon = icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = colors.onPrimaryContainer,
            selectedTextColor = colors.onPrimaryContainer,
            indicatorColor = colors.primaryContainer,
            unselectedIconColor = colors.onSurfaceVariant,
            unselectedTextColor = colors.onSurfaceVariant,
            disabledIconColor = colors.onSurfaceDisabled,
            disabledTextColor = colors.onSurfaceDisabled,
        ),
    )
}
