package org.hogwarts.android.feature.fees.domain

import org.hogwarts.android.feature.fees.data.remote.InvoiceListResponse
import org.hogwarts.android.feature.fees.testing.overdueStudent
import org.hogwarts.android.feature.fees.testing.settledGuardian
import org.hogwarts.android.feature.fees.testing.unbilledFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyMoneyTest {

    @Test
    fun `fees group the instalments and keep the server totals`() {
        val money = overdueStudent()
        assertEquals(listOf("fee-1", "fee-bus"), money.fees.map { it.id })
        val tuition = money.fees.first()
        assertEquals(5000.0, tuition.total, 0.0)
        assertEquals(1650.0, tuition.paid, 0.0)
        assertEquals(3350.0, tuition.remaining, 0.0)
        assertEquals(3350.0, money.totals.remaining, 0.0)
        assertEquals("2026-09-01", money.installments[1].dueDate)
    }

    @Test
    fun `due is the unpaid part in order, and the first of it is next`() {
        val money = overdueStudent()
        assertEquals(listOf("inv-2", "inv-3", "inv-4"), money.due.map { it.id })
        assertEquals("inv-2", money.nextDue?.id)
        assertTrue(money.isOverdue)
        assertFalse(money.settled)
        assertEquals(40, money.progress) // 2250 / 5600 = 40.2%
        assertTrue(money.showsFullBalanceNote)
    }

    @Test
    fun `pay follows the web dialog - an owed fee and a payable rail`() {
        assertTrue(overdueStudent().canPay("fee-1"))
        assertFalse(overdueStudent().canPay("fee-bus"))
        assertFalse(overdueStudent(methods = listOf("cash", "bank_transfer")).canPay("fee-1"))
        assertTrue(overdueStudent(methods = listOf("cashi")).canPay("fee-1"))
    }

    @Test
    fun `payable rails put redirects before wallets and drop office rails`() {
        assertEquals(
            listOf(Gateway.TAP, Gateway.BANKAK, Gateway.CASHI),
            payableGateways(listOf(Gateway.BANKAK, Gateway.CASH, Gateway.TAP, Gateway.CASHI, Gateway.BANK_TRANSFER)),
        )
    }

    @Test
    fun `settled and unbilled families read differently`() {
        val settled = settledGuardian()
        assertTrue(settled.settled)
        assertNull(settled.nextDue)
        assertEquals(listOf("Omar Nasser", "Sara Nasser"), settled.studentNames)

        val unbilled = unbilledFamily()
        assertTrue(unbilled.fees.isEmpty())
        assertEquals(0, unbilled.progress)
    }

    @Test
    fun `no totals means no family`() {
        assertNull(FamilyMoney.from(InvoiceListResponse(), emptyList()))
    }
}
