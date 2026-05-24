package org.hogwarts.android.feature.auth.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.FormError
import org.hogwarts.android.core.designsystem.atom.HogwartsButton
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.auth.R

/**
 * OTP verification screen — shown after registration.
 *
 * 4-digit code input with auto-submit, resend timer, and email display.
 */
@Composable
fun VerifyScreen(
    email: String,
    onVerifySuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: VerifyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(email) {
        viewModel.setEmail(email)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onVerifySuccess()
        }
    }

    VerifyScreenContent(
        uiState = uiState,
        onOtpChange = viewModel::onOtpChange,
        onVerifyClick = viewModel::verify,
        onResendClick = viewModel::resendOtp,
        onBackClick = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerifyScreenContent(
    uiState: VerifyUiState,
    onOtpChange: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onResendClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                    .widthIn(max = 350.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(48.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = stringResource(R.string.auth_verify_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Subtitle
                Text(
                    text = stringResource(R.string.auth_verify_subtitle, uiState.email),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // OTP Input — 4 boxes
                OtpInput(
                    otp = uiState.otp,
                    onOtpChange = { value ->
                        onOtpChange(value)
                        if (value.length == 4) {
                            keyboardController?.hide()
                            onVerifyClick()
                        }
                    },
                    enabled = !uiState.isLoading,
                    focusRequester = focusRequester
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Error
                if (uiState.error != null) {
                    FormError(message = uiState.error)
                }

                // Verify Button
                HogwartsButton(
                    text = stringResource(R.string.auth_verify_button),
                    onClick = {
                        keyboardController?.hide()
                        onVerifyClick()
                    },
                    enabled = uiState.otp.length == 4,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Resend
                TextButton(
                    onClick = onResendClick,
                    enabled = uiState.resendCooldown == 0 && !uiState.isLoading
                ) {
                    Text(
                        text = if (uiState.resendCooldown > 0) {
                            stringResource(R.string.auth_verify_resend_cooldown, uiState.resendCooldown)
                        } else {
                            stringResource(R.string.auth_verify_resend)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.resendCooldown > 0) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun OtpInput(
    otp: String,
    onOtpChange: (String) -> Unit,
    enabled: Boolean,
    focusRequester: FocusRequester
) {
    BasicTextField(
        value = otp,
        onValueChange = { value ->
            if (value.length <= 4 && value.all { it.isDigit() }) {
                onOtpChange(value)
            }
        },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.focusRequester(focusRequester),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(4) { index ->
                    val char = otp.getOrNull(index)
                    val isFocused = otp.length == index

                    Surface(
                        modifier = Modifier
                            .width(56.dp)
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = if (isFocused) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        tonalElevation = if (isFocused) 2.dp else 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = char?.toString() ?: "",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    )
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RTL", locale = "ar")
@Composable
private fun VerifyScreenPreview() {
    HogwartsTheme {
        VerifyScreenContent(
            uiState = VerifyUiState(email = "ahmed@example.com"),
            onOtpChange = {},
            onVerifyClick = {},
            onResendClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "With OTP")
@Composable
private fun VerifyScreenWithOtpPreview() {
    HogwartsTheme {
        VerifyScreenContent(
            uiState = VerifyUiState(email = "ahmed@example.com", otp = "12"),
            onOtpChange = {},
            onVerifyClick = {},
            onResendClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Resend Cooldown")
@Composable
private fun VerifyScreenCooldownPreview() {
    HogwartsTheme {
        VerifyScreenContent(
            uiState = VerifyUiState(
                email = "ahmed@example.com",
                otp = "1234",
                resendCooldown = 47
            ),
            onOtpChange = {},
            onVerifyClick = {},
            onResendClick = {},
            onBackClick = {}
        )
    }
}
