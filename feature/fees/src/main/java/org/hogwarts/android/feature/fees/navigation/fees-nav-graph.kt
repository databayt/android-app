package org.hogwarts.android.feature.fees.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.fees.ui.FeeBalanceDashboardScreen
import org.hogwarts.android.feature.fees.ui.FeesScreen
import org.hogwarts.android.feature.fees.ui.InvoiceDetailScreen
import org.hogwarts.android.feature.fees.ui.InvoiceListScreen
import org.hogwarts.android.feature.fees.ui.PaymentProcessingScreen
import org.hogwarts.android.feature.fees.ui.PaymentReceiptScreen
import org.hogwarts.android.feature.fees.ui.TransactionHistoryScreen

@Serializable data object Fees
@Serializable data object FeeInvoices
@Serializable data class FeeInvoiceDetail(val invoiceId: String)
@Serializable data class FeePayment(val invoiceId: String)
@Serializable data class FeeReceipt(val transactionId: String)
@Serializable data object FeeTransactions
@Serializable data object FeeBalance

fun NavGraphBuilder.feesScreen(
    onNavigateBack: () -> Unit
) {
    composable<Fees> {
        FeesScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.invoiceListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInvoice: (invoiceId: String) -> Unit,
    onNavigateToTransactions: () -> Unit
) {
    composable<FeeInvoices> {
        InvoiceListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToInvoice = onNavigateToInvoice,
            onNavigateToTransactions = onNavigateToTransactions
        )
    }
}

fun NavGraphBuilder.invoiceDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (invoiceId: String) -> Unit
) {
    composable<FeeInvoiceDetail> {
        InvoiceDetailScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToPayment = onNavigateToPayment
        )
    }
}

fun NavGraphBuilder.paymentProcessingScreen(
    onNavigateBack: () -> Unit,
    onPaymentComplete: (transactionId: String) -> Unit
) {
    composable<FeePayment> {
        PaymentProcessingScreen(
            onNavigateBack = onNavigateBack,
            onPaymentComplete = onPaymentComplete
        )
    }
}

fun NavGraphBuilder.paymentReceiptScreen(
    onNavigateBack: () -> Unit
) {
    composable<FeeReceipt> {
        PaymentReceiptScreen(
            onNavigateBack = onNavigateBack
        )
    }
}

fun NavGraphBuilder.transactionHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReceipt: (transactionId: String) -> Unit
) {
    composable<FeeTransactions> {
        TransactionHistoryScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToReceipt = onNavigateToReceipt
        )
    }
}

fun NavGraphBuilder.feeBalanceDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToPayment: () -> Unit
) {
    composable<FeeBalance> {
        FeeBalanceDashboardScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToInvoices = onNavigateToInvoices,
            onNavigateToTransactions = onNavigateToTransactions,
            onNavigateToPayment = onNavigateToPayment
        )
    }
}
