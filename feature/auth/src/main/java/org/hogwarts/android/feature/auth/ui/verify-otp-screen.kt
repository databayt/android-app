package org.hogwarts.android.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.FormAlert
import org.hogwarts.android.core.designsystem.kit.FormAlertTone
import org.hogwarts.android.core.designsystem.kit.FormButton
import org.hogwarts.android.core.designsystem.kit.FormButtonVariant
import org.hogwarts.android.core.designsystem.kit.InputOtp
import org.hogwarts.android.core.designsystem.kit.TextLink
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.auth.R

/** The code step — the web's verify view: heading, masked address, slots, resend, back. */
@Composable
fun VerifyOtpScreen(
    onCodeEntered: (otp: String) -> Unit,
    onWrongEmail: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: VerifyOtpViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.confirmedOtp) {
        state.confirmedOtp?.let {
            viewModel.onNewPasswordOpened()
            onCodeEntered(it)
        }
    }
    VerifyOtpContent(state, viewModel::onOtpChange, viewModel::submit, viewModel::resend, onWrongEmail, onBackToLogin)
}

@Composable
internal fun VerifyOtpContent(
    state: VerifyOtpUiState,
    onOtpChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onResend: () -> Unit = {},
    onWrongEmail: () -> Unit = {},
    onBackToLogin: () -> Unit = {},
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    AuthFrame(cardPadding = false) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.auth_check_your_email),
                style = type.section,
                color = colors.foreground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.auth_code_sent_to))
                    append(' ')
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.foreground)) {
                        append("⁦${maskEmail(state.email)}⁩")
                    }
                },
                style = type.body,
                color = colors.mutedForeground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        InputOtp(
            value = state.otp,
            onValueChange = onOtpChange,
            length = VerifyOtpViewModel.OTP_LENGTH,
            isError = state.error != null,
            onComplete = { onSubmit() },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        state.error?.let { FormAlert(stringResource(it.messageRes())) }
        if (state.resent && state.error == null) {
            FormAlert(stringResource(R.string.auth_check_your_email), tone = FormAlertTone.Success)
        }
        FormButton(
            stringResource(R.string.auth_confirm),
            onClick = onSubmit,
            enabled = state.otp.length == VerifyOtpViewModel.OTP_LENGTH,
        )
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FormButton(
                label = if (state.resendCooldown > 0) {
                    stringResource(R.string.auth_resend_in, state.resendCooldown)
                } else {
                    stringResource(R.string.auth_resend_code)
                },
                onClick = onResend,
                variant = FormButtonVariant.Ghost,
                height = 32.dp,
                enabled = state.resendCooldown == 0,
                loading = state.isResending,
            )
            TextLink(stringResource(R.string.auth_wrong_email), onClick = onWrongEmail)
            TextLink(stringResource(R.string.auth_back_to_login), onClick = onBackToLogin)
        }
    }
}
