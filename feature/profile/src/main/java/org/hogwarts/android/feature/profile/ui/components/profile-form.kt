package org.hogwarts.android.feature.profile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.feature.profile.R
import org.hogwarts.android.feature.profile.domain.validation.ProfileUpdateForm
import org.hogwarts.android.feature.profile.domain.validation.ProfileValidator

/**
 * Web reference: profile/form.tsx — edit your own profile.
 */
@Composable
fun ProfileForm(
    initial: ProfileUpdateForm,
    onSubmit: (ProfileUpdateForm) -> Unit,
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false
) {
    var form by remember { mutableStateOf(initial) }
    val errors = remember(form) { ProfileValidator.validateProfileUpdate(form) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppleSpacing.Standard),
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
    ) {
        Field(
            label = stringResource(R.string.profile_form_display_name),
            value = form.displayName,
            onChange = { form = form.copy(displayName = it) },
            errorKey = errors["displayName"]
        )
        Field(
            label = stringResource(R.string.profile_form_bio),
            value = form.bio.orEmpty(),
            onChange = { form = form.copy(bio = it.ifBlank { null }) },
            errorKey = errors["bio"],
            singleLine = false
        )
        Field(
            label = stringResource(R.string.profile_form_website),
            value = form.website.orEmpty(),
            onChange = { form = form.copy(website = it.ifBlank { null }) },
            errorKey = errors["website"]
        )
        Field(
            label = stringResource(R.string.profile_form_github),
            value = form.github.orEmpty(),
            onChange = { form = form.copy(github = it.ifBlank { null }) },
            errorKey = errors["github"]
        )
        Field(
            label = stringResource(R.string.profile_form_status),
            value = form.statusMessage.orEmpty(),
            onChange = { form = form.copy(statusMessage = it.ifBlank { null }) },
            errorKey = errors["statusMessage"]
        )
        Button(
            onClick = { onSubmit(form) },
            enabled = !isSubmitting && errors.isEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.profile_form_save))
        }
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    errorKey: String?,
    singleLine: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label) },
            isError = errorKey != null,
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth()
        )
        if (errorKey != null) {
            Text(
                text = errorKey.replace('_', ' '),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
