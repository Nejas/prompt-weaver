package org.example.project.ui.components.interaction

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role

@Composable
internal fun rememberCompliClick(onClick: () -> Unit): () -> Unit {
    val feedback = LocalCompliInteractionFeedback.current
    val currentOnClick by rememberUpdatedState(onClick)

    return remember(feedback) {
        {
            feedback.perform()
            currentOnClick()
        }
    }
}

fun Modifier.compliClickable(
    enabled: Boolean = true,
    role: Role? = null,
    onClickLabel: String? = null,
    interactionSource: MutableInteractionSource? = null,
    showIndication: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val handledClick = rememberCompliClick(onClick)

    clickable(
        enabled = enabled,
        role = role,
        onClickLabel = onClickLabel,
        interactionSource = source,
        indication = if (showIndication) LocalIndication.current else null,
        onClick = handledClick,
    )
}
