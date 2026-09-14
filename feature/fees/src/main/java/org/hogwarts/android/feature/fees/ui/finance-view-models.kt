package org.hogwarts.android.feature.fees.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.fees.data.remote.StaffDashboardDto
import org.hogwarts.android.feature.fees.data.repository.CheckoutResult
import org.hogwarts.android.feature.fees.data.repository.FamilyResult
import org.hogwarts.android.feature.fees.data.repository.FeesRepository
import org.hogwarts.android.feature.fees.domain.FamilyMoney
import org.hogwarts.android.feature.fees.domain.Gateway
import org.hogwarts.android.feature.fees.domain.Installment
import org.hogwarts.android.feature.fees.domain.payableGateways
import javax.inject.Inject

/** Which `/finance` a role gets — `finance/page.tsx`. */
enum class FinanceLanding { Family, Staff, Denied }

internal fun landingFor(role: UserRole?): FinanceLanding = when (role) {
    UserRole.STUDENT, UserRole.GUARDIAN -> FinanceLanding.Family
    // The hub's reports gate: DEVELOPER always, ADMIN / ACCOUNTANT by role.
    // Granular grants to other roles are not visible to the app.
    UserRole.DEVELOPER, UserRole.ADMIN, UserRole.ACCOUNTANT -> FinanceLanding.Staff
    else -> FinanceLanding.Denied
}

/** `getFinanceRootTabs()` in `finance/permissions.ts` (visible tabs only). */
enum class FinanceTab(val href: String) {
    Overview("/finance"), Invoice("/finance/invoice"), Banking("/finance/banking"), Fees("/finance/fees"),
    Salary("/finance/salary"), Payroll("/finance/payroll"), Reports("/finance/reports"),
}

internal fun financeTabs(role: UserRole?): List<FinanceTab> =
    if (role == UserRole.DEVELOPER || role == UserRole.ADMIN || role == UserRole.ACCOUNTANT) {
        FinanceTab.entries
    } else {
        listOf(FinanceTab.Overview, FinanceTab.Fees)
    }

@HiltViewModel
class FinanceViewModel @Inject constructor(tenantContext: TenantContext) : ViewModel() {
    val role: UserRole? = tenantContext.userRole
    val landing: FinanceLanding = landingFor(role)
}

/** Something only the screen can do: open a hosted page or a web path. */
sealed interface FinanceEvent {
    /** An absolute https URL (hosted checkout, published invoice) for a Custom Tab. */
    data class OpenUrl(val url: String) : FinanceEvent
    /** A web path the shell hands off (wallet rails, deeper finance pages). */
    data class OpenHref(val href: String) : FinanceEvent
}

/** The Pay dialog for one fee — `PayFeeDialog` + `GatewayPicker`. */
data class PaySheet(
    val feeAssignmentId: String,
    val label: String,
    val gateways: List<Gateway>,
    val loading: Gateway? = null,
    val failed: Boolean = false,
)

data class FamilyUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val offline: Boolean = false,
    val loadFailed: Boolean = false,
    val money: FamilyMoney? = null,
    /** The server resolved no student for this account — the web falls through to the hub's denial. */
    val noFamily: Boolean = false,
    val paySheet: PaySheet? = null,
)

@HiltViewModel
class FamilyFinanceViewModel @Inject constructor(
    private val repository: FeesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyUiState())
    val uiState: StateFlow<FamilyUiState> = _uiState.asStateFlow()

    private val _events = Channel<FinanceEvent>(Channel.BUFFERED)
    val events: Flow<FinanceEvent> = _events.receiveAsFlow()

    private var lang: String? = null
    private var awaitingCheckout = false

    /** Load for the reader's language; fee names come back localized. */
    fun start(lang: String) {
        if (this.lang == lang) return
        this.lang = lang
        viewModelScope.launch {
            repository.cachedFamily(lang)?.let { cached -> _uiState.update { it.copy(money = cached, loading = false) } }
            load(refreshing = false)
        }
    }

    fun refresh() {
        viewModelScope.launch { load(refreshing = true) }
    }

    /** Back from the hosted checkout: the webhook records the payment, so read the balance again. */
    fun onResume() {
        if (!awaitingCheckout) return
        awaitingCheckout = false
        refresh()
    }

    fun openPay(installment: Installment) {
        val money = _uiState.value.money ?: return
        if (!money.canPay(installment.feeAssignmentId)) return
        _uiState.update {
            it.copy(
                paySheet = PaySheet(
                    feeAssignmentId = installment.feeAssignmentId,
                    label = "${installment.feeName} · ${installment.academicYear}",
                    gateways = payableGateways(money.methods),
                ),
            )
        }
    }

    fun dismissPay() = _uiState.update { it.copy(paySheet = null) }

    fun choose(gateway: Gateway) {
        val sheet = _uiState.value.paySheet ?: return
        if (sheet.loading != null) return
        if (gateway.wallet) {
            // The Bankak / Cashi transfer + proof dialog has no mobile route: the web page carries it.
            _uiState.update { it.copy(paySheet = null) }
            _events.trySend(FinanceEvent.OpenHref(WALLET_HREF))
            return
        }
        _uiState.update { it.copy(paySheet = sheet.copy(loading = gateway, failed = false)) }
        viewModelScope.launch {
            when (val result = repository.checkout(sheet.feeAssignmentId, gateway, lang ?: "ar")) {
                is CheckoutResult.Ready -> {
                    awaitingCheckout = true
                    _uiState.update { it.copy(paySheet = null) }
                    _events.send(FinanceEvent.OpenUrl(result.url))
                }
                is CheckoutResult.Refused, is CheckoutResult.Failed ->
                    _uiState.update { state -> state.copy(paySheet = state.paySheet?.copy(loading = null, failed = true)) }
            }
        }
    }

    fun openInvoice(installment: Installment) {
        installment.shareUrl?.takeIf { it.startsWith("https://") }?.let { _events.trySend(FinanceEvent.OpenUrl(it)) }
    }

    private suspend fun load(refreshing: Boolean) {
        val lang = lang ?: return
        _uiState.update { it.copy(refreshing = refreshing) }
        when (val result = repository.family(lang)) {
            is FamilyResult.Fresh -> _uiState.update {
                it.copy(loading = false, refreshing = false, offline = false, loadFailed = false, money = result.money, noFamily = result.money == null)
            }
            is FamilyResult.Cached -> _uiState.update {
                it.copy(loading = false, refreshing = false, offline = true, loadFailed = false, money = result.money)
            }
            is FamilyResult.Failed -> _uiState.update {
                it.copy(loading = false, refreshing = false, loadFailed = it.money == null)
            }
        }
    }

    companion object {
        /** The web family table whose Pay dialog carries the wallet transfer proof. */
        const val WALLET_HREF = "/finance/fees/my"
    }
}

data class StaffUiState(
    val loading: Boolean = true,
    val role: UserRole? = null,
    /** Present only for an ACCOUNTANT — the one staff role the dashboard route gives money figures. */
    val stats: StaffDashboardDto? = null,
)

@HiltViewModel
class StaffFinanceViewModel @Inject constructor(
    private val repository: FeesRepository,
    tenantContext: TenantContext,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffUiState(role = tenantContext.userRole))
    val uiState: StateFlow<StaffUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val stats = if (_uiState.value.role == UserRole.ACCOUNTANT) repository.staff()?.takeIf { it.collectedToday != null } else null
            _uiState.update { it.copy(loading = false, stats = stats) }
        }
    }
}
