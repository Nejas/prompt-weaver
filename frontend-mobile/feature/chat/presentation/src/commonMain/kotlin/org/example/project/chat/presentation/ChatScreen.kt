package org.example.project.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.project.chat.presentation.generated.resources.Res
import org.example.project.chat.presentation.generated.resources.*
import org.example.project.ui.components.actions.CompliButtonDefaults
import org.example.project.ui.components.actions.CompliIconButton
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
    Box(Modifier.fillMaxSize().background(CompliTheme.colors.background)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                CompliIconButton({ onIntent(ChatIntent.OpenCatalog) }, "Каталог шаблонов", colors = CompliButtonDefaults.textColors(), size = 48.dp) { Text("◷", color = CompliTheme.colors.onSurfaceVariant) }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(modelResource(state.model)), style = CompliTheme.typography.titleMedium)
                    Text("⌄", color = CompliTheme.colors.onSurfaceVariant)
                }
                CompliIconButton({ onIntent(ChatIntent.OpenSettings) }, stringResource(Res.string.chat_settings), colors = CompliButtonDefaults.textColors(), size = 40.dp) { Text("⚙", color = CompliTheme.colors.onSurfaceVariant) }
                CompliIconButton({ onIntent(ChatIntent.OpenProfile) }, stringResource(Res.string.chat_profile), colors = CompliButtonDefaults.textColors(), size = 48.dp) { Text("♙", color = CompliTheme.colors.onSurfaceVariant) }
            }
            Conversation(state, Modifier.weight(1f))
            Composer(state.draft, onIntent)
        }
    }
}

@Composable
private fun Conversation(state: ChatState, modifier: Modifier) {
    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(stringResource(Res.string.chat_topic), style = CompliTheme.typography.labelMedium, color = CompliTheme.colors.onSurfaceVariant)
        state.messages.forEach { message ->
            when (message) {
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
private fun Composer(draft: String, onIntent: (ChatIntent) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CompliSurface(Modifier.fillMaxWidth().height(88.dp), RoundedCornerShape(20.dp), CompliTheme.colors.surfaceContainer) {
            Column(Modifier.padding(start = 12.dp, top = 14.dp, end = 8.dp, bottom = 8.dp)) {
                BasicTextField(draft, { onIntent(ChatIntent.ChangeDraft(it)) }, Modifier.fillMaxWidth().weight(1f), textStyle = CompliTheme.typography.bodyMedium.copy(color = CompliTheme.colors.onSurface), decorationBox = { inner -> if (draft.isEmpty()) Text(stringResource(Res.string.chat_placeholder), color = CompliTheme.colors.onSurfaceVariant); inner() })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    ChatIconButton("+", stringResource(Res.string.chat_attach), 36.dp)
                    CompliIconButton({ onIntent(ChatIntent.Send) }, stringResource(Res.string.chat_send), colors = CompliButtonDefaults.primaryColors(), shape = RoundedCornerShape(12.dp), size = 36.dp) { Text("↑", color = CompliTheme.colors.onPrimary) }
                }
            }
        }
        Text(stringResource(Res.string.chat_disclaimer), Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = CompliTheme.typography.bodySmall, color = CompliTheme.colors.onSurfaceVariant)
    }
}

private fun modelResource(model: ChatModel): StringResource = when (model) {
    ChatModel.GeminiFlash -> Res.string.chat_model_name
}

@Composable private fun ChatIconButton(symbol: String, description: String, size: Dp = 48.dp) = CompliIconButton(onClick = {}, contentDescription = description, enabled = false, colors = CompliButtonDefaults.textColors(), size = size) { Text(symbol, color = CompliTheme.colors.onSurfaceVariant) }
