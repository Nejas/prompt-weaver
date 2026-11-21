package org.example.project.ui.components.patterns

import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.containers.CompliSurface
import org.example.project.ui.theme.CompliTheme

enum class CompliHeroIconStyle {
    Primary,
    Error,
}

/** Large semantic icon tile for onboarding, status, and empty-state heroes. */
@Composable
fun CompliHeroIcon(
    icon: DrawableResource,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    style: CompliHeroIconStyle = CompliHeroIconStyle.Primary,
) {
    val containerColor = when (style) {
        CompliHeroIconStyle.Primary -> CompliTheme.colors.primary
        CompliHeroIconStyle.Error ->
            CompliTheme.colors.rejected.copy(alpha = CompliHeroIconDefaults.ErrorContainerAlpha)
    }
    val contentColor = when (style) {
        CompliHeroIconStyle.Primary -> CompliTheme.colors.onPrimary
        CompliHeroIconStyle.Error -> CompliTheme.colors.rejected
    }

    CompliSurface(
        modifier = modifier.size(CompliHeroIconDefaults.ContainerSize),
        shape = CompliTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = contentDescription,
                modifier = Modifier.size(CompliHeroIconDefaults.GlyphSize),
            )
        }
    }
}

private object CompliHeroIconDefaults {
    val ContainerSize = 76.dp
    val GlyphSize = 36.dp
    const val ErrorContainerAlpha = 0.14f
}
