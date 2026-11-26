package org.example.project.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.project.chat.presentation.generated.resources.Res
import org.example.project.chat.presentation.generated.resources.*
import org.example.project.ui.components.actions.CompliButtonDefaults
import org.example.project.ui.components.actions.CompliIconButton
import org.example.project.ui.components.containers.CompliConfirmationDialog
import org.example.project.ui.components.containers.CompliSurface
import org.example.project.ui.theme.CompliTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatScreen(component: ChatComponent) {
    val state by component.state.collectAsState()
    ChatScreenContent(state, component::onIntent)
}

@Composable
fun ChatScreenContent(state: ChatState, onIntent: (ChatIntent) -> Unit) {
    BoxWithConstraints(
        Modifier.fillMaxSize().background(CompliTheme.colors.background)
            .safeDrawingPadding().imePadding(),
    ) {
        val compactHeight = maxHeight < 400.dp
        // Keep the editor in the same composition when the IME changes the window height.
        val singleRowChrome = maxWidth >= 600.dp
        Column(Modifier.fillMaxSize()) {
            if (singleRowChrome) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ChatHeader(state.model, onIntent, Modifier.weight(0.4f))
                    Composer(state.draft, onIntent, compactHeight = true, modifier = Modifier.weight(0.6f))
                }
            } else {
                ChatHeader(state.model, onIntent)
            }
            ChatBody(state, onIntent, Modifier.weight(1f), compactHeight)
            if (!singleRowChrome) Composer(state.draft, onIntent, compactHeight)
        }
        if (state.pendingPrompt != null) {
            CompliConfirmationDialog(
                title = "Заменить черновик?",
                body = "В поле сообщения уже есть текст. Заменить его собранным промптом?",
                confirmLabel = "Заменить",
                dismissLabel = "Отмена",
                onConfirm = { onIntent(ChatIntent.ConfirmDraftReplacement) },
                onDismissRequest = { onIntent(ChatIntent.CancelDraftReplacement) },
            )
        }
    }
    if (state.templateFormVisible) ChatTemplateCreationSheet(state, onIntent)
}

@Composable
private fun ChatHeader(model: ChatModel, onIntent: (ChatIntent) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = CompliTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(modelResource(model)),
            modifier = Modifier.weight(1f).padding(horizontal = CompliTheme.spacing.small),
            style = CompliTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        CompliIconButton(
            { onIntent(ChatIntent.OpenSettings) }, stringResource(Res.string.chat_settings),
            colors = CompliButtonDefaults.textColors(), size = 48.dp,
        ) { Text("⚙", color = CompliTheme.colors.onSurfaceVariant) }
        CompliIconButton(
            { onIntent(ChatIntent.OpenProfile) }, stringResource(Res.string.chat_profile),
            colors = CompliButtonDefaults.textColors(), size = 48.dp,
        ) { Text("♙", color = CompliTheme.colors.onSurfaceVariant) }
    }
}

@Composable
private fun ChatBody(state: ChatState, onIntent: (ChatIntent) -> Unit, modifier: Modifier, compactHeight: Boolean) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val menuMaxHeight = maxHeight * 0.4f
        Column(Modifier.fillMaxSize()) {
            ChatTemplatesMenu(
                state, onIntent,
                // Only the menu can scroll vertically if the keyboard leaves too little height.
                // Keep most of the available space for the independently scrolling conversation.
                Modifier.heightIn(max = menuMaxHeight).verticalScroll(rememberScrollState()),
            )
            ChatFeed(state, Modifier.weight(1f), compactHeight)
        }
    }
}

@Composable
private fun ChatFeed(state: ChatState, modifier: Modifier, compactHeight: Boolean) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = CompliTheme.spacing.large,
            vertical = if (compactHeight) CompliTheme.spacing.extraSmall else CompliTheme.spacing.large,
        ),
        verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.medium),
    ) {
        item(key = "topic") {
            Text(stringResource(Res.string.chat_topic), style = CompliTheme.typography.labelMedium, color = CompliTheme.colors.onSurfaceVariant)
        }
        items(state.messages.size, key = { "message-$it" }) { index ->
            when (val message = state.messages[index]) {
                ChatMessage.DemoUser -> UserBubble(stringResource(Res.string.chat_demo_user))
                is ChatMessage.User -> UserBubble(message.text)
                ChatMessage.DemoAssistant -> AssistantDemo(state.model)
            }
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        CompliSurface(shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp), color = CompliTheme.colors.primaryContainer) {
            Text(text, Modifier.padding(14.dp), style = CompliTheme.typography.bodyMedium, color = CompliTheme.colors.onPrimaryContainer)
        }
    }
}

