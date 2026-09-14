package org.hogwarts.android.feature.fees.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.fees.ui.FeesScreen

/** `/finance` — the menu key "finance" resolves here. */
@Serializable data object Fees

/**
 * The finance section. [onOpenHref] receives web paths (`/finance/invoice`,
 * `/finance/fees/my`…) for the shell to resolve: only `/finance` itself is
 * native, so every deeper path should hand off to the web.
 */
fun NavGraphBuilder.feesGraph(onOpenHref: (href: String) -> Unit) {
    composable<Fees> {
        FeesScreen(onOpenHref = onOpenHref)
    }
}
