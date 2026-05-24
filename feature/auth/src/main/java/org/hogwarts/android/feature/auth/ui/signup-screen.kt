package org.hogwarts.android.feature.auth.ui

import android.app.Activity
import android.content.res.Configuration
import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.DividerWithText
import org.hogwarts.android.core.designsystem.atom.FormError
import org.hogwarts.android.core.designsystem.atom.HogwartsButton
import org.hogwarts.android.core.designsystem.atom.HogwartsTextField
import org.hogwarts.android.core.designsystem.atom.SocialAuthButtons
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.auth.R

/**
 * Join (register) screen — separate page with back arrow.
 *
 * Registration: first name, last name, email, password, school.
 * After successful registration, navigates to OTP verification.
 *
 * Layout:
 * 1. "Join" title (in top bar)
 * 2. First name, last name, email, password, school ID fields
 * 3. Join button
 * 4. "Or continue with" divider + OAuth
 * 5. "Already have account? Login" link
 */
@Composable
fun SignUpScreen(
    onSignUpSuccess: (email: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit = onNavigateToLogin,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSignUpSuccess(uiState.email)
        }
    }

    SignUpScreenContent(
        uiState = uiState,
        onFirstNameChange = viewModel::onFirstNameChange,
        onLastNameChange = viewModel::onLastNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSchoolIdChange = viewModel::onSchoolIdChange,
        onSignUpClick = viewModel::signUp,
        onGoogleClick = {
            scope.launch {
                try {
                    val credentialManager = CredentialManager.create(context)
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(context.getString(
                            context.resources.getIdentifier(
                                "google_web_client_id", "string", context.packageName
                            )
                        ))
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(
                        context = context as Activity,
                        request = request
                    )

                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(
                        result.credential.data
                    )
                    viewModel.signUpWithGoogle(googleIdTokenCredential.idToken)
                } catch (e: GetCredentialCancellationException) {
                    // User cancelled
                } catch (e: Exception) {
                    // Error handled in view model
                }
            }
        },
        onFacebookClick = {
            Toast.makeText(context, context.getString(R.string.auth_signup_facebook_not_configured), Toast.LENGTH_SHORT).show()
        },
        onNavigateToLogin = onNavigateToLogin,
        onBackClick = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignUpScreenContent(
    uiState: SignUpUiState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSchoolIdChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.auth_signup_title)) },
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
                Spacer(modifier = Modifier.height(8.dp))

                // 1. Form Fields
                HogwartsTextField(
                    value = uiState.firstName,
                    onValueChange = onFirstNameChange,
                    label = stringResource(R.string.auth_signup_first_name_label),
                    placeholder = stringResource(R.string.auth_signup_first_name_placeholder),
                    imeAction = ImeAction.Next,
                    enabled = !uiState.isLoading,
                    isError = uiState.fieldErrors.containsKey("firstName"),
                    errorMessage = uiState.fieldErrors["firstName"]
                )

                HogwartsTextField(
                    value = uiState.lastName,
                    onValueChange = onLastNameChange,
                    label = stringResource(R.string.auth_signup_last_name_label),
                    placeholder = stringResource(R.string.auth_signup_last_name_placeholder),
                    imeAction = ImeAction.Next,
                    enabled = !uiState.isLoading,
                    isError = uiState.fieldErrors.containsKey("lastName"),
                    errorMessage = uiState.fieldErrors["lastName"]
                )

                HogwartsTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = stringResource(R.string.auth_signup_email_label),
                    placeholder = stringResource(R.string.auth_signup_email_placeholder),
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    enabled = !uiState.isLoading,
                    isError = uiState.fieldErrors.containsKey("email"),
                    errorMessage = uiState.fieldErrors["email"]
                )

                HogwartsTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    label = stringResource(R.string.auth_signup_password_label),
                    isPassword = true,
                    imeAction = ImeAction.Next,
                    enabled = !uiState.isLoading,
                    isError = uiState.fieldErrors.containsKey("password"),
                    errorMessage = uiState.fieldErrors["password"]
                )

                HogwartsTextField(
                    value = uiState.schoolId,
                    onValueChange = onSchoolIdChange,
                    label = stringResource(R.string.auth_signup_school_id_label),
                    placeholder = stringResource(R.string.auth_signup_school_id_placeholder),
                    imeAction = ImeAction.Done,
                    enabled = !uiState.isLoading,
                    isError = uiState.fieldErrors.containsKey("schoolId"),
                    errorMessage = uiState.fieldErrors["schoolId"],
                    onImeAction = {
                        keyboardController?.hide()
                        onSignUpClick()
                    }
                )

                // 2. Error Message
                if (uiState.generalError != null) {
                    FormError(message = uiState.generalError)
                }

                // 3. Join Button
                HogwartsButton(
                    text = stringResource(R.string.auth_signup_button),
                    onClick = {
                        keyboardController?.hide()
                        onSignUpClick()
                    },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. OAuth
                Spacer(modifier = Modifier.height(8.dp))

                DividerWithText(text = stringResource(R.string.auth_signup_or_continue_with))

                SocialAuthButtons(
                    onGoogleClick = onGoogleClick,
                    onFacebookClick = onFacebookClick,
                    enabled = !uiState.isLoading
                )

                // 5. Login Link
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = stringResource(R.string.auth_signup_already_have_account),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.auth_signup_login_link),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RTL", locale = "ar")
@Composable
private fun SignUpScreenPreview() {
    HogwartsTheme {
        SignUpScreenContent(
            uiState = SignUpUiState(),
            onFirstNameChange = {},
            onLastNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onSchoolIdChange = {},
            onSignUpClick = {},
            onGoogleClick = {},
            onFacebookClick = {},
            onNavigateToLogin = {}
        )
    }
}
