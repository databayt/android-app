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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.FormError
import org.hogwarts.android.core.designsystem.atom.HogwartsButton
import org.hogwarts.android.core.designsystem.atom.HogwartsTextField
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.core.security.BiometricHelper
import org.hogwarts.android.feature.auth.R

/**
 * Login screen — credentials obtained from school admin via web app.
 *
 * Layout:
 * 1. "Login" title (in top bar)
 * 2. Email and password fields
 * 3. Forgot password link
 * 4. Login button
 * 5. Biometric button (if available)
 * 6. "Or continue with" divider + OAuth
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    biometricHelper: BiometricHelper,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalContext.current as? FragmentActivity

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onForgotPasswordClick = onNavigateToForgotPassword,
        onBackClick = onNavigateBack,
        showBiometric = viewModel.canUseBiometricLogin,
        onBiometricClick = {
            activity?.let { fragmentActivity ->
                biometricHelper.authenticate(
                    activity = fragmentActivity,
                    onSuccess = { viewModel.onBiometricSuccess() },
                    onError = { message -> viewModel.onBiometricError(message) }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    showBiometric: Boolean = false,
    onBiometricClick: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val isLoading = uiState is LoginUiState.Loading
    val errorMessage = (uiState as? LoginUiState.Error)?.message

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.auth_login_title)) },
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
            Surface(
                modifier = Modifier.widthIn(max = 350.dp),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. Form Fields
                    HogwartsTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            onEmailChange(it)
                        },
                        label = stringResource(R.string.auth_login_email_label),
                        placeholder = stringResource(R.string.auth_login_email_placeholder),
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        enabled = !isLoading
                    )

                    HogwartsTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            onPasswordChange(it)
                        },
                        label = stringResource(R.string.auth_login_password_label),
                        isPassword = true,
                        imeAction = ImeAction.Done,
                        enabled = !isLoading,
                        onImeAction = {
                            keyboardController?.hide()
                            if (email.isNotBlank() && password.isNotBlank()) {
                                onLoginClick()
                            }
                        }
                    )

                    // 2. Forgot Password
                    TextButton(
                        onClick = onForgotPasswordClick,
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text(
                            text = stringResource(R.string.auth_login_forgot_password),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )
                    }

                    // 3. Error Message
                    if (errorMessage != null) {
                        FormError(message = errorMessage)
                    }

                    // 4. Login Button
                    HogwartsButton(
                        text = stringResource(R.string.auth_login_button),
                        onClick = {
                            keyboardController?.hide()
                            onLoginClick()
                        },
                        enabled = email.isNotBlank() && password.isNotBlank(),
                        isLoading = isLoading
                    )

                    // 5. Biometric Button
                    if (showBiometric) {
                        IconButton(
                            onClick = onBiometricClick,
                            enabled = !isLoading,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Fingerprint,
                                contentDescription = stringResource(R.string.auth_login_biometric_description),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RTL", locale = "ar")
@Composable
private fun LoginScreenPreview() {
    HogwartsTheme {
        LoginScreenContent(
            uiState = LoginUiState.Idle,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {}
        )
    }
}

@Preview(name = "With Biometric")
@Composable
private fun LoginScreenBiometricPreview() {
    HogwartsTheme {
        LoginScreenContent(
            uiState = LoginUiState.Idle,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            showBiometric = true,
            onBiometricClick = {}
        )
    }
}

@Preview(name = "Error")
@Composable
private fun LoginScreenErrorPreview() {
    HogwartsTheme {
        LoginScreenContent(
            uiState = LoginUiState.Error("Invalid email or password"),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {}
        )
    }
}
