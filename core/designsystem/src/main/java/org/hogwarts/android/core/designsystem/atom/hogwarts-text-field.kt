package org.hogwarts.android.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Form text field — shadcn/ui Input parity. Used across 18+ screens.
 * Typography per Figma node 7:2112 (Body/Regular): 17sp, line-22, tracking -0.43.
 */
@Composable
fun HogwartsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp,
                lineHeight = 22.sp,
                letterSpacing = (-0.43).sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction() },
                onNext = { onImeAction() }
            ),
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (passwordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else null
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * iOS list-row text field — Figma node 7:2112 (402×44, Row-Text-Field).
 *
 * Layout: ps=16 (start padding), pe=16, py=11.
 *   Title column: w=100, single line, ellipsis, labels/primary (onSurface).
 *   Value editor: flex-1, labels/tertiary for placeholder (onSurfaceVariant @ 0.3).
 *   Bottom hairline: 0.333dp, color outlineVariant (Figma: separators/non-opaque rgba(84,84,86,0.34)).
 * Typography: 17sp Body/Regular, line=22, tracking=-0.43.
 */
@Composable
fun HogwartsRowTextField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.43).sp,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurface
    )
    val separatorColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(start = 16.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(PaddingValues(end = 16.dp, top = 11.dp, bottom = 11.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = textStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(100.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = textStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        enabled = enabled,
                        singleLine = true,
                        textStyle = textStyle,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = keyboardType,
                            imeAction = imeAction
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onImeAction() },
                            onNext = { onImeAction() }
                        )
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
                .height(0.333.dp)
                .background(separatorColor) // Figma: rgba(84,84,86,0.34)
        )
    }
}

@Suppress("UnusedPrivateMember")
private val TextStyleHint: TextStyle = TextStyle.Default

@Preview(showBackground = true)
@Composable
private fun HogwartsTextFieldPreview() {
    HogwartsTheme {
        Column {
            HogwartsTextField(
                value = "user@example.com",
                onValueChange = {},
                label = "Email",
                keyboardType = KeyboardType.Email
            )
        }
    }
}

@Preview(showBackground = true, name = "Row")
@Composable
private fun HogwartsRowTextFieldPreview() {
    HogwartsTheme {
        Column {
            HogwartsRowTextField(
                title = "Title",
                value = "",
                onValueChange = {},
                placeholder = "Value"
            )
            HogwartsRowTextField(
                title = "Title",
                value = "Content",
                onValueChange = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Password")
@Composable
private fun HogwartsTextFieldPasswordPreview() {
    HogwartsTheme {
        HogwartsTextField(
            value = "password123",
            onValueChange = {},
            label = "Password",
            isPassword = true
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun HogwartsTextFieldErrorPreview() {
    HogwartsTheme {
        HogwartsTextField(
            value = "invalid",
            onValueChange = {},
            label = "Email",
            isError = true,
            errorMessage = "Please enter a valid email address"
        )
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun HogwartsTextFieldRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column {
                HogwartsTextField(
                    value = "user@example.com",
                    onValueChange = {},
                    label = "البريد الإلكتروني",
                    placeholder = "أدخل بريدك",
                    keyboardType = KeyboardType.Email
                )
                HogwartsRowTextField(
                    title = "اسم",
                    value = "",
                    onValueChange = {},
                    placeholder = "القيمة"
                )
            }
        }
    }
}
