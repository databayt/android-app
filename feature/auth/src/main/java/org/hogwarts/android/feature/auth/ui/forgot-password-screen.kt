package org.hogwarts.android.feature.auth.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.FormError
import org.hogwarts.android.core.designsystem.atom.FormSuccess
import org.hogwarts.android.core.designsystem.atom.HogwartsButton
import org.hogwarts.android.core.designsystem.atom.HogwartsTextField
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.auth.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onResetSuccess: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (uiState.step) {
                            ResetStep.EMAIL -> stringResource(R.string.auth_reset_password_title)
                            ResetStep.OTP -> stringResource(R.string.auth_verify_code_title)
                            ResetStep.NEW_PASSWORD -> stringResource(R.string.auth_new_password_title)
                            ResetStep.SUCCESS -> stringResource(R.string.auth_password_reset_title)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.auth_back_description))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                when (uiState.step) {
                    ResetStep.EMAIL -> EmailStep(
                        email = uiState.email,
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        onEmailChange = viewModel::onEmailChange,
                        onSubmit = {
                            keyboardController?.hide()
                            viewModel.requestReset()
                        }
                    )

                    ResetStep.OTP -> OtpStep(
                        otp = uiState.otp,
                        email = uiState.email,
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        resendCooldown = uiState.resendCooldown,
                        onOtpChange = viewModel::onOtpChange,
                        onVerify = {
                            keyboardController?.hide()
                            viewModel.verifyOtp()
                        },
                        onResend = viewModel::resendOtp
                    )

                    ResetStep.NEW_PASSWORD -> NewPasswordStep(
                        newPassword = uiState.newPassword,
                        confirmPassword = uiState.confirmPassword,
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        onNewPasswordChange = viewModel::onNewPasswordChange,
                        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                        onSubmit = {
                            keyboardController?.hide()
                            viewModel.setNewPassword()
                        }
                    )

                    ResetStep.SUCCESS -> SuccessStep(
                        onBackToLogin = onResetSuccess
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun EmailStep(
    email: String,
    isLoading: Boolean,
    error: String?,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Text(
        text = stringResource(R.string.auth_reset_email_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    HogwartsTextField(
        value = email,
        onValueChange = onEmailChange,
        label = stringResource(R.string.auth_reset_email_label),
        placeholder = stringResource(R.string.auth_reset_email_placeholder),
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Done,
        enabled = !isLoading,
        onImeAction = onSubmit
    )

    if (error != null) {
        FormError(message = error)
    }

    HogwartsButton(
        text = stringResource(R.string.auth_reset_send_code_button),
        onClick = onSubmit,
        enabled = email.isNotBlank(),
        isLoading = isLoading,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun OtpStep(
    otp: String,
    email: String,
    isLoading: Boolean,
    error: String?,
    resendCooldown: Int,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit
) {
    Text(
        text = stringResource(R.string.auth_reset_otp_description, email),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    HogwartsTextField(
        value = otp,
        onValueChange = onOtpChange,
        label = stringResource(R.string.auth_reset_verification_code_label),
        placeholder = stringResource(R.string.auth_reset_verification_code_placeholder),
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done,
        enabled = !isLoading,
        onImeAction = onVerify
    )

    if (error != null) {
        FormError(message = error)
    }

    HogwartsButton(
        text = stringResource(R.string.auth_reset_verify_button),
        onClick = onVerify,
        enabled = otp.length == 6,
        isLoading = isLoading,
        modifier = Modifier.fillMaxWidth()
    )

    TextButton(
        onClick = onResend,
        enabled = resendCooldown == 0
    ) {
        Text(
            text = if (resendCooldown > 0) stringResource(R.string.auth_reset_resend_cooldown, resendCooldown) else stringResource(R.string.auth_reset_resend),
            style = MaterialTheme.typography.bodySmall,
            color = if (resendCooldown > 0) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}

@Composable
private fun NewPasswordStep(
    newPassword: String,
    confirmPassword: String,
    isLoading: Boolean,
    error: String?,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Text(
        text = stringResource(R.string.auth_reset_new_password_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    HogwartsTextField(
        value = newPassword,
        onValueChange = onNewPasswordChange,
        label = stringResource(R.string.auth_reset_new_password_label),
        isPassword = true,
        imeAction = ImeAction.Next,
        enabled = !isLoading
    )

    HogwartsTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = stringResource(R.string.auth_reset_confirm_password_label),
        isPassword = true,
        imeAction = ImeAction.Done,
        enabled = !isLoading,
        onImeAction = onSubmit
    )

    if (error != null) {
        FormError(message = error)
    }

    HogwartsButton(
        text = stringResource(R.string.auth_reset_set_password_button),
        onClick = onSubmit,
        enabled = newPassword.isNotBlank() && confirmPassword.isNotBlank(),
        isLoading = isLoading,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SuccessStep(
    onBackToLogin: () -> Unit
) {
    FormSuccess(message = stringResource(R.string.auth_reset_success_message))

    Spacer(modifier = Modifier.height(16.dp))

    HogwartsButton(
        text = stringResource(R.string.auth_reset_back_to_login_button),
        onClick = onBackToLogin,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(name = "Email Step")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmailStepPreview() {
    HogwartsTheme {
        Surface {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EmailStep(
                    email = "",
                    isLoading = false,
                    error = null,
                    onEmailChange = {},
                    onSubmit = {}
                )
            }
        }
    }
}

@Preview(name = "OTP Step")
@Composable
private fun OtpStepPreview() {
    HogwartsTheme {
        Surface {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OtpStep(
                    otp = "123",
                    email = "user@example.com",
                    isLoading = false,
                    error = null,
                    resendCooldown = 45,
                    onOtpChange = {},
                    onVerify = {},
                    onResend = {}
                )
            }
        }
    }
}