@Composable
private fun AssistantDemo(model: ChatModel) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text("✦", color = CompliTheme.colors.primary); Spacer(Modifier.size(8.dp)); Text(stringResource(modelResource(model)), style = CompliTheme.typography.titleSmall) }
        Text(stringResource(Res.string.chat_demo_intro), style = CompliTheme.typography.bodyMedium)
        PlanItem(Res.string.chat_step_words_title, Res.string.chat_step_words_body)
        PlanItem(Res.string.chat_step_practice_title, Res.string.chat_step_practice_body)
        PlanItem(Res.string.chat_step_talk_title, Res.string.chat_step_talk_body)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ChatIconButton("⧉", stringResource(Res.string.chat_copy), 36.dp)
            ChatIconButton("↻", stringResource(Res.string.chat_retry), 36.dp)
            ChatIconButton("♡", stringResource(Res.string.chat_like), 36.dp)
        }
    }
}

@Composable private fun PlanItem(title: StringResource, body: StringResource) { Column { Text(stringResource(title), style = CompliTheme.typography.titleSmall); Text(stringResource(body), style = CompliTheme.typography.bodyMedium) } }

@Composable
private fun Composer(
    draft: String,
    onIntent: (ChatIntent) -> Unit,
    compactHeight: Boolean,
    modifier: Modifier = Modifier,
) {
    val verticalPadding = if (compactHeight) CompliTheme.spacing.extraSmall else CompliTheme.spacing.small
    Column(
        modifier.fillMaxWidth().padding(horizontal = CompliTheme.spacing.large, vertical = verticalPadding),
        verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.small),
    ) {
        CompliSurface(Modifier.fillMaxWidth(), RoundedCornerShape(CompliTheme.radii.card), CompliTheme.colors.surfaceContainer) {
            Row(
                Modifier.padding(verticalPadding),
                horizontalArrangement = Arrangement.spacedBy(CompliTheme.spacing.small),
                verticalAlignment = Alignment.Bottom,
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = { onIntent(ChatIntent.ChangeDraft(it)) },
                    modifier = Modifier.weight(1f).padding(CompliTheme.spacing.small),
                    minLines = 1,
                    maxLines = if (compactHeight) 1 else 4,
                    textStyle = CompliTheme.typography.bodyMedium.copy(color = CompliTheme.colors.onSurface),
                    decorationBox = { inner ->
                        if (draft.isEmpty()) Text(
                            stringResource(Res.string.chat_placeholder),
                            style = CompliTheme.typography.bodyMedium,
                            color = CompliTheme.colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        inner()
                    },
                )
                CompliIconButton(
                    { onIntent(ChatIntent.Send) }, stringResource(Res.string.chat_send),
                    colors = CompliButtonDefaults.primaryColors(), shape = RoundedCornerShape(CompliTheme.radii.small), size = 48.dp,
                ) { Text("↑", color = CompliTheme.colors.onPrimary) }
            }
        }
        if (!compactHeight) {
            Text(stringResource(Res.string.chat_disclaimer), Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = CompliTheme.typography.bodySmall, color = CompliTheme.colors.onSurfaceVariant)
        }
    }
}

private fun modelResource(model: ChatModel): StringResource = when (model) {
    ChatModel.GeminiFlash -> Res.string.chat_model_name
}

@Composable private fun ChatIconButton(symbol: String, description: String, size: Dp = 48.dp) = CompliIconButton(onClick = {}, contentDescription = description, enabled = false, colors = CompliButtonDefaults.textColors(), size = size) { Text(symbol, color = CompliTheme.colors.onSurfaceVariant) }
