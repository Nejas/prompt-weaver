package org.example.project.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class CompliSpacing(
    val extraSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
    val extraExtraLarge: Dp,
)

@Immutable
data class CompliIconSizes(
    val small: Dp,
    val medium: Dp,
    val large: Dp,
)

@Immutable
data class CompliRadii(
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val card: Dp,
    val dialog: Dp,
    val sheet: Dp,
    val fieldGroup: Dp,
)

@Immutable
data class CompliElevations(
    val floating: Dp,
    val sheet: Dp,
    val dialog: Dp,
)

internal val DefaultCompliSpacing = CompliSpacing(
    extraSmall = 4.dp,
    small = 8.dp,
    medium = 12.dp,
    large = 16.dp,
    extraLarge = 24.dp,
    extraExtraLarge = 32.dp,
)

internal val DefaultCompliIconSizes = CompliIconSizes(
    small = 16.dp,
    medium = 24.dp,
    large = 32.dp,
)

internal val DefaultCompliRadii = CompliRadii(
    small = 12.dp,
    medium = 14.dp,
    large = 16.dp,
    card = 20.dp,
    dialog = 24.dp,
    sheet = 28.dp,
    fieldGroup = 18.dp,
)

internal val DefaultCompliElevations = CompliElevations(
    floating = 8.dp,
    sheet = 12.dp,
    dialog = 16.dp,
)

internal val CompliShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

internal val LocalCompliSpacing = staticCompositionLocalOf { DefaultCompliSpacing }
internal val LocalCompliIconSizes = staticCompositionLocalOf { DefaultCompliIconSizes }
internal val LocalCompliRadii = staticCompositionLocalOf { DefaultCompliRadii }
internal val LocalCompliElevations = staticCompositionLocalOf { DefaultCompliElevations }
