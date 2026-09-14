package org.hogwarts.android.feature.auth.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.auth.R
import org.hogwarts.android.feature.auth.domain.model.AuthError
import org.hogwarts.android.feature.auth.domain.model.DemoRole
import org.hogwarts.android.feature.auth.domain.model.FieldError

/**
 * The `(auth)` layout on a phone: `min-h-screen items-center justify-center px-6`
 * around a borderless card (`px-6 py-6`), so a form sits centered with a 48dp
 * gutter and never wider than the web's 350px card.
 */
@Composable
internal fun AuthFrame(
    modifier: Modifier = Modifier,
    cardPadding: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HogwartsTheme.colors.background)
            .safeDrawingPadding()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .widthIn(max = if (cardPadding) 398.dp else 400.dp)
                .fillMaxWidth()
                .padding(horizontal = if (cardPadding) 24.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            content = content,
        )
    }
}

@StringRes
internal fun AuthError.messageRes(): Int = when (this) {
    AuthError.InvalidCredentials -> R.string.auth_error_invalid_credentials
    AuthError.SocialOnly -> R.string.auth_error_social_only
    AuthError.Suspended -> R.string.auth_error_suspended
    AuthError.PermissionDenied, AuthError.UnsupportedRole -> R.string.auth_error_permission_denied
    AuthError.AccountNotFound -> R.string.auth_error_account_not_found
    AuthError.TooManyRequests -> R.string.auth_error_too_many_requests
    AuthError.Network -> R.string.auth_error_network
    AuthError.InvalidCode -> R.string.auth_error_invalid_code
    AuthError.CodeExpired -> R.string.auth_error_code_expired
    AuthError.NoSchool -> R.string.auth_error_no_school
    AuthError.BiometricFailed -> R.string.auth_error_biometric
    AuthError.Generic -> R.string.auth_error_generic
}

@StringRes
internal fun FieldError.messageRes(): Int = when (this) {
    FieldError.IdentifierRequired -> R.string.auth_field_identifier_required
    FieldError.InvalidIdentifier -> R.string.auth_field_invalid_identifier
    FieldError.PasswordRequired -> R.string.auth_field_password_required
    FieldError.InvalidEmail -> R.string.auth_field_invalid_email
    FieldError.PasswordTooShort -> R.string.auth_field_password_too_short
}

@StringRes
internal fun DemoRole.labelRes(): Int = when (this) {
    DemoRole.Admin -> R.string.auth_demo_role_admin
    DemoRole.Teacher -> R.string.auth_demo_role_teacher
    DemoRole.Student -> R.string.auth_demo_role_student
    DemoRole.Guardian -> R.string.auth_demo_role_guardian
    DemoRole.Accountant -> R.string.auth_demo_role_accountant
    DemoRole.Staff -> R.string.auth_demo_role_staff
}

/** The web's `maskEmail`: keep two characters of the local part. */
internal fun maskEmail(email: String): String {
    val at = email.indexOf('@')
    if (at < 0) return email
    val local = email.substring(0, at)
    val visible = local.take(2)
    return visible + "*".repeat((local.length - 2).coerceAtLeast(0)) + email.substring(at)
}
