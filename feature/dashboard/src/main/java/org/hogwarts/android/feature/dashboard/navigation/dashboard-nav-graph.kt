package org.hogwarts.android.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.dashboard.ui.DashboardScreen

@Serializable data object Dashboard

/**
 * Dashboard destination. Every door on it is a web path (`/attendance`,
 * `/finance/invoice`…) resolved by the app shell to a native screen or a web
 * handoff, so the dashboard stays a literal mirror of the server's links.
 */
fun NavGraphBuilder.dashboardScreen(onOpenHref: (href: String) -> Unit) {
    composable<Dashboard> {
        DashboardScreen(onOpenHref = onOpenHref)
    }
}
