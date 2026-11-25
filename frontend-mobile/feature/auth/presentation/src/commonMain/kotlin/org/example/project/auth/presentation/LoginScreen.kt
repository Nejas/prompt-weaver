package org.example.project.auth.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.actions.CompliPrimaryButton
import org.example.project.ui.components.inputs.CompliTextField
import org.example.project.ui.components.screens.CompliAuthScreen
import org.example.project.ui.components.screens.CompliFormFieldDivider
import org.example.project.ui.components.screens.CompliFormFieldGroup
import org.example.project.ui.theme.CompliTheme
import org.example.project.auth.presentation.generated.resources.Res
import org.example.project.auth.presentation.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(component: LoginComponent) {
    val state by component.state.collectAsState()
    LoginScreenContent(state = state, onIntent = component::onIntent)
}

@Composable
fun LoginScreenContent(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
) {
    CompliAuthScreen(
        title = stringResource(Res.string.auth_login_title),
        subtitle = stringResource(Res.string.auth_login_subtitle),
        footerText = stringResource(Res.string.auth_no_account),
        footerAction = stringResource(Res.string.auth_register),
        onFooterAction = { onIntent(LoginIntent.Register) },
    ) {
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.auth_login_section), style = CompliTheme.typography.labelMedium, color = CompliTheme.colors.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        CompliFormFieldGroup(Modifier.fillMaxWidth()) {
            CompliTextField(
                value = state.email,
                onValueChange = { onIntent(LoginIntent.UpdateEmail(it)) },
                modifier = Modifier.fillMaxWidth().height(70.dp),
                label = stringResource(Res.string.auth_email),
                singleLine = true,
                enabled = !state.submitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RectangleShape,
            )
            CompliFormFieldDivider()
            CompliTextField(
                value = state.password,
                onValueChange = { onIntent(LoginIntent.UpdatePassword(it)) },
                modifier = Modifier.fillMaxWidth().height(70.dp),
                label = stringResource(Res.string.auth_password),
                singleLine = true,
                enabled = !state.submitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                shape = RectangleShape,
            )
        }
        Text(
            text = stringResource(Res.string.auth_forgot_password),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            style = CompliTheme.typography.labelLarge,
            color = CompliTheme.colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(18.dp))
        state.error?.let {
            Text(it.asText(), color = CompliTheme.colors.rejected, style = CompliTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }
        CompliPrimaryButton(
            text = stringResource(Res.string.auth_login),
            onClick = { onIntent(LoginIntent.Submit) },
            modifier = Modifier.fillMaxWidth(),
            loading = state.submitting,
            minHeight = 48.dp,
            shape = RoundedCornerShape(CompliTheme.radii.medium),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(Res.string.auth_login_data_notice),
            modifier = Modifier.fillMaxWidth(),
            style = CompliTheme.typography.bodySmall,
            color = CompliTheme.colors.onSurfaceVariant,
        )
    }
}
