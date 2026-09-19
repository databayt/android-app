package org.hogwarts.android.shell

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.hogwarts.android.R
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * The menu toolbar's account control — the avatar, and the menu it opens.
 *
 * `UserButton variant="platform"` is a dropdown, not a link: the reader's name
 * over the account links `getUserMenuItems` returns for the platform surface —
 * Profile, My Account, School Settings for an admin, Help & Support — and
 * Logout under a rule at the end. Tapping the avatar used to jump straight to
 * the profile, which reached one of the five and hid the rest.
 *
 * The web also prints the reader's email under their name. `TenantContext`
 * carries the name and not the address, so the name stands alone here until
 * the session does.
 */
@Composable
fun AccountControl(
    userName: String?,
    role: UserRole?,
    onOpenHref: (href: String) -> Unit,
    onLogout: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    var open by remember { mutableStateOf(false) }

    UserAvatar(
        name = userName.orEmpty(),
        imageUrl = null,
        size = 24.dp,
        containerColor = colors.primary,
        contentColor = colors.primaryForeground,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(role = Role.Button) { open = true },
    )

    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        if (!userName.isNullOrBlank()) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(userName, style = type.bodyMedium, color = colors.foreground)
            }
            HorizontalDivider(color = colors.border)
        }

        // `getUserMenuItems("platform")`, in its order.
        val items = buildList {
            add(R.string.account_profile to "/profile")
            add(R.string.account_my_account to "/account")
            if (role == UserRole.ADMIN || role == UserRole.DEVELOPER) {
                add(R.string.account_school_settings to "/admin/settings")
            }
            add(R.string.account_help to "/help")
        }
        items.forEach { (label, href) ->
            DropdownMenuItem(
                text = { Text(stringResource(label), style = type.body, color = colors.foreground) },
                onClick = {
                    open = false
                    onOpenHref(href)
                },
            )
        }

        HorizontalDivider(color = colors.border)
        DropdownMenuItem(
            text = { Text(stringResource(R.string.account_logout), style = type.body, color = colors.destructive) },
            onClick = {
                open = false
                onLogout()
            },
        )
    }
}
