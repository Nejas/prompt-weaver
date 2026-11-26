package org.example.project.chat.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.actions.CompliButton
import org.example.project.ui.components.actions.CompliButtonDefaults
import org.example.project.ui.components.actions.CompliIconButton
import org.example.project.ui.theme.CompliTheme

@Composable
internal fun ChatTemplatesMenu(state: ChatState, onIntent: (ChatIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LazyRow(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = CompliTheme.spacing.large),
                horizontalArrangement = Arrangement.spacedBy(CompliTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.templateCategories.isEmpty()) {
                    item {
                        Text(
                            if (state.templatesLoading) "Загружаем категории…" else "Категории шаблонов",
                            style = CompliTheme.typography.labelMedium,
                            color = CompliTheme.colors.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                items(state.templateCategories, key = { it.name }) { category ->
                    TemplateChoiceChip(
                        label = category.name.ifBlank { "Без категории" },
                        selected = state.selectedCategory == category.name,
                        onClick = { onIntent(ChatIntent.SelectCategory(category.name)) },
                    )
                }
            }
            CompliIconButton(
                onClick = { onIntent(ChatIntent.ShowTemplateForm) },
                contentDescription = "Создать шаблон или категорию",
                modifier = Modifier.padding(end = CompliTheme.spacing.small),
                colors = CompliButtonDefaults.textColors(),
                size = 48.dp,
            ) { Text("+", style = CompliTheme.typography.titleLarge) }
        }
        state.templatesError?.let { error ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = CompliTheme.spacing.large),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(error, Modifier.weight(1f), style = CompliTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                CompliIconButton(
                    onClick = { onIntent(ChatIntent.RetryTemplates) },
                    contentDescription = "Повторить загрузку шаблонов",
                    colors = CompliButtonDefaults.textColors(), size = 48.dp,
                ) { Text("↻") }
            }
        }
        state.selectedCategory?.let { categoryName ->
            val templates = state.templateCategories.firstOrNull { it.name == categoryName }?.templates.orEmpty()
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                key(categoryName) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = CompliTheme.spacing.large),
                        horizontalArrangement = Arrangement.spacedBy(CompliTheme.spacing.small),
                    ) {
                        if (templates.isEmpty()) {
                            item {
                                Box(Modifier.heightIn(min = 48.dp), contentAlignment = Alignment.CenterStart) {
                                    Text("В категории пока нет шаблонов", style = CompliTheme.typography.bodySmall, maxLines = 1)
                                }
                            }
                        }
                        items(templates, key = { "${it.isLocal}-${it.id}" }) { template ->
                            TemplateChoiceChip(
                                label = template.title,
                                selected = state.selectedTemplate?.id == template.id,
                                onClick = { onIntent(ChatIntent.SelectTemplate(template.id)) },
                            )
                        }
                    }
                }
                if (state.selectedTemplate?.isLocal == false) {
                    CompliIconButton(
                        onClick = { onIntent(ChatIntent.UseSelectedTemplate) },
                        contentDescription = "Открыть выбранный шаблон",
                        modifier = Modifier.padding(end = CompliTheme.spacing.small),
                        colors = CompliButtonDefaults.primaryColors(), size = 48.dp,
                    ) { Text("→") }
                }
            }
        }
    }
}

@Composable
internal fun TemplateChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    // Leave room for the expanded touch target while keeping the visible chip small.
    Box(Modifier.heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
        CompliButton(
            onClick = onClick,
            modifier = Modifier.widthIn(max = 180.dp).semantics { this.selected = selected },
            enabled = enabled,
            colors = if (selected) CompliButtonDefaults.primaryColors() else CompliButtonDefaults.tonalColors(),
            minHeight = 32.dp,
            contentPadding = PaddingValues(horizontal = CompliTheme.spacing.medium, vertical = CompliTheme.spacing.extraSmall),
        ) {
            Text(label, style = CompliTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
