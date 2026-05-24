package org.hogwarts.android.feature.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.profile.ui.ProfileDetailScreen
import org.hogwarts.android.feature.profile.ui.ProfileScreen

@Serializable
data object Profile

/**
 * Detail route for viewing another user's profile by id.
 * Mirrors web `[lang]/s/[subdomain]/profile/[id]`.
 */
@Serializable
data class ProfileDetail(val userId: String)

fun NavGraphBuilder.profileScreen(
    onNavigateBack: () -> Unit
) {
    composable<Profile> {
        ProfileScreen(onNavigateBack = onNavigateBack)
    }

    composable<ProfileDetail> {
        ProfileDetailScreen(onNavigateBack = onNavigateBack)
    }
}
