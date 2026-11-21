package org.example.project.ui.components.feedback

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.ui.components.containers.CompliSurface
import org.example.project.ui.theme.CompliTheme

/** Верхний host для коротких сообщений поверх текущего экрана. */
@Composable
fun CompliSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { data ->
        CompliSnackbar(message = data.visuals.message)
    }
}

@Composable
private fun CompliSnackbar(
    message: String,
    modifier: Modifier = Modifier,
) {
    CompliSurface(
        modifier = modifier.fillMaxWidth(),
        color = CompliTheme.colors.surfaceContainerHigh,
        shadowElevation = CompliTheme.elevations.floating,
    ) {
        Row(
            modifier = Modifier.padding(CompliTheme.spacing.large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = CompliTheme.typography.bodyMedium,
            )
        }
    }
}
