package org.example.project.ui.components.inputs

import org.example.project.ui.generated.resources.Res
import org.example.project.ui.generated.resources.ic_check
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.interaction.compliClickable
import org.example.project.ui.theme.CompliTheme

@Immutable
data class CompliSelectionControlColors(
    val selectedContainerColor: Color,
    val selectedContentColor: Color,
    val unselectedBorderColor: Color,
    val disabledSelectedContainerColor: Color,
    val disabledSelectedContentColor: Color,
    val disabledUnselectedBorderColor: Color,
)

object CompliSelectionControlDefaults {
    val VisualSize = 24.dp
    val TouchTargetSize = 48.dp

    @Composable
    fun colors() = CompliSelectionControlColors(
        selectedContainerColor = CompliTheme.colors.primary,
        selectedContentColor = CompliTheme.colors.onPrimary,
        unselectedBorderColor = CompliTheme.colors.outline,
        disabledSelectedContainerColor = CompliTheme.colors.onSurfaceDisabled,
        disabledSelectedContentColor = CompliTheme.colors.surface,
        disabledUnselectedBorderColor = CompliTheme.colors.outlineVariant,
    )
}

/**
 * Радио-кнопка для управляемого состояния [selected].
 * Взаимоисключение обеспечивает вызывающий код; `null` допустим только при обработке клика родителем.
 */
@Composable
fun CompliRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CompliSelectionControlColors = CompliSelectionControlDefaults.colors(),
) {
    CompliSelectionControl(
        selected = selected,
        onClick = onClick,
        modifier = modifier.semantics { this.selected = selected },
        enabled = enabled,
        role = Role.RadioButton,
        colors = colors,
    )
}

/**
 * Флажок с независимым управляемым состоянием [checked].
 * Передавайте `null` в [onCheckedChange] только когда взаимодействием владеет родитель.
 */
@Composable
fun CompliCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CompliSelectionControlColors = CompliSelectionControlDefaults.colors(),
) {
    CompliSelectionControl(
        selected = checked,
        onClick = onCheckedChange?.let { { it(!checked) } },
        modifier = modifier.semantics {
            toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
        },
        enabled = enabled,
        role = Role.Checkbox,
        colors = colors,
    )
}

@Composable
private fun CompliSelectionControl(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier,
    enabled: Boolean,
    role: Role,
    colors: CompliSelectionControlColors,
) {
    val containerColor = when {
        !selected -> Color.Transparent
        enabled -> colors.selectedContainerColor
        else -> colors.disabledSelectedContainerColor
    }
    val contentColor = if (enabled) {
        colors.selectedContentColor
    } else {
        colors.disabledSelectedContentColor
    }
    val borderColor = if (enabled) {
        colors.unselectedBorderColor
    } else {
        colors.disabledUnselectedBorderColor
    }
    val controlModifier = if (onClick == null) {
        modifier.size(CompliSelectionControlDefaults.VisualSize)
    } else {
        modifier
            .size(CompliSelectionControlDefaults.TouchTargetSize)
            .clip(CircleShape)
            .compliClickable(
                enabled = enabled,
                role = role,
                onClick = onClick,
            )
    }

    Box(
        modifier = controlModifier,
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .size(CompliSelectionControlDefaults.VisualSize)
                .then(
                    if (selected) Modifier else Modifier.border(
                        border = BorderStroke(1.dp, borderColor),
                        shape = CircleShape,
                    ),
                ),
            shape = CircleShape,
            color = containerColor,
            contentColor = contentColor,
        ) {
            if (selected) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier
                            .size(14.dp)
                            .graphicsLayer { translationX = -1f },
                    )
                }
            }
        }
    }
}
