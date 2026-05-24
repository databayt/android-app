package org.hogwarts.android.feature.idcard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.idcard.ui.DigitalIdScreen
import org.hogwarts.android.feature.idcard.ui.IdCardPdfScreen
import org.hogwarts.android.feature.idcard.ui.WalletModeScreen

@Serializable data object IdCard
@Serializable data object IdCardWallet
@Serializable data object IdCardPdf

fun NavGraphBuilder.digitalIdScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWalletMode: () -> Unit,
    onNavigateToPdf: () -> Unit
) {
    composable<IdCard> {
        DigitalIdScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToWalletMode = onNavigateToWalletMode,
            onNavigateToPdf = onNavigateToPdf
        )
    }
}

fun NavGraphBuilder.walletModeScreen(
    onNavigateBack: () -> Unit
) {
    composable<IdCardWallet> {
        WalletModeScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.idCardPdfScreen(
    onNavigateBack: () -> Unit
) {
    composable<IdCardPdf> {
        IdCardPdfScreen(onNavigateBack = onNavigateBack)
    }
}
