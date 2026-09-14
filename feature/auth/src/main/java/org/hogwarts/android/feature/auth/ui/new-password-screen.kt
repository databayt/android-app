package org.hogwarts.android.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.FieldMessage
import org.hogwarts.android.core.designsystem.kit.FormAlert
import org.hogwarts.android.core.designsystem.kit.FormAlertTone
import org.hogwarts.android.core.designsystem.kit.FormButton
import org.hogwarts.android.core.designsystem.kit.PasswordInput
import org.hogwarts.android.core.designsystem.kit.TextLink
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.auth.R
import org.hogwarts.android.feature.auth.domain.model.AuthError

/** New password — hogwarts `(auth)/new-password`: heading, one field, Reset Password, Back to login. */
@Composable
fun NewPasswordScreen(
    onBackToCode: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: NewPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    NewPasswordContent(state, viewModel::onPasswordChange, viewModel::onTogglePasswordVisible, viewModel::submit, onBackToCode, onBackToLogin)
}

@Composable
internal fun NewPasswordContent(
    state: NewPasswordUiState,
    onPasswordChange: (String) -> Unit = {},
    onToggleVisible: () -> Unit = {},
    onSubmit: () -> Unit = {},
    onBackToCode: () -> Unit = {},
    onBackToLogin: () -> Unit = {},
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val submit = {
        keyboard?.hide()
        onSubmit()
    }
    AuthFrame {
        Text(
            stringResource(R.string.auth_enter_new_password),
            style = HogwartsTheme.type.section,
            color = HogwartsTheme.colors.foreground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            if (!state.updated) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PasswordInput(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        visible = state.passwordVisible,
                        onToggleVisible = onToggleVisible,
                        showLabel = stringResource(R.string.auth_password_show),
                        hideLabel = stringResource(R.string.auth_password_hide),
                        placeholder = stringResource(R.string.auth_new_password_placeholder),
                        enabled = !state.isLoading,
                        isError = state.passwordError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                    )
                    state.passwordError?.let { FieldMessage(stringResource(it.messageRes())) }
                }
            }
            state.error?.let { FormAlert(stringResource(it.messageRes())) }
            if (state.updated) {
                FormAlert(stringResource(R.string.auth_password_updated), tone = FormAlertTone.Success)
                FormButton(stringResource(R.string.auth_sign_in), onClick = onBackToLogin)
            } else {
                FormButton(stringResource(R.string.auth_reset_password), onClick = submit, loading = state.isLoading)
            }
        }
        if (!state.updated) {
            val codeProblem = state.error == AuthError.InvalidCode || state.error == AuthError.CodeExpired
            TextLink(
                stringResource(if (codeProblem) R.string.auth_back else R.string.auth_back_to_login),
                onClick = if (codeProblem) onBackToCode else onBackToLogin,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}
