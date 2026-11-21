package org.example.project.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

private val LightColorScheme = createColorScheme(LightCompliColors, false)
private val DarkColorScheme = createColorScheme(DarkCompliColors, true)

private fun createColorScheme(colors: CompliColors, darkTheme: Boolean): ColorScheme {
    val base = if (darkTheme) darkColorScheme() else lightColorScheme()

    return base.copy(
        primary = colors.primary,
        onPrimary = colors.onPrimary,
        primaryContainer = colors.primaryContainer,
        onPrimaryContainer = colors.onPrimaryContainer,
        inversePrimary = colors.primary,
        secondary = colors.primary,
        onSecondary = colors.onPrimary,
        secondaryContainer = colors.primaryContainer,
        onSecondaryContainer = colors.onPrimaryContainer,
        tertiary = colors.primary,
        onTertiary = colors.onPrimary,
        tertiaryContainer = colors.primaryContainer,
        onTertiaryContainer = colors.onPrimaryContainer,
        background = colors.background,
        onBackground = colors.onSurface,
        surface = colors.surface,
        onSurface = colors.onSurface,
        surfaceVariant = colors.surfaceContainer,
        onSurfaceVariant = colors.onSurfaceVariant,
        surfaceTint = colors.primary,
        inverseSurface = colors.onSurface,
        inverseOnSurface = colors.background,
        error = colors.rejected,
        onError = colors.onRejected,
        errorContainer = colors.surfaceContainerHigh,
        onErrorContainer = colors.rejected,
        outline = colors.outline,
        outlineVariant = colors.outlineVariant,
        scrim = Color.Black,
        surfaceBright = if (darkTheme) colors.surfaceContainerHighest else colors.background,
        surfaceContainer = colors.surfaceContainer,
        surfaceContainerHigh = colors.surfaceContainerHigh,
        surfaceContainerHighest = colors.surfaceContainerHighest,
        surfaceContainerLow = colors.surface,
        surfaceContainerLowest = colors.background,
        surfaceDim = if (darkTheme) colors.background else colors.surfaceContainerHigh,
        primaryFixed = colors.primary,
        primaryFixedDim = colors.primaryPressed,
        onPrimaryFixed = colors.onPrimary,
        onPrimaryFixedVariant = colors.onPrimary,
        secondaryFixed = colors.primary,
        secondaryFixedDim = colors.primaryPressed,
        onSecondaryFixed = colors.onPrimary,
        onSecondaryFixedVariant = colors.onPrimary,
        tertiaryFixed = colors.primary,
        tertiaryFixedDim = colors.primaryPressed,
        onTertiaryFixed = colors.onPrimary,
        onTertiaryFixedVariant = colors.onPrimary,
    )
}

private const val THEME_CHANGE_DURATION_MILLIS = 250

internal val LocalCompliDarkProgress = compositionLocalOf { 0f }

@Composable
private fun animateThemeProgress(darkTheme: Boolean): Float {
    val progress by animateFloatAsState(
        targetValue = if (darkTheme) 1f else 0f,
        animationSpec = tween(durationMillis = THEME_CHANGE_DURATION_MILLIS),
        label = "themeProgress",
    )
    return progress
}

private fun blend(light: CompliColors, dark: CompliColors, fraction: Float): CompliColors =
    CompliColors(
        primary = lerp(light.primary, dark.primary, fraction),
        primaryPressed = lerp(light.primaryPressed, dark.primaryPressed, fraction),
        onPrimary = lerp(light.onPrimary, dark.onPrimary, fraction),
        primaryContainer = lerp(light.primaryContainer, dark.primaryContainer, fraction),
        onPrimaryContainer = lerp(light.onPrimaryContainer, dark.onPrimaryContainer, fraction),
        yandex = lerp(light.yandex, dark.yandex, fraction),
        onYandex = lerp(light.onYandex, dark.onYandex, fraction),
        background = lerp(light.background, dark.background, fraction),
        surface = lerp(light.surface, dark.surface, fraction),
        surfaceContainer = lerp(light.surfaceContainer, dark.surfaceContainer, fraction),
        surfaceContainerHigh = lerp(light.surfaceContainerHigh, dark.surfaceContainerHigh, fraction),
        surfaceContainerHighest =
            lerp(light.surfaceContainerHighest, dark.surfaceContainerHighest, fraction),
        shimmerHighlight = lerp(light.shimmerHighlight, dark.shimmerHighlight, fraction),
        outline = lerp(light.outline, dark.outline, fraction),
        outlineVariant = lerp(light.outlineVariant, dark.outlineVariant, fraction),
        onSurface = lerp(light.onSurface, dark.onSurface, fraction),
        onSurfaceVariant = lerp(light.onSurfaceVariant, dark.onSurfaceVariant, fraction),
        onSurfaceDisabled = lerp(light.onSurfaceDisabled, dark.onSurfaceDisabled, fraction),
        offline = lerp(light.offline, dark.offline, fraction),
        pending = lerp(light.pending, dark.pending, fraction),
        revision = lerp(light.revision, dark.revision, fraction),
        approved = lerp(light.approved, dark.approved, fraction),
        rejected = lerp(light.rejected, dark.rejected, fraction),
        onRejected = lerp(light.onRejected, dark.onRejected, fraction),
        info = lerp(light.info, dark.info, fraction),
        chart1 = lerp(light.chart1, dark.chart1, fraction),
        chart2 = lerp(light.chart2, dark.chart2, fraction),
        chart3 = lerp(light.chart3, dark.chart3, fraction),
        chart4 = lerp(light.chart4, dark.chart4, fraction),
        chart5 = lerp(light.chart5, dark.chart5, fraction),
        chart6 = lerp(light.chart6, dark.chart6, fraction),
        chart7 = lerp(light.chart7, dark.chart7, fraction),
    )

@Composable
fun CompliTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val progress = animateThemeProgress(darkTheme)
    val typography = defaultCompliTypography()

    CompositionLocalProvider(
        LocalCompliColors provides blend(LightCompliColors, DarkCompliColors, progress),
        LocalCompliDarkProgress provides progress,
        LocalCompliSpacing provides DefaultCompliSpacing,
        LocalCompliIconSizes provides DefaultCompliIconSizes,
        LocalCompliRadii provides DefaultCompliRadii,
        LocalCompliElevations provides DefaultCompliElevations,
        LocalCompliTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = typography.material,
            shapes = CompliShapes,
            content = content,
        )
    }
}

object CompliTheme {
    val colors: CompliColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCompliColors.current

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalCompliDarkProgress.current >= 0.5f

    val spacing: CompliSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalCompliSpacing.current

    val iconSizes: CompliIconSizes
        @Composable
        @ReadOnlyComposable
        get() = LocalCompliIconSizes.current

    val radii: CompliRadii
        @Composable
        @ReadOnlyComposable
        get() = LocalCompliRadii.current

    val elevations: CompliElevations
        @Composable
        @ReadOnlyComposable
        get() = LocalCompliElevations.current

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val typography: CompliTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalCompliTypography.current
}
