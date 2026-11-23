package org.example.project.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import org.example.project.ui.components.actions.CompliPrimaryButton
import org.example.project.ui.components.actions.CompliTextButton
import org.example.project.ui.components.inputs.CompliTextField
import org.example.project.ui.theme.CompliTheme

@Composable
fun RegistrationScreen(component: RegistrationComponent) {
    val state by component.state.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding().verticalScroll(rememberScrollState()).padding(CompliTheme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.medium),
    ) {
        Text("Регистрация", style = MaterialTheme.typography.headlineSmall)
        Text("Укажите имя и фамилию — они будут видны в вашем профиле.")
        CompliTextField(state.firstName, component::updateFirstName, Modifier.fillMaxWidth(), label = "Имя", singleLine = true, enabled = !state.isSubmitting)
        CompliTextField(state.lastName, component::updateLastName, Modifier.fillMaxWidth(), label = "Фамилия", singleLine = true, enabled = !state.isSubmitting)
        CompliTextField(
            state.email, component::updateEmail, Modifier.fillMaxWidth(), label = "Email", singleLine = true,
            enabled = !state.isSubmitting, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        CompliTextField(
            state.password, component::updatePassword, Modifier.fillMaxWidth(), label = "Пароль", singleLine = true,
            supportingText = "Минимум 8 символов", enabled = !state.isSubmitting,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), visualTransformation = PasswordVisualTransformation(),
        )
        state.errorMessage?.let { Text(it, color = CompliTheme.colors.rejected) }
        Spacer(Modifier.height(CompliTheme.spacing.small))
        CompliPrimaryButton("Зарегистрироваться", component::submit, Modifier.fillMaxWidth(), loading = state.isSubmitting)
        CompliTextButton("Назад к каталогу", component::back, Modifier.fillMaxWidth(), enabled = !state.isSubmitting)
        CompliTextButton("Уже есть аккаунт? Войти", component::login, Modifier.fillMaxWidth(), enabled = !state.isSubmitting)
    }
}
