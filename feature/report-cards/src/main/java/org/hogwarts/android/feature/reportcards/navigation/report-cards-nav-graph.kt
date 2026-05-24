package org.hogwarts.android.feature.reportcards.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.reportcards.ui.ProgressChartsScreen
import org.hogwarts.android.feature.reportcards.ui.ReportCardDetailScreen
import org.hogwarts.android.feature.reportcards.ui.ReportCardsListScreen

@Serializable data object ReportCardsList
@Serializable data class ReportCardDetail(val reportCardId: String)
@Serializable data object ReportCardsProgress

/**
 * Report cards list screen navigation entry.
 */
fun NavGraphBuilder.reportCardsListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToProgress: () -> Unit
) {
    composable<ReportCardsList> {
        ReportCardsListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail
        )
    }
}

/**
 * Report card detail screen navigation entry.
 */
fun NavGraphBuilder.reportCardDetailScreen(
    onNavigateBack: () -> Unit
) {
    composable<ReportCardDetail> {
        ReportCardDetailScreen(
            onNavigateBack = onNavigateBack
        )
    }
}

/**
 * Progress charts screen navigation entry.
 */
fun NavGraphBuilder.progressChartsScreen(
    onNavigateBack: () -> Unit
) {
    composable<ReportCardsProgress> {
        ProgressChartsScreen(
            onNavigateBack = onNavigateBack
        )
    }
}
