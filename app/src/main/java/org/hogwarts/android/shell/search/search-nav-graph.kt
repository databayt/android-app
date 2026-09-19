package org.hogwarts.android.shell.search

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable data object Search

/**
 * The search destination. It draws its own input row where the shell header
 * would be — the web's palette is a sheet over the page with its own field,
 * not a page under the platform header.
 */
fun NavGraphBuilder.searchScreen(
    onOpen: (href: String) -> Unit,
    onBack: () -> Unit,
) {
    composable<Search> {
        val viewModel: SearchViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        SearchScreen(
            role = state.role,
            enabledModules = state.enabledModules,
            recentIds = state.recentIds,
            onOpen = { item ->
                viewModel.remember(item)
                onOpen(item.href)
            },
            onBack = onBack,
        )
    }
}
