package org.hogwarts.android.feature.auth.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.FieldMessage
import org.hogwarts.android.core.designsystem.kit.FormAlert
import org.hogwarts.android.core.designsystem.kit.FormButton
import org.hogwarts.android.core.designsystem.kit.Input
import org.hogwarts.android.core.designsystem.kit.ListRow
import org.hogwarts.android.core.designsystem.kit.ListRows
import org.hogwarts.android.core.designsystem.kit.PasswordInput
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.kit.TextLink
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.core.security.BiometricHelper
import org.hogwarts.android.feature.auth.R
import org.hogwarts.android.feature.auth.domain.model.SchoolInfo
import org.hogwarts.android.feature.auth.domain.model.DemoRole

/**
 * Login: email or username and a password, and under them "Try demo", which
 * turns the card over to the demo school's role picker. No social sign-in
 * and no sign-up link — a school account is issued, not self-made.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    biometricHelper: BiometricHelper,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val language = LocalConfiguration.current.locales[0].language
    val biometricTitle = stringResource(R.string.auth_biometric)
    val biometricNegative = stringResource(R.string.auth_biometric_use_password)

    LaunchedEffect(state.signedIn) {
        if (state.signedIn) onLoginSuccess()
    }
    BackHandler(enabled = state.schools != null) { viewModel.dismissSchoolPicker() }

    val schools = state.schools
    if (schools != null) {
        SchoolPickerContent(
            schools = schools,
            arabic = language == "ar",
            state = state,
            onSelect = viewModel::onSchoolSelected,
            onBack = viewModel::dismissSchoolPicker,
        )
        return
    }

    LoginContent(
        state = state,
        onIdentifierChange = viewModel::onIdentifierChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisible = viewModel::onTogglePasswordVisible,
        onLogin = viewModel::login,
        onForgotPassword = onNavigateToForgotPassword,
        onBiometric = (activity as? FragmentActivity)?.let { fragmentActivity ->
            {
                biometricHelper.authenticate(
                    activity = fragmentActivity,
                    title = biometricTitle,
                    subtitle = "",
                    negativeButtonText = biometricNegative,
                    onSuccess = viewModel::onBiometricSuccess,
                    onError = viewModel::onBiometricError,
                )
            }
        },
        onShowDemo = viewModel::showDemo,
        onShowCredentials = viewModel::showCredentials,
        onDemoRoleSelected = viewModel::onDemoRoleSelected,
        onDemoLogin = viewModel::loginAsDemo,
    )
}

@Composable
internal fun LoginContent(
    state: LoginUiState,
    onIdentifierChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onTogglePasswordVisible: () -> Unit = {},
    onLogin: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onBiometric: (() -> Unit)? = null,
    onShowDemo: () -> Unit = {},
    onShowCredentials: () -> Unit = {},
    onDemoRoleSelected: (DemoRole) -> Unit = {},
    onDemoLogin: () -> Unit = {},
) {
    // "Try demo" turns the card over: the form spins to its edge, the role
    // picker comes round on the back, and "Sign in with email" turns it back.
    val demo = state.mode == LoginMode.Demo && state.demoRoles.isNotEmpty()
    val rotation by animateFloatAsState(
        targetValue = if (demo) 180f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "login-flip",
    )
    val density = LocalDensity.current.density
    Box(
        Modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
        },
    ) {
        if (rotation <= 90f) {
            CredentialsSide(state, onIdentifierChange, onPasswordChange, onTogglePasswordVisible, onLogin,
                onForgotPassword, onBiometric, onShowDemo)
        } else {
            // The back face is drawn mirrored; turning it once more reads it right way round.
            Box(Modifier.graphicsLayer { rotationY = 180f }) {
                DemoLoginContent(state, onDemoRoleSelected, onDemoLogin, onShowCredentials)
            }
        }
    }
}

@Composable
private fun CredentialsSide(
    state: LoginUiState,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisible: () -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onBiometric: (() -> Unit)?,
    onShowDemo: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val submit = {
        keyboard?.hide()
        onLogin()
    }

    AuthFrame {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Input(
                    value = state.identifier,
                    onValueChange = onIdentifierChange,
                    placeholder = stringResource(R.string.auth_identifier_placeholder),
                    enabled = !state.isLoading,
                    isError = state.identifierError != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        autoCorrectEnabled = false,
                    ),
                )
                state.identifierError?.let { FieldMessage(stringResource(it.messageRes())) }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PasswordInput(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    visible = state.passwordVisible,
                    onToggleVisible = onTogglePasswordVisible,
                    showLabel = stringResource(R.string.auth_password_show),
                    hideLabel = stringResource(R.string.auth_password_hide),
                    placeholder = stringResource(R.string.auth_password_placeholder),
                    enabled = !state.isLoading,
                    isError = state.passwordError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                )
                TextLink(stringResource(R.string.auth_forgot_password), onClick = onForgotPassword)
                state.passwordError?.let { FieldMessage(stringResource(it.messageRes())) }
            }

            state.error?.let { FormAlert(stringResource(it.messageRes())) }

            FormButton(
                label = stringResource(R.string.auth_sign_in),
                onClick = submit,
                loading = state.isLoading,
            )

            if (state.canUseBiometric && onBiometric != null) {
                val label = stringResource(R.string.auth_biometric)
                IconButton(
                    onClick = onBiometric,
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(48.dp)
                        .semantics { contentDescription = label },
                ) {
                    Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = HogwartsTheme.colors.foreground, modifier = Modifier.size(32.dp))
                }
            }
        }

        if (state.demoRoles.isNotEmpty()) {
            TextLink(
                stringResource(R.string.auth_try_demo),
                onClick = onShowDemo,
                enabled = !state.isLoading,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

/**
 * The card's back: `demo-form.tsx` — a role select, Login, and "Sign in with
 * email instead" to turn the card back. The select's menu opens exactly as
 * wide as the select, as the web's popover does (`w-(--radix-select-trigger-width)`).
 */
