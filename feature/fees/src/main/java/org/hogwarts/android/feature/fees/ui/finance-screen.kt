package org.hogwarts.android.feature.fees.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.kit.PageNav
import org.hogwarts.android.core.designsystem.kit.PageNavItem
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.fees.R
import timber.log.Timber

/**
 * `/finance` on a phone — `finance/page.tsx`: the section's tabs, then the
 * family's own money for a student or guardian, the finance hub for a finance
 * officer, and the hub's permission line for everyone else. The app shell
 * draws the platform header above it.
 *
 * [onOpenHref] takes web paths (the other tabs, the hub's doors, the wallet
 * rails' proof dialog) for the shell to hand off.
 */
@Composable
fun FeesScreen(
    onOpenHref: (String) -> Unit,
    viewModel: FinanceViewModel = hiltViewModel(),
) {
    when (viewModel.landing) {
        FinanceLanding.Family -> FamilyFinanceRoute(viewModel.role, onOpenHref)
        FinanceLanding.Staff -> StaffFinanceRoute(viewModel.role, onOpenHref)
        FinanceLanding.Denied -> FinancePage(viewModel.role, onOpenHref) { NoPermission() }
    }
}

@Composable
private fun FamilyFinanceRoute(
    role: UserRole?,
    onOpenHref: (String) -> Unit,
    viewModel: FamilyFinanceViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lang = financeFormat().lang
    val context = LocalContext.current

    LaunchedEffect(lang) { viewModel.start(lang) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is FinanceEvent.OpenUrl -> openCustomTab(context, event.url)
                is FinanceEvent.OpenHref -> onOpenHref(event.href)
            }
        }
    }

    FinancePage(role, onOpenHref, refreshing = state.refreshing, onRefresh = viewModel::refresh) {
        FamilyFinanceView(
            state = state,
            onPay = viewModel::openPay,
            onOpenInvoice = viewModel::openInvoice,
            onRetry = viewModel::refresh,
        )
    }
    state.paySheet?.let { sheet -> PayFeeSheet(sheet, onChoose = viewModel::choose, onDismiss = viewModel::dismissPay) }
}

@Composable
private fun StaffFinanceRoute(
    role: UserRole?,
    onOpenHref: (String) -> Unit,
    viewModel: StaffFinanceViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FinancePage(role, onOpenHref) { StaffFinanceView(state, onOpenHref) }
}

/**
 * The page frame: 16dp gutters, the finance tabs, then the content 24dp below
 * (`space-y-6` in `finance/layout.tsx` and `page.tsx`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FinancePage(
    role: UserRole?,
    onOpenHref: (String) -> Unit,
    refreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize().background(HogwartsTheme.colors.background),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 32.dp),
        ) {
            FinanceTabs(role, onOpenHref)
            Spacer(Modifier.height(24.dp))
            content()
        }
    }
}

@Composable
private fun FinanceTabs(role: UserRole?, onOpenHref: (String) -> Unit) {
    PageNav(
        items = financeTabs(role).map { PageNavItem(it.name, stringResource(it.labelRes())) },
        selectedKey = FinanceTab.Overview.name,
        onSelect = { item ->
            val tab = FinanceTab.valueOf(item.key)
            if (tab != FinanceTab.Overview) onOpenHref(tab.href)
        },
    )
}

private fun FinanceTab.labelRes(): Int = when (this) {
    FinanceTab.Overview -> R.string.finance_tab_overview
    FinanceTab.Invoice -> R.string.finance_tab_invoice
    FinanceTab.Banking -> R.string.finance_tab_banking
    FinanceTab.Fees -> R.string.finance_tab_fees
    FinanceTab.Salary -> R.string.finance_tab_salary
    FinanceTab.Payroll -> R.string.finance_tab_payroll
    FinanceTab.Reports -> R.string.finance_tab_reports
}

/** A hosted page (checkout, published invoice) in a Custom Tab — no card data touches the app. */
private fun openCustomTab(context: Context, url: String) {
    val uri = Uri.parse(url)
    try {
        CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, uri)
    } catch (e: ActivityNotFoundException) {
        Timber.w(e, "No Custom Tabs provider; falling back to a browser")
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}
