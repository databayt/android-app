package org.hogwarts.android.feature.fees.testing

import org.hogwarts.android.core.data.tenant.CurrentUser
import org.hogwarts.android.core.data.tenant.SessionManager
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.fees.data.remote.InvoiceDto
import org.hogwarts.android.feature.fees.data.remote.InvoiceListResponse
import org.hogwarts.android.feature.fees.data.remote.PaymentDto
import org.hogwarts.android.feature.fees.data.remote.StaffDashboardDto
import org.hogwarts.android.feature.fees.data.remote.TotalsDto
import org.hogwarts.android.feature.fees.data.repository.CheckoutResult
import org.hogwarts.android.feature.fees.data.repository.FamilyResult
import org.hogwarts.android.feature.fees.data.repository.FeesRepository
import org.hogwarts.android.feature.fees.domain.FamilyMoney
import org.hogwarts.android.feature.fees.domain.Gateway

/*
 * Fictional families only — android-app is a public repository. Names, ids,
 * invoice numbers and amounts here are invented.
 */

fun invoice(
    id: String,
    fee: String = "fee-1",
    feeName: String = "Tuition",
    student: String = "Layla Haddad",
    number: Int = 1,
    count: Int = 1,
    due: String? = "2026-10-01T00:00:00.000Z",
    amount: Double = 1000.0,
    paid: Double = 0.0,
    status: String = "PENDING",
    invoiceNo: String? = "INV-0001",
    share: String? = null,
) = InvoiceDto(
    id = id,
    invoiceNo = invoiceNo,
    feeAssignmentId = fee,
    feeName = feeName,
    studentId = "stu-" + student.lowercase().substringBefore(' '),
    studentName = student,
    academicYear = "2026-2027",
    installmentNumber = number,
    installmentCount = count,
    dueDate = due,
    amount = amount,
    paidAmount = paid,
    remaining = (amount - paid).coerceAtLeast(0.0),
    currency = "SDG",
    status = status,
    shareUrl = share,
)

fun payment(id: String, fee: String = "fee-1", amount: Double = 500.0, status: String = "SUCCESS", method: String = "BANK_TRANSFER") = PaymentDto(
    id = id,
    feeAssignmentId = fee,
    paymentNumber = "PAY-${id.uppercase()}",
    receiptNumber = "RCP-${id.uppercase()}",
    amount = amount,
    currency = "SDG",
    paymentDate = "2026-09-02T09:15:00.000Z",
    paymentMethod = method,
    status = status,
    feeName = "Tuition",
    studentId = "stu-layla",
    studentName = "Layla Haddad",
    academicYear = "2026-2027",
)

/** A student with a four-instalment tuition (one overdue, one partly paid) and a paid bus fee. */
fun overdueStudent(methods: List<String> = listOf("stripe", "bankak", "cash")): FamilyMoney = FamilyMoney.from(
    InvoiceListResponse(
        data = listOf(
            invoice("inv-1", number = 1, count = 4, due = "2026-08-01T00:00:00.000Z", amount = 1250.0, paid = 1250.0, status = "PAID", invoiceNo = "INV-0101"),
            invoice("inv-2", number = 2, count = 4, due = "2026-09-01T00:00:00.000Z", amount = 1250.0, status = "OVERDUE", invoiceNo = "INV-0102", share = "https://demo.example.test/en/invoice/tok-2"),
            invoice("inv-3", number = 3, count = 4, due = "2026-10-01T00:00:00.000Z", amount = 1250.0, paid = 400.0, status = "PARTIAL", invoiceNo = "INV-0103"),
            invoice("inv-4", number = 4, count = 4, due = "2026-11-01T00:00:00.000Z", amount = 1250.0, status = "PENDING", invoiceNo = "INV-0104"),
            invoice("fee-bus", fee = "fee-bus", feeName = "School bus", due = null, amount = 600.0, paid = 600.0, status = "PAID", invoiceNo = null),
        ),
        total = 5,
        currency = "SDG",
        totals = TotalsDto(billed = 5600.0, paid = 2250.0, pendingVerification = 300.0, remaining = 3350.0, overdue = 1250.0),
        methods = methods,
    ),
    listOf(
        payment("p1", amount = 1650.0, method = "CASH"),
        payment("p2", fee = "fee-bus", amount = 600.0),
        payment("p3", amount = 300.0, status = "PENDING_VERIFICATION", method = "BANKAK"),
    ),
)!!

/** A guardian of two children with everything paid. */
fun settledGuardian(): FamilyMoney = FamilyMoney.from(
    InvoiceListResponse(
        data = listOf(
            invoice("fee-a", fee = "fee-a", student = "Omar Nasser", due = null, amount = 900.0, paid = 900.0, status = "PAID", invoiceNo = null),
            invoice("fee-b", fee = "fee-b", student = "Sara Nasser", due = null, amount = 900.0, paid = 900.0, status = "PAID", invoiceNo = null),
        ),
        total = 2,
        currency = "SDG",
        totals = TotalsDto(billed = 1800.0, paid = 1800.0, remaining = 0.0),
        methods = listOf("bankak", "cashi", "cash", "bank_transfer"),
    ),
    listOf(payment("g1", fee = "fee-a", amount = 900.0), payment("g2", fee = "fee-b", amount = 900.0)),
)!!

/** A family the school has not billed yet. */
fun unbilledFamily(): FamilyMoney = FamilyMoney.from(
    InvoiceListResponse(currency = "SDG", totals = TotalsDto(), methods = listOf("cash")),
    emptyList(),
)!!

class FakeFeesRepository : FeesRepository {
    var cached: FamilyMoney? = null
    var family: FamilyResult = FamilyResult.Fresh(overdueStudent())
    var staffStats: StaffDashboardDto? = null
    var checkoutResult: CheckoutResult = CheckoutResult.Ready("https://checkout.example.test/session/fake-1")
    val checkouts = mutableListOf<Triple<String, Gateway, String>>()
    var familyCalls = 0

    override suspend fun cachedFamily(lang: String) = cached
    override suspend fun family(lang: String): FamilyResult {
        familyCalls++
        return family
    }
    override suspend fun staff() = staffStats
    override suspend fun checkout(feeAssignmentId: String, gateway: Gateway, lang: String): CheckoutResult {
        checkouts += Triple(feeAssignmentId, gateway, lang)
        return checkoutResult
    }
}

class FakeSessionManager(role: UserRole) : SessionManager {
    override var currentUser: CurrentUser? = CurrentUser("u1", "u1@school.test", "school-1", role, "Test", "User")
    override val isAuthenticated: Boolean get() = currentUser != null
    override suspend fun setUser(user: CurrentUser) { currentUser = user }
    override suspend fun clearSession() { currentUser = null }
}

fun tenant(role: UserRole) = TenantContext(FakeSessionManager(role))
