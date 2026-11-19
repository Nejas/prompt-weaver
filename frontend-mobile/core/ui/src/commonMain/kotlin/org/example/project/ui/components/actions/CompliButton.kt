package org.example.project.ui.components.actions

import org.example.project.ui.generated.resources.Res
import org.example.project.ui.generated.resources.ic_yandex
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.containers.CompliClickableSurface
import org.example.project.ui.theme.CompliTheme

@Immutable
data class CompliButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
)

object CompliButtonDefaults {
    val MinHeight = 56.dp
    val ContentPadding = PaddingValues(horizontal = 20.dp)
    val ContentSpacing = 10.dp

    @Composable
    fun shape(): Shape = RoundedCornerShape(CompliTheme.radii.large)

    @Composable
    fun primaryColors() = CompliButtonColors(
        containerColor = CompliTheme.colors.primary,
        contentColor = CompliTheme.colors.onPrimary,
        disabledContainerColor = CompliTheme.colors.surfaceContainerHigh,
        disabledContentColor = CompliTheme.colors.onSurfaceDisabled,
    )

    @Composable
    fun tonalColors() = CompliButtonColors(
        containerColor = CompliTheme.colors.surfaceContainerHigh,
        contentColor = CompliTheme.colors.onSurface,
        disabledContainerColor = CompliTheme.colors.surfaceContainer,
        disabledContentColor = CompliTheme.colors.onSurfaceDisabled,
    )

    @Composable
    fun textColors() = CompliButtonColors(
        containerColor = Color.Transparent,
        contentColor = CompliTheme.colors.onSurface,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = CompliTheme.colors.onSurfaceDisabled,
    )

    /** Текстовая кнопка фирменным жёлтым: второстепенное действие, которое всё же нужно заметить. */
    @Composable
    fun accentTextColors() = CompliButtonColors(
        containerColor = Color.Transparent,
        contentColor = CompliTheme.colors.primary,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = CompliTheme.colors.onSurfaceDisabled,
    )

    @Composable
    fun yandexColors(style: CompliYandexButtonStyle) = when (style) {
        CompliYandexButtonStyle.Red -> CompliButtonColors(
            containerColor = CompliTheme.colors.yandex,
            contentColor = CompliTheme.colors.onYandex,
            disabledContainerColor = CompliTheme.colors.surfaceContainerHigh,
            disabledContentColor = CompliTheme.colors.onSurfaceDisabled,
        )
        CompliYandexButtonStyle.Black -> CompliButtonColors(
            containerColor = Color.Black,
            contentColor = Color.White,
            disabledContainerColor = CompliTheme.colors.surfaceContainerHigh,
            disabledContentColor = CompliTheme.colors.onSurfaceDisabled,
        )
        CompliYandexButtonStyle.White -> CompliButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = CompliTheme.colors.surfaceContainerHigh,
            disabledContentColor = CompliTheme.colors.onSurfaceDisabled,
        )
    }
}

/**
 * Базовая кнопка с произвольным содержимым строки и настраиваемым оформлением.
 * При загрузке содержимое скрывается, а нажатия блокируются независимо от [enabled].
 * @param loading показывает индикатор и временно запрещает взаимодействие
 */
