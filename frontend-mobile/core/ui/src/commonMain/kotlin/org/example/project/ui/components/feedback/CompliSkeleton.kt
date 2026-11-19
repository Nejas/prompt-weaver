package org.example.project.ui.components.feedback

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.CompliTheme

@Immutable
data class CompliSkeletonColors(
    val baseColor: Color,
    val highlightColor: Color,
)

object CompliSkeletonDefaults {
    const val AnimationDurationMillis = 1_200

    @Composable
    fun shape(): Shape = RoundedCornerShape(CompliTheme.radii.small)

    @Composable
    fun colors() = CompliSkeletonColors(
        baseColor = CompliTheme.colors.surfaceContainerHigh,
        highlightColor = CompliTheme.colors.shimmerHighlight,
    )
}

/**
 * Заполнитель загружаемого элемента. Размер обязательно задаётся через [modifier] по размеру
 * будущего контента; [animated] включает циклический shimmer. Компонент исключён из семантики,
 * поэтому состояние загрузки следует озвучивать на родительском контейнере.
 */
@Composable
fun CompliSkeleton(
    modifier: Modifier,
    shape: Shape = CompliSkeletonDefaults.shape(),
    animated: Boolean = true,
    colors: CompliSkeletonColors = CompliSkeletonDefaults.colors(),
) {
    val progress = if (animated) {
        val transition = rememberInfiniteTransition(label = "CompliSkeleton")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = CompliSkeletonDefaults.AnimationDurationMillis,
                    easing = LinearEasing,
                ),
            ),
            label = "CompliSkeletonShimmer",
        )
    } else {
        null
    }

    Box(
        modifier = modifier
            .clearAndSetSemantics {}
            .clip(shape)
            .drawWithCache {
                val gradientColors = listOf(
                    colors.baseColor,
                    colors.highlightColor,
                    colors.baseColor,
                )
                val shimmerWidth = maxOf(size.width * 0.65f, size.height * 2f)

                onDrawBehind {
                    val brush = if (progress == null || size.width <= 0f || size.height <= 0f) {
                        SolidColor(colors.baseColor)
                    } else {
                        val shimmerEnd = -shimmerWidth +
                            (size.width + shimmerWidth * 2f) * progress.value
                        Brush.linearGradient(
                            colors = gradientColors,
                            start = Offset(shimmerEnd - shimmerWidth, 0f),
                            end = Offset(shimmerEnd, size.height),
                        )
                    }
                    drawRect(brush)
                }
            },
    )
}

/**
 * Готовый скелетон строки с ведущим кругом, двумя строками и хвостовым блоком.
 * Высота зависит от стандартных внутренних отступов, ширину задаёт [modifier]; анимация
 * управляется через [animated], а сам заполнитель не передаёт данные accessibility.
 */
@Composable
fun CompliSkeletonRow(
    modifier: Modifier = Modifier,
    showLeading: Boolean = true,
    animated: Boolean = true,
) {
    Row(
        modifier = modifier.padding(CompliTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(CompliTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showLeading) {
            CompliSkeleton(
                modifier = Modifier.size(CompliTheme.iconSizes.large),
                shape = CircleShape,
                animated = animated,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.small),
        ) {
            CompliSkeleton(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(12.dp),
                animated = animated,
            )
            CompliSkeleton(
                modifier = Modifier
                    .fillMaxWidth(0.46f)
                    .height(8.dp),
                animated = animated,
            )
        }
        CompliSkeleton(
            modifier = Modifier
                .width(48.dp)
                .height(12.dp),
            animated = animated,
        )
    }
}
