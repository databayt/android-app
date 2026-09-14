package org.hogwarts.android.feature.fees.ui

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.fees.data.remote.StaffDashboardDto
import org.hogwarts.android.feature.fees.data.repository.CheckoutResult
import org.hogwarts.android.feature.fees.data.repository.FamilyResult
import org.hogwarts.android.feature.fees.domain.Gateway
import org.hogwarts.android.feature.fees.testing.FakeFeesRepository
import org.hogwarts.android.feature.fees.testing.overdueStudent
import org.hogwarts.android.feature.fees.testing.settledGuardian
import org.hogwarts.android.feature.fees.testing.tenant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeFeesRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `each role lands where finance page tsx sends it`() {
        listOf(UserRole.STUDENT, UserRole.GUARDIAN).forEach { assertEquals(FinanceLanding.Family, landingFor(it)) }
        listOf(UserRole.ADMIN, UserRole.ACCOUNTANT, UserRole.DEVELOPER).forEach { assertEquals(FinanceLanding.Staff, landingFor(it)) }
        listOf(UserRole.TEACHER, UserRole.STAFF, UserRole.USER, UserRole.UNKNOWN, null).forEach { assertEquals(FinanceLanding.Denied, landingFor(it)) }
    }

    @Test
    fun `finance officers get the full tab row, everyone else overview and fees`() {
        assertEquals(FinanceTab.entries, financeTabs(UserRole.ACCOUNTANT))
        assertEquals(listOf(FinanceTab.Overview, FinanceTab.Fees), financeTabs(UserRole.GUARDIAN))
        assertEquals(listOf(FinanceTab.Overview, FinanceTab.Fees), financeTabs(UserRole.TEACHER))
    }

    @Test
    fun `family loads the cache first, then the network`() = runTest(dispatcher) {
        repository.cached = settledGuardian()
        val vm = FamilyFinanceViewModel(repository)
        vm.start("en")
        vm.uiState.test {
            assertTrue(awaitItem().loading)
            assertEquals(settledGuardian(), awaitItem().money)
            val fresh = awaitItem()
            assertEquals(overdueStudent(), fresh.money)
            assertFalse(fresh.offline)
        }
    }

    @Test
    fun `no resolvable student reads as the hub denial, a failure offers retry`() = runTest(dispatcher) {
        repository.family = FamilyResult.Fresh(null)
        val vm = FamilyFinanceViewModel(repository)
        vm.start("ar")
        advanceUntilIdle()
        assertTrue(vm.uiState.value.noFamily)

        repository.family = FamilyResult.Failed("offline")
        val failing = FamilyFinanceViewModel(repository)
        failing.start("ar")
        advanceUntilIdle()
        assertTrue(failing.uiState.value.loadFailed)
    }

    @Test
    fun `a redirect rail opens the hosted checkout and refreshes on return`() = runTest(dispatcher) {
        val vm = FamilyFinanceViewModel(repository)
        vm.start("ar")
        advanceUntilIdle()
        val due = vm.uiState.value.money!!.nextDue!!
        vm.openPay(due)
        val sheet = vm.uiState.value.paySheet!!
        assertEquals(listOf(Gateway.STRIPE, Gateway.BANKAK), sheet.gateways)
        assertEquals("Tuition · 2026-2027", sheet.label)

        vm.events.test {
            vm.choose(Gateway.STRIPE)
            assertEquals(FinanceEvent.OpenUrl("https://checkout.example.test/session/fake-1"), awaitItem())
        }
        assertEquals(Triple("fee-1", Gateway.STRIPE, "ar"), repository.checkouts.single())
        assertNull(vm.uiState.value.paySheet)

        val before = repository.familyCalls
        vm.onResume()
        advanceUntilIdle()
        assertEquals(before + 1, repository.familyCalls)
        vm.onResume()
        advanceUntilIdle()
        assertEquals("a second resume without a checkout does not reload", before + 1, repository.familyCalls)
    }

    @Test
    fun `a refused checkout keeps the sheet open with the failure line`() = runTest(dispatcher) {
        repository.checkoutResult = CheckoutResult.Refused(422)
        val vm = FamilyFinanceViewModel(repository)
        vm.start("en")
        advanceUntilIdle()
        vm.openPay(vm.uiState.value.money!!.nextDue!!)
        vm.choose(Gateway.STRIPE)
        advanceUntilIdle()
        val sheet = vm.uiState.value.paySheet!!
        assertTrue(sheet.failed)
        assertNull(sheet.loading)
    }

    @Test
    fun `a wallet rail hands off to the web proof dialog without calling pay`() = runTest(dispatcher) {
        val vm = FamilyFinanceViewModel(repository)
        vm.start("en")
        advanceUntilIdle()
        vm.openPay(vm.uiState.value.money!!.nextDue!!)
        vm.events.test {
            vm.choose(Gateway.BANKAK)
            assertEquals(FinanceEvent.OpenHref("/finance/fees/my"), awaitItem())
        }
        assertTrue(repository.checkouts.isEmpty())
    }

    @Test
    fun `pay does not open for a settled fee`() = runTest(dispatcher) {
        repository.family = FamilyResult.Fresh(settledGuardian())
        val vm = FamilyFinanceViewModel(repository)
        vm.start("en")
        advanceUntilIdle()
        vm.openPay(vm.uiState.value.money!!.installments.first())
        assertNull(vm.uiState.value.paySheet)
    }

    @Test
    fun `only an accountant gets the dashboard figures`() = runTest(dispatcher) {
        repository.staffStats = StaffDashboardDto(role = "ACCOUNTANT", pendingInvoices = 4, pendingAmount = 12000.0, overdueInvoices = 1, overdueAmount = 900.0, collectedToday = 3500.0)
        val accountant = StaffFinanceViewModel(repository, tenant(UserRole.ACCOUNTANT))
        val admin = StaffFinanceViewModel(repository, tenant(UserRole.ADMIN))
        advanceUntilIdle()
        assertEquals(4, accountant.uiState.value.stats?.pendingInvoices)
        assertNull(admin.uiState.value.stats)
        assertFalse(admin.uiState.value.loading)
    }
}
