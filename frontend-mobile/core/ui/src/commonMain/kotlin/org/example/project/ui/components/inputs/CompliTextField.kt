package org.example.project.ui.components.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.CompliTheme

/**
 * Текстовое поле с управляемым [value] и оформлением Compli.
 * Ненулевой [errorText] задаёт состояние ошибки и показывается вместо [supportingText].
 */
@Composable
fun CompliTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(CompliTheme.radii.medium),
) {
    val colors = CompliTheme.colors
    val supporting = errorText ?: supportingText

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = CompliTheme.typography.bodyLarge,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supporting?.let { { Text(it) } },
        isError = errorText != null,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        shape = shape,
        colors = TextFieldDefaults.colors(
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface,
            disabledTextColor = colors.onSurfaceVariant,
            errorTextColor = colors.onSurface,
            focusedContainerColor = colors.surfaceContainer,
            unfocusedContainerColor = colors.surfaceContainer,
            disabledContainerColor = colors.surfaceContainer,
            errorContainerColor = colors.surfaceContainer,
            cursorColor = colors.primary,
            errorCursorColor = colors.rejected,
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            errorIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            focusedLabelColor = colors.onSurfaceVariant,
            unfocusedLabelColor = colors.onSurfaceVariant,
            disabledLabelColor = colors.onSurfaceDisabled,
            errorLabelColor = colors.rejected,
            focusedPlaceholderColor = colors.onSurfaceVariant,
            unfocusedPlaceholderColor = colors.onSurfaceVariant,
            disabledPlaceholderColor = colors.onSurfaceVariant,
            errorPlaceholderColor = colors.onSurfaceVariant,
            focusedSupportingTextColor = colors.onSurfaceVariant,
            unfocusedSupportingTextColor = colors.onSurfaceVariant,
            disabledSupportingTextColor = colors.onSurfaceDisabled,
            errorSupportingTextColor = colors.rejected,
            focusedLeadingIconColor = colors.onSurfaceVariant,
            unfocusedLeadingIconColor = colors.onSurfaceVariant,
            disabledLeadingIconColor = colors.onSurfaceVariant,
            errorLeadingIconColor = colors.rejected,
            focusedTrailingIconColor = colors.onSurfaceVariant,
            unfocusedTrailingIconColor = colors.onSurfaceVariant,
            disabledTrailingIconColor = colors.onSurfaceVariant,
            errorTrailingIconColor = colors.rejected,
        ),
    )
}
