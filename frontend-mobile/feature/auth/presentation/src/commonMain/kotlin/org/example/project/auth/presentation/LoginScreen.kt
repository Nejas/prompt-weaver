package org.example.project.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import org.example.project.ui.components.actions.CompliPrimaryButton
import org.example.project.ui.components.actions.CompliTextButton
import org.example.project.ui.components.inputs.CompliTextField
import org.example.project.ui.theme.CompliTheme

@Composable
fun LoginScreen(component: LoginComponent) {
    val state by component.state.collectAsState()
    Column(Modifier.fillMaxSize().safeContentPadding().verticalScroll(rememberScrollState())
        .padding(CompliTheme.spacing.large), verticalArrangement = Arrangement.spacedBy(CompliTheme.spacing.medium)) {
        Text("Вход", style = CompliTheme.typography.headlineSmall)
        CompliTextField(state.email, component::updateEmail, Modifier.fillMaxWidth(), label = "Email",
            singleLine = true, enabled = !state.submitting, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        CompliTextField(state.password, component::updatePassword, Modifier.fillMaxWidth(), label = "Пароль",
            singleLine = true, enabled = !state.submitting, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation())
        state.error?.let { Text(it, color = CompliTheme.colors.rejected) }
        CompliPrimaryButton("Войти", component::submit, Modifier.fillMaxWidth(), loading = state.submitting)
        CompliTextButton("Назад", component.back, Modifier.fillMaxWidth(), enabled = !state.submitting)
    }
}
