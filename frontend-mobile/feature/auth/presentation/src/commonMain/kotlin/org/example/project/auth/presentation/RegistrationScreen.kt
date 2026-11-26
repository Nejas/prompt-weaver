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
fun RegistrationScreen(component: RegistrationComponent) {
    val state by component.state.collectAsState()
    RegistrationScreenContent(state = state, onIntent = component::onIntent)
}

@Composable
fun RegistrationScreenContent(
    state: RegistrationState,
    onIntent: (RegistrationIntent) -> Unit,
) {
    CompliAuthScreen(
        title = stringResource(Res.string.auth_registration_title),
        subtitle = stringResource(Res.string.auth_registration_subtitle),
        footerText = stringResource(Res.string.auth_has_account),
        footerAction = stringResource(Res.string.auth_login),
        onFooterAction = { onIntent(RegistrationIntent.Login) },
    ) {
        Spacer(Modifier.height(9.dp))
        CompliFormFieldGroup(Modifier.fillMaxWidth()) {
            CompliTextField(
                value = state.firstName,
                onValueChange = { onIntent(RegistrationIntent.UpdateFirstName(it)) },
                modifier = Modifier.fillMaxWidth().height(70.dp),
                label = stringResource(Res.string.auth_first_name),
                singleLine = true,
                enabled = !state.isSubmitting,
                shape = RectangleShape,
            )
            CompliFormFieldDivider()
            CompliTextField(
                value = state.lastName,
                onValueChange = { onIntent(RegistrationIntent.UpdateLastName(it)) },
                modifier = Modifier.fillMaxWidth().height(70.dp),
                label = stringResource(Res.string.auth_last_name),
                singleLine = true,
                enabled = !state.isSubmitting,
                shape = RectangleShape,
            )
            CompliFormFieldDivider()
            CompliTextField(
                value = state.email,
                onValueChange = { onIntent(RegistrationIntent.UpdateEmail(it)) },
                modifier = Modifier.fillMaxWidth().height(70.dp),
                label = stringResource(Res.string.auth_email),
                singleLine = true,
                enabled = !state.isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RectangleShape,
            )
            CompliFormFieldDivider()
            CompliTextField(
                value = state.password,
                onValueChange = { onIntent(RegistrationIntent.UpdatePassword(it)) },
                modifier = Modifier.fillMaxWidth().height(70.dp),
                label = stringResource(Res.string.auth_password),
                singleLine = true,
                enabled = !state.isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                shape = RectangleShape,
            )
        }
        Spacer(Modifier.height(28.dp))
        state.error?.let {
            Text(it.asText(), color = CompliTheme.colors.rejected, style = CompliTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }
        CompliPrimaryButton(
            text = stringResource(Res.string.auth_create_account),
            onClick = { onIntent(RegistrationIntent.Submit) },
            modifier = Modifier.fillMaxWidth(),
            loading = state.isSubmitting,
            minHeight = 48.dp,
            shape = RoundedCornerShape(CompliTheme.radii.medium),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.auth_terms_notice),
            style = CompliTheme.typography.bodySmall,
            color = CompliTheme.colors.onSurfaceVariant,
        )
    }
}
