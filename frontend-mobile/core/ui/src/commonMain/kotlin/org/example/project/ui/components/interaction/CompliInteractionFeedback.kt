package org.example.project.ui.components.interaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

fun interface CompliInteractionFeedback {
    fun perform()
}

private val NoOpInteractionFeedback = CompliInteractionFeedback {}

internal val LocalCompliInteractionFeedback = staticCompositionLocalOf {
    NoOpInteractionFeedback
}

/**
 * Предоставляет обратную связь компонентам Compli; размещайте рядом с корнем приложения.
 */
@Composable
fun CompliInteractionFeedbackProvider(
    feedback: CompliInteractionFeedback,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalCompliInteractionFeedback provides feedback,
        content = content,
    )
}
