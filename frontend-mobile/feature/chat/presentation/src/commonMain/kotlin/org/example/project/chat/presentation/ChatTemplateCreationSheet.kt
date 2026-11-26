package org.example.project.chat.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import org.example.project.ui.components.actions.CompliPrimaryButton
import org.example.project.ui.components.actions.CompliTextButton
import org.example.project.ui.components.containers.CompliBottomSheet
import org.example.project.ui.components.inputs.CompliTextField
import org.example.project.ui.theme.CompliTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatTemplateCreationSheet(state: ChatState, onIntent: (ChatIntent) -> Unit) {
    val saving = state.templateSaving || state.categorySaving
    val currentSaving by rememberUpdatedState(saving)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !currentSaving },
    )
    CompliBottomSheet(
        onDismissRequest = { onIntent(ChatIntent.HideTemplateForm) },
        sheetState = sheetState,
        title = { Text("Новый шаблон") },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.medium),
        ) {
            CompliTextField(
                state.templateTitleDraft, { onIntent(ChatIntent.ChangeTemplateTitle(it)) },
                Modifier.fillMaxWidth(), label = "Название", singleLine = true, enabled = !saving,
            )
            CompliTextField(
                state.templateDescriptionDraft, { onIntent(ChatIntent.ChangeTemplateDescription(it)) },
                Modifier.fillMaxWidth(), label = "Описание", enabled = !saving,
            )
            Text("Категория", style = CompliTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(CompliTheme.spacing.small)) {
                items(state.templateCategories, key = { "category-${it.name}" }) { category ->
                    TemplateChoiceChip(
                        label = category.name.ifBlank { "Без категории" },
                        selected = !state.creatingCategory && state.templateCategoryDraft == category.name,
                        onClick = { onIntent(ChatIntent.ChangeTemplateCategory(category.name)) },
                        enabled = !saving,
                    )
                }
                item(key = "new-category-action") {
                    TemplateChoiceChip(
                        label = "+ Новая категория",
                        selected = state.creatingCategory,
                        onClick = { onIntent(ChatIntent.SetCreatingCategory(true)) },
                        enabled = !saving,
                    )
                }
            }
            if (state.creatingCategory) {
                CompliTextField(
                    state.categoryDraft, { onIntent(ChatIntent.ChangeCategoryDraft(it)) },
                    Modifier.fillMaxWidth(), label = "Название категории", singleLine = true, enabled = !saving,
                    supportingText = "Будет создана при сохранении шаблона",
                )
                CompliTextButton(
                    "Создать только категорию", { onIntent(ChatIntent.CreateCategory) },
                    enabled = !saving, loading = state.categorySaving,
                )
            }
            state.templateFormError?.let {
                Text(it, style = CompliTheme.typography.bodySmall, color = CompliTheme.colors.rejected)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(CompliTheme.spacing.small)) {
                CompliTextButton(
                    "Отмена", { onIntent(ChatIntent.HideTemplateForm) },
                    modifier = Modifier.weight(1f), enabled = !saving,
                )
                CompliPrimaryButton(
                    "Сохранить", { onIntent(ChatIntent.CreateTemplate) },
                    modifier = Modifier.weight(1f), enabled = !saving, loading = state.templateSaving,
                )
            }
        }
    }
}