@Composable
internal fun DemoLoginContent(
    state: LoginUiState,
    onRoleSelected: (DemoRole) -> Unit,
    onLogin: () -> Unit,
    onUseEmail: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    var expanded by remember { mutableStateOf(false) }
    var selectWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    // The same frame as the front, so the card keeps its width as it turns.
    AuthFrame {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box {
                val selected = state.demoRole
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { selectWidth = with(density) { it.width.toDp() } }
                        .height(48.dp)
                        .clip(HogwartsShapes.Lg)
                        .border(1.dp, colors.input, HogwartsShapes.Lg)
                        .clickable(enabled = !state.isLoading, role = Role.DropdownList) { expanded = true }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selected?.let { stringResource(it.labelRes()) }
                            ?: stringResource(R.string.auth_demo_role_prompt),
                        style = type.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
                        color = if (selected != null) colors.foreground else colors.mutedForeground,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = colors.mutedForeground, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = HogwartsShapes.Lg,
                    containerColor = colors.background,
                    modifier = Modifier.width(selectWidth),
                ) {
                    state.demoRoles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(stringResource(role.labelRes()), style = type.body.copy(fontSize = 16.sp)) },
                            onClick = {
                                expanded = false
                                onRoleSelected(role)
                            },
                            modifier = Modifier
                                .height(44.dp)
                                .then(if (role == selected) Modifier.background(colors.muted) else Modifier),
                        )
                    }
                }
            }

            state.error?.let { FormAlert(stringResource(it.messageRes())) }

            FormButton(
                label = stringResource(R.string.auth_sign_in),
                onClick = onLogin,
                loading = state.isLoading,
                enabled = state.demoRole != null,
                height = 48.dp,
            )
        }
        TextLink(
            stringResource(R.string.auth_use_email_instead),
            onClick = onUseEmail,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

/** A Google identity in several schools picks one; no web equivalent (the web knows the school from its host). */
@Composable
internal fun SchoolPickerContent(
    schools: List<SchoolInfo>,
    arabic: Boolean,
    state: LoginUiState,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
) {
    AuthFrame(cardPadding = false) {
        SectionHeader(
            title = stringResource(R.string.auth_choose_school),
            description = stringResource(R.string.auth_choose_school_hint),
        )
        ListRows(
            divided = true,
            rows = schools.map { school ->
                {
                    val name = if (arabic) school.name else school.nameEn ?: school.name
                    ListRow(
                        title = name,
                        description = school.domain,
                        onClick = if (state.isLoading) null else ({ onSelect(school.id) }),
                        chevron = true,
                    )
                }
            },
        )
        state.error?.let { FormAlert(stringResource(it.messageRes())) }
        TextLink(
            stringResource(R.string.auth_back_to_login),
            onClick = onBack,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}
