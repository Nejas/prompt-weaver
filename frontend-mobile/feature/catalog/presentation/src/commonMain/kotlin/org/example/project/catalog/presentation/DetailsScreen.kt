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
    Column(Modifier.fillMaxSize().safeContentPadding().verticalScroll(rememberScrollState()).padding(CompliTheme.spacing.large), verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.medium)) {
        CompliTextButton(text = "Назад", onClick = component.back)
        when (val current = state) {
            DetailsState.Loading -> CompliContentState(CompliContentStateType.Loading)
            DetailsState.Error -> {
                Text("Не удалось загрузить шаблон")
                CompliPrimaryButton(text = "Повторить", onClick = component::reload)
            }
            is DetailsState.Loaded -> {
                Text(current.template.summary.title, style = CompliTheme.typography.headlineSmall)
                Text(current.template.summary.description)
                current.template.parameters.forEach { parameter ->
                    val value = current.values[parameter.name].orEmpty()
                    val error = current.errors.any { it.parameterName == parameter.name }
                    if (parameter.type == "choice") {
                        Text(parameter.label, style = CompliTheme.typography.titleMedium)
                        parameter.options.forEach { option ->
                            CompliSelectionRow(selected = value == option, onClick = { component.setValue(parameter.name, if (!parameter.required && value == option) "" else option) }, headline = { Text(option) })
                        }
                        if (error) Text("Выберите вариант", color = CompliTheme.colors.rejected)
                    } else {
                        CompliTextField(value = value, onValueChange = { component.setValue(parameter.name, it) }, label = parameter.label, errorText = if (error) "Заполните поле" else null, modifier = Modifier.fillMaxWidth())
                    }
                }
                CompliPrimaryButton(text = "Проверить", onClick = component::validate)
                if (current.validated && current.errors.isEmpty()) Text("Предварительная проверка пройдена")
            }
        }
    }
}
