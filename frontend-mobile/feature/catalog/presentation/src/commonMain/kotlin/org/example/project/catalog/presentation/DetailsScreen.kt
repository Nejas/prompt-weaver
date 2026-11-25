package org.example.project.catalog.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.example.project.catalog.domain.ParameterValidationError
import org.example.project.ui.components.actions.CompliPrimaryButton
import org.example.project.ui.components.actions.CompliTextButton
import org.example.project.ui.components.feedback.CompliContentState
import org.example.project.ui.components.feedback.CompliContentStateType
import org.example.project.ui.components.inputs.CompliSelectionRow
import org.example.project.ui.components.inputs.CompliTextField
import org.example.project.ui.theme.CompliTheme

@Composable
fun DetailsScreen(component: DetailsComponent) {
    val state by component.state.collectAsState()
    DetailsScreenContent(
        state = state,
        onIntent = component::onIntent
    )
}

@Composable
fun DetailsScreenContent(
    state: DetailsState,
    onIntent: (DetailsIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding().verticalScroll(rememberScrollState()).padding(CompliTheme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.medium)
    ) {
        CompliTextButton(text = "Назад", onClick = { onIntent(DetailsIntent.Back) })
        when (val current = state) {
            DetailsState.Loading -> CompliContentState(CompliContentStateType.Loading)
            DetailsState.Error -> {
                Text("Не удалось загрузить шаблон")
                CompliPrimaryButton(text = "Повторить", onClick = { onIntent(DetailsIntent.Reload) })
            }
            is DetailsState.Loaded -> {
                Text(current.template.summary.title, style = CompliTheme.typography.headlineSmall)
                Text(current.template.summary.description)
                Text(current.template.body)
                current.template.parameters.forEach { parameter ->
                    val value = current.values[parameter.name].orEmpty()
                    val error = current.errors.firstOrNull { it.parameterName == parameter.name }
                    if (parameter.type == "choice") {
                        Text(parameter.label, style = CompliTheme.typography.titleMedium)
                        parameter.options.forEach { option ->
                            CompliSelectionRow(
                                selected = value == option,
                                onClick = {
                                    val newValue = if (!parameter.required && value == option) "" else option
                                    onIntent(DetailsIntent.SetValue(parameter.name, newValue))
                                },
                                headline = { Text(option) }
                            )
                        }
                        if (error != null) {
                            Text(
                                if (error is ParameterValidationError.TooLong) "Максимум символов: ${error.maxLength}" else "Выберите вариант",
                                color = CompliTheme.colors.rejected
                            )
                        }
                    } else {
                        val errorText = when (error) {
                            is ParameterValidationError.Required -> "Заполните поле"
                            is ParameterValidationError.TooLong -> "Максимум символов: ${error.maxLength}"
                            is ParameterValidationError.InvalidChoice -> "Выберите вариант"
                            null -> null
                        }
                        CompliTextField(
                            value = value,
                            onValueChange = { onIntent(DetailsIntent.SetValue(parameter.name, it)) },
                            label = parameter.label,
                            errorText = errorText,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                CompliPrimaryButton(
                    text = if (current.composing) "Сборка…" else "Собрать промпт",
                    onClick = { onIntent(DetailsIntent.Compose) },
                    enabled = !current.composing
                )
                if (current.composeError) Text("Не удалось собрать промпт. Попробуйте ещё раз.")
                current.result?.let {
                    Text("Готовый промпт", style = CompliTheme.typography.titleMedium)
                    Text(it)
                }
            }
        }
    }
}
