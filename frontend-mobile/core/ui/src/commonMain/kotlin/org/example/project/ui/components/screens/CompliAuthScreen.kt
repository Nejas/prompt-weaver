package org.example.project.ui.components.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.interaction.compliClickable
import org.example.project.ui.theme.CompliTheme

/**
 * Общий каркас экранов входа и регистрации. Содержимое [content] располагается
 * между описанием и закреплённым внизу переходом к соседнему auth-сценарию.
 */
@Composable
fun CompliAuthScreen(
    title: String,
    subtitle: String,
    footerText: String,
    footerAction: String,
    onFooterAction: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(64.dp))
            CompliAppMark()
            Spacer(Modifier.height(21.dp))
            Text(title, style = CompliTheme.typography.titleLarge)
            Spacer(Modifier.height(11.dp))
            Text(
                text = subtitle,
                style = CompliTheme.typography.bodyMedium,
                color = CompliTheme.colors.onSurfaceVariant,
            )
            content()
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = footerText,
                style = CompliTheme.typography.labelLarge,
                color = CompliTheme.colors.onSurfaceVariant,
            )
            CompliInlineTextAction(text = footerAction, onClick = onFooterAction)
        }
        Spacer(Modifier.height(36.dp))
    }
}

/** Контейнер визуально связанных полей формы. */
@Composable
fun CompliFormFieldGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CompliTheme.radii.fieldGroup))
            .background(CompliTheme.colors.surfaceContainer),
        content = content,
    )
}

/** Разделитель для полей внутри [CompliFormFieldGroup]. */
@Composable
fun CompliFormFieldDivider(modifier: Modifier = Modifier) {
    Spacer(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CompliTheme.colors.outlineVariant),
    )
}

/** Компактное текстовое действие для встроенных переходов. */
@Composable
fun CompliInlineTextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Text(
        text = text,
        modifier = modifier
            .padding(start = 6.dp)
            .compliClickable(enabled = enabled, role = Role.Button, onClick = onClick),
        style = CompliTheme.typography.labelLarge,
        color = if (enabled) CompliTheme.colors.onSurface else CompliTheme.colors.onSurfaceDisabled,
    )
}

@Composable
private fun CompliAppMark() {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(CompliTheme.colors.primary, RoundedCornerShape(CompliTheme.radii.large)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "✦",
            style = CompliTheme.typography.titleLarge,
            color = CompliTheme.colors.onPrimary,
        )
    }
}
