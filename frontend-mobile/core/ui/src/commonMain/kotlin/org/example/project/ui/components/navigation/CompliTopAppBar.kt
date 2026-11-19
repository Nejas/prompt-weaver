package org.example.project.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.interaction.rememberCompliClick
import org.example.project.ui.theme.CompliTheme

private val TopAppBarContentSpacing = 4.dp

/**
 * Верхняя панель с произвольным заголовком, навигацией и действиями.
 * По умолчанию учитывает status bar inset; иконки действий используйте через доступные кнопки с `contentDescription`.
 *
 * Фон рисуем сами, а панели отдаём прозрачный: Material анимирует свой цвет
 * контейнера пружиной (для затемнения при скролле), и при смене темы панель
 * догоняла бы палитру с задержкой — на границе с контентом был бы виден разрыв.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompliTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    containerColor: Color = CompliTheme.colors.background,
    contentColor: Color = CompliTheme.colors.onSurface,
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (navigationIcon == null) 0.dp else TopAppBarContentSpacing,
                        end = TopAppBarContentSpacing,
                    ),
            ) {
                title()
            }
        },
        modifier = modifier
            .background(containerColor)
            .padding(horizontal = TopAppBarContentSpacing),
        navigationIcon = navigationIcon ?: {},
        actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(TopAppBarContentSpacing),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        },
        windowInsets = windowInsets,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            navigationIconContentColor = contentColor,
            titleContentColor = contentColor,
            actionIconContentColor = contentColor,
        ),
    )
}

/**
 * Круглая кнопка-иконка для navigation и actions слотов [CompliTopAppBar].
 * Использует стандартные размеры Material TopAppBar и прозрачный фон.
 */
@Composable
fun CompliTopAppBarIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val handledClick = rememberCompliClick(onClick)

    IconButton(
        onClick = handledClick,
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent,
            contentColor = CompliTheme.colors.onSurface,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = CompliTheme.colors.onSurfaceDisabled,
        ),
        content = content,
    )
}

/**
 * Верхняя панель для простого строкового заголовка.
 * По умолчанию учитывает status bar inset; иконки навигации и действий должны иметь `contentDescription`.
 */
@Composable
fun CompliTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    containerColor: Color = CompliTheme.colors.background,
    contentColor: Color = CompliTheme.colors.onSurface,
) {
    CompliTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        containerColor = containerColor,
        contentColor = contentColor,
    )
}