@Composable
fun CompliButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    colors: CompliButtonColors = CompliButtonDefaults.primaryColors(),
    shape: Shape = CompliButtonDefaults.shape(),
    contentPadding: PaddingValues = CompliButtonDefaults.ContentPadding,
    minHeight: Dp = CompliButtonDefaults.MinHeight,
    border: BorderStroke? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val containerColor = if (enabled) colors.containerColor else colors.disabledContainerColor
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
    val loadingDescription = "Загрузка"

    CompliClickableSurface(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = minHeight),
        enabled = enabled && !loading,
        role = Role.Button,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
    ) {
        Box(
            modifier = Modifier
                .then(if (loading) Modifier.semantics { stateDescription = loadingDescription } else Modifier)
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.alpha(if (loading) 0f else 1f),
                horizontalArrangement = Arrangement.spacedBy(CompliButtonDefaults.ContentSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leadingContent?.invoke()
                ProvideTextStyle(CompliTheme.typography.labelLarge) {
                    content()
                }
                trailingContent?.invoke()
            }
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = LocalContentColor.current,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

/**
 * Текстовая перегрузка базовой кнопки для стандартной подписи.
 * При загрузке подпись скрывается, а нажатия блокируются независимо от [enabled].
 * @param loading показывает индикатор и временно запрещает взаимодействие
 */
@Composable
fun CompliButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    colors: CompliButtonColors = CompliButtonDefaults.primaryColors(),
    shape: Shape = CompliButtonDefaults.shape(),
    contentPadding: PaddingValues = CompliButtonDefaults.ContentPadding,
    minHeight: Dp = CompliButtonDefaults.MinHeight,
    border: BorderStroke? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    CompliButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        colors = colors,
        shape = shape,
        contentPadding = contentPadding,
        minHeight = minHeight,
        border = border,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
    ) {
        Text(text)
    }
}

/**
 * Основная акцентная кнопка для главного действия на экране.
 * Используйте не более одного такого визуального приоритета в одной группе действий.
 * @param loading показывает индикатор и временно запрещает взаимодействие
 */
@Composable
fun CompliPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) = CompliButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    loading = loading,
    colors = CompliButtonDefaults.primaryColors(),
    leadingContent = leadingContent,
    trailingContent = trailingContent,
)

/**
 * Тональная кнопка для вторичного, но заметного действия.
 * В отличие от основной кнопки использует менее контрастный контейнер темы.
 * @param loading показывает индикатор и временно запрещает взаимодействие
 */
@Composable
fun CompliTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) = CompliButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    loading = loading,
    colors = CompliButtonDefaults.tonalColors(),
    leadingContent = leadingContent,
    trailingContent = trailingContent,
)

/**
 * Кнопка без цветного контейнера для действия низкого приоритета.
 * Не используйте её там, где границы области нажатия должны быть визуально очевидны.
 * @param loading показывает индикатор и временно запрещает взаимодействие
 */
@Composable
fun CompliTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) = CompliButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    loading = loading,
    colors = CompliButtonDefaults.textColors(),
    leadingContent = leadingContent,
    trailingContent = trailingContent,
)

enum class CompliYandexButtonStyle { Red, Black, White }

/**
 * Кнопка для входа или продолжения через Яндекс.
 * Используйте [style] по контрасту с фоном, не заменяя фирменные цвета произвольными.
 * @param loading показывает индикатор и временно запрещает взаимодействие
 */
@Composable
fun CompliYandexButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CompliYandexButtonStyle = CompliYandexButtonStyle.Red,
    enabled: Boolean = true,
    loading: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val colors = CompliButtonDefaults.yandexColors(style)
    val containerColor = if (enabled) colors.containerColor else colors.disabledContainerColor
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor

    CompliButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        colors = colors,
        border = if (style == CompliYandexButtonStyle.White) {
            BorderStroke(1.dp, CompliTheme.colors.outlineVariant)
        } else {
            null
        },
        leadingContent = {
            YandexLogo(
                circleColor = contentColor,
                glyphColor = containerColor,
            )
        },
        trailingContent = trailingContent,
    )
}

@Composable
private fun YandexLogo(
    circleColor: Color,
    glyphColor: Color,
) {
    Box(
        modifier = Modifier
            .size(YandexLogoDefaults.ContainerSize)
            .background(circleColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_yandex),
            contentDescription = null,
            tint = glyphColor,
            modifier = Modifier.size(YandexLogoDefaults.GlyphSize),
        )
    }
}

private object YandexLogoDefaults {
    val ContainerSize = 28.dp
    val GlyphSize = 24.dp
}
