package org.hogwarts.android.feature.fees.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.kit.BadgeVariant
import org.hogwarts.android.core.designsystem.kit.BrandProgress
import org.hogwarts.android.core.designsystem.kit.LabelBadge
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.kit.ProgressBar
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.theme.BrandColors
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.fees.R
import org.hogwarts.android.feature.fees.domain.FamilyFee
import org.hogwarts.android.feature.fees.domain.FamilyMoney
import org.hogwarts.android.feature.fees.domain.FamilyPayment
import org.hogwarts.android.feature.fees.domain.Gateway
import org.hogwarts.android.feature.fees.domain.Installment
import org.hogwarts.android.feature.fees.domain.InstallmentStatus

/** `rounded-2xl` — Tailwind's 1rem, which the web theme does not override. */
private val Card2xl = RoundedCornerShape(16.dp)

/**
 * `/finance` for a student or a guardian — `finance/family/content.tsx` below
 * `md`: the green balance banner, then (when anything is billed) what is due,
 * every fee with its schedule, the ways to pay and the receipts, 40dp apart.
 */
@Composable
fun FamilyFinanceView(
    state: FamilyUiState,
    onPay: (Installment) -> Unit,
    onOpenInvoice: (Installment) -> Unit,
    onRetry: () -> Unit,
) {
    val money = state.money
    if (money == null) {
        when {
            state.noFamily -> NoPermission()
            state.loadFailed -> LoadFailed(onRetry)
        }
        return
    }
    val colors = HogwartsTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
        if (state.offline) {
            Text(stringResource(R.string.finance_offline), style = HogwartsTheme.type.caption, color = colors.mutedForeground)
        }
        FamilyBalanceBanner(money, onPay)
        if (money.fees.isEmpty()) {
            EmptyBilling()
        } else {
            FamilyDueList(money, onPay, onOpenInvoice)
            FamilyFeeCards(money, onOpenInvoice)
            FamilyPayOptions(money.methods)
            FamilyReceipts(money)
        }
    }
}

/**
 * `FamilyBalanceBanner`: whose money small, the balance bold at the banner's
 * size, the state in the light weight under it, Pay as the white pill, then
 * the paid / billed bar. Overdue is said in words with a warning glyph — the
 * brand ground never turns red.
 */
@Composable
internal fun FamilyBalanceBanner(money: FamilyMoney, onPay: (Installment) -> Unit) {
    val f = financeFormat()
    val type = HogwartsTheme.type
    val totals = money.totals
    val next = money.nextDue
    val state = when {
        money.settled -> stringResource(R.string.family_all_settled)
        money.isOverdue -> "${stringResource(R.string.family_overdue)} · ${f.money(totals.overdue, money.currency)}"
        next?.dueDate != null -> stringResource(R.string.family_due_on, f.longDate(next.dueDate))
        else -> stringResource(R.string.family_outstanding)
    }
    val eyebrow = f.names(money.studentNames).ifBlank { stringResource(R.string.family_title) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Banner)
            .background(BrandColors.Green)
            .padding(horizontal = 32.dp, vertical = 40.dp),
    ) {
        Text(eyebrow, style = type.body, color = BrandColors.Ink.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
        Text(
            f.money(totals.remaining, money.currency),
            style = type.bannerHeadline.copy(fontWeight = FontWeight.Bold),
            color = BrandColors.Ink,
        )
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val glyph = when {
                money.isOverdue -> Icons.Outlined.WarningAmber
                money.settled -> Icons.Outlined.CheckCircle
                else -> null
            }
            if (glyph != null) Icon(glyph, contentDescription = null, tint = BrandColors.Ink, modifier = Modifier.size(20.dp))
            Text(state, style = type.bannerHeadline.copy(fontSize = 18.sp, lineHeight = 28.sp), color = BrandColors.Ink)
        }
        if (next != null && money.canPay(next.feeAssignmentId)) {
            Box(Modifier.padding(top = 28.dp)) {
                PillButton(
                    label = stringResource(R.string.family_pay_now),
                    onClick = { onPay(next) },
                    variant = PillVariant.BrandWhite,
                    icon = Icons.Outlined.CreditCard,
                )
            }
        }
        if (totals.billed > 0) {
            Column(Modifier.padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BrandProgress(money.progress.toFloat())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val ink = BrandColors.Ink.copy(alpha = 0.7f)
                    Text("${f.money(totals.paid, money.currency)} ${stringResource(R.string.family_paid)}", style = type.caption, color = ink)
                    Text("${f.money(totals.billed, money.currency)} ${stringResource(R.string.family_billed)}", style = type.caption, color = ink)
                }
                if (totals.pendingVerification > 0) {
                    Text(
                        "${stringResource(R.string.family_awaiting_verification)} · ${f.money(totals.pendingVerification, money.currency)}",
                        style = type.caption,
                        color = BrandColors.Ink.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

/** `EmptyBilling`: nothing issued yet — distinct from a settled account. */
@Composable
private fun EmptyBilling() {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Card2xl)
            .background(colors.muted)
            .padding(horizontal = 24.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = colors.mutedForeground, modifier = Modifier.size(32.dp))
        Text(stringResource(R.string.family_nothing_billed), style = type.bodyMedium.copy(fontSize = 16.sp, lineHeight = 24.sp), color = colors.foreground, textAlign = TextAlign.Center)
        Text(
            stringResource(R.string.family_nothing_billed_body),
            style = type.body,
            color = colors.mutedForeground,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 384.dp),
        )
    }
}

/** `FamilyDueList`: the instalments still to pay, in due-date order; hidden when none. */
@Composable
private fun FamilyDueList(money: FamilyMoney, onPay: (Installment) -> Unit, onOpenInvoice: (Installment) -> Unit) {
    if (money.due.isEmpty()) return
    Column {
        Heading(stringResource(R.string.family_due_now))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            money.due.forEach { installment ->
                InstallmentRow(
                    installment = installment,
                    currency = money.currency,
                    onOpenInvoice = onOpenInvoice,
                    onPay = if (money.canPay(installment.feeAssignmentId)) ({ onPay(installment) }) else null,
                )
            }
        }
        if (money.showsFullBalanceNote) {
            Text(
                stringResource(R.string.family_full_balance_note),
                style = HogwartsTheme.type.caption,
                color = HogwartsTheme.colors.mutedForeground,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

/** `SectionHeading`: `mb-4 text-lg font-semibold`. */
@Composable
private fun Heading(title: String) {
    SectionHeader(title = title, modifier = Modifier.semantics { heading() })
}

private data class Tone(val ink: Color, val icon: ImageVector)

@Composable
private fun toneOf(status: InstallmentStatus): Tone {
    val colors = HogwartsTheme.colors
    return when (status) {
        InstallmentStatus.PAID -> Tone(colors.positive, Icons.Outlined.CheckCircle)
        InstallmentStatus.PARTIAL -> Tone(colors.warning, Icons.Outlined.Schedule)
        InstallmentStatus.OVERDUE -> Tone(colors.destructive, Icons.Outlined.WarningAmber)
        InstallmentStatus.PENDING, InstallmentStatus.CANCELLED -> Tone(colors.mutedForeground, Icons.Outlined.Schedule)
    }
}

@Composable
private fun statusLabel(status: InstallmentStatus): String = when (status) {
    InstallmentStatus.PAID -> stringResource(R.string.family_status_paid)
    InstallmentStatus.PARTIAL -> stringResource(R.string.family_status_partial)
    InstallmentStatus.PENDING -> stringResource(R.string.family_status_pending)
    InstallmentStatus.OVERDUE -> stringResource(R.string.family_status_overdue)
    InstallmentStatus.CANCELLED -> stringResource(R.string.family_status_cancelled)
}

/**
 * `FamilyInstallmentRow` at phone width: the status circle beside the label,
 * due line and invoice number; under them the amount with its status chip at
 * the start and Pay at the end.
 */
@Composable
private fun InstallmentRow(
    installment: Installment,
    currency: String,
    onOpenInvoice: (Installment) -> Unit,
    onPay: (() -> Unit)?,
    nested: Boolean = false,
) {
    val f = financeFormat()
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val i = installment
    val tone = toneOf(i.status)
    val muted = i.status == InstallmentStatus.PENDING || i.status == InstallmentStatus.CANCELLED
    val label = if (i.count > 1) stringResource(R.string.family_installment_of, f.latin(i.number), f.latin(i.count)) else i.feeName
    val due = when {
        i.dueDate == null -> stringResource(R.string.family_no_due_date)
        i.status == InstallmentStatus.OVERDUE -> stringResource(R.string.family_overdue_since, f.shortDate(i.dueDate))
        else -> stringResource(R.string.family_due_on, f.shortDate(i.dueDate))
    }
    val circleBg = if (muted) colors.muted else tone.ink.copy(alpha = 0.10f)
    val circleBorder = if (muted) colors.border else tone.ink.copy(alpha = 0.30f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(if (nested) HogwartsShapes.Card else Card2xl)
            .background(if (nested) colors.background else colors.muted)
            .padding(if (nested) 12.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(circleBg)
                    .border(1.dp, circleBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(tone.icon, contentDescription = null, tint = tone.ink, modifier = Modifier.size(16.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(label, style = type.bodyMedium.copy(fontSize = 16.sp, lineHeight = 24.sp), color = colors.foreground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (i.count > 1) "${i.feeName} · $due" else due,
                    style = type.caption,
                    color = colors.mutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (i.invoiceNo != null) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Outlined.Description, contentDescription = null, tint = colors.mutedForeground, modifier = Modifier.size(12.dp))
                        if (i.shareUrl != null) {
                            Text(
                                ltr(i.invoiceNo),
                                style = type.caption.copy(textDecoration = TextDecoration.Underline),
                                color = colors.mutedForeground,
                                modifier = Modifier.clickable { onOpenInvoice(i) },
                            )
                        } else {
                            Text(ltr(i.invoiceNo), style = type.caption.copy(fontFamily = FontFamily.Monospace), color = colors.mutedForeground)
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    f.money(if (i.outstanding > 0) i.outstanding else i.amount, currency),
                    style = type.rowTitle,
                    color = colors.foreground,
                )
                LabelBadge(
                    label = statusLabel(i.status),
                    variant = BadgeVariant.Outline,
                    tint = if (muted) null else tone.ink,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (onPay != null) {
                PillButton(
                    label = stringResource(R.string.family_pay_now),
                    onClick = onPay,
                    icon = Icons.Outlined.CreditCard,
                )
            }
        }
    }
}

/** `FamilyFeeCards`: every fee with its progress and, past one date, its schedule. */
@Composable
private fun FamilyFeeCards(money: FamilyMoney, onOpenInvoice: (Installment) -> Unit) {
    val showStudent = money.studentNames.size > 1
    Column {
        Heading(stringResource(R.string.family_fees))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            money.fees.forEach { fee -> FeeCard(fee, money.currency, showStudent, onOpenInvoice) }
        }
    }
}

@Composable
private fun FeeCard(fee: FamilyFee, currency: String, showStudent: Boolean, onOpenInvoice: (Installment) -> Unit) {
    val f = financeFormat()
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val progress = if (fee.total > 0) kotlin.math.floor(fee.paid / fee.total * 100 + 0.5).toInt().coerceAtMost(100) else 0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Card2xl)
            .background(colors.muted)
            .padding(20.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(fee.feeName, style = type.bodyMedium.copy(fontSize = 16.sp, lineHeight = 24.sp), color = colors.foreground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (showStudent) "${fee.studentName} · ${fee.academicYear}" else fee.academicYear,
                    style = type.caption,
                    color = colors.mutedForeground,
                )
            }
            Text(f.money(fee.total, currency), style = type.rowTitle, color = colors.foreground)
        }
        Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ProgressBar(progress.toFloat())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${f.money(fee.paid, currency)} ${stringResource(R.string.family_paid)}", style = type.caption, color = colors.mutedForeground)
                Text("${f.money(fee.remaining, currency)} ${stringResource(R.string.family_remaining)}", style = type.caption, color = colors.mutedForeground)
            }
        }
        if (fee.installments.size > 1) {
            Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                fee.installments.forEach { InstallmentRow(it, currency, onOpenInvoice, onPay = null, nested = true) }
            }
        }
    }
}

/** `FamilyPayOptions`: the rails this school actually takes — informational. */
@Composable
private fun FamilyPayOptions(methods: List<Gateway>) {
    if (methods.isEmpty()) return
    Column {
        Heading(stringResource(R.string.family_pay_with))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            methods.forEach { GatewayRow(it) }
        }
    }
}

@Composable
internal fun GatewayRow(gateway: Gateway, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Card2xl)
            .background(colors.muted)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(HogwartsShapes.Card).background(colors.background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(gateway.icon(), contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(stringResource(gateway.labelRes()), style = type.bodyMedium.copy(fontSize = 16.sp, lineHeight = 24.sp), color = colors.foreground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(stringResource(gateway.descriptionRes()), style = type.caption, color = colors.mutedForeground)
        }
        trailing?.invoke()
    }
}

/** `GATEWAY_DISPLAY[gateway].icon` → lucide CreditCard / Smartphone / Wallet / Banknote / Building2. */
private fun Gateway.icon(): ImageVector = when (this) {
    Gateway.STRIPE, Gateway.TAP -> Icons.Outlined.CreditCard
    Gateway.BANKAK -> Icons.Outlined.Smartphone
    Gateway.CASHI -> Icons.Outlined.AccountBalanceWallet
    Gateway.CASH -> Icons.Outlined.Payments
    Gateway.BANK_TRANSFER -> Icons.Outlined.AccountBalance
}

private fun Gateway.labelRes(): Int = when (this) {
    Gateway.STRIPE -> R.string.gateway_stripe_label
    Gateway.TAP -> R.string.gateway_tap_label
    Gateway.BANKAK -> R.string.gateway_bankak_label
    Gateway.CASHI -> R.string.gateway_cashi_label
    Gateway.CASH -> R.string.gateway_cash_label
    Gateway.BANK_TRANSFER -> R.string.gateway_bank_transfer_label
}

private fun Gateway.descriptionRes(): Int = when (this) {
    Gateway.STRIPE -> R.string.gateway_stripe_description
    Gateway.TAP -> R.string.gateway_tap_description
    Gateway.BANKAK -> R.string.gateway_bankak_description
    Gateway.CASHI -> R.string.gateway_cashi_description
    Gateway.CASH -> R.string.gateway_cash_description
    Gateway.BANK_TRANSFER -> R.string.gateway_bank_transfer_description
}

/**
 * `FamilyReceipts`: what has been paid, newest first (20 at most). The web's
 * Receipt PDF (`/api/payment/:id/receipt`) needs a web session the app cannot
 * hand over yet, so the rows carry no download.
 */
@Composable
private fun FamilyReceipts(money: FamilyMoney) {
    if (money.payments.isEmpty()) return
    val f = financeFormat()
    Column {
        Heading(stringResource(R.string.family_receipts))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            money.payments.take(20).forEach { p ->
                ReceiptRow(p, money.currency, f)
            }
        }
    }
}

@Composable
private fun ReceiptRow(p: FamilyPayment, currency: String, f: FinanceFormat) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Card2xl)
            .background(colors.muted)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column {
            Text(p.feeName, style = type.bodyMedium.copy(fontSize = 16.sp, lineHeight = 24.sp), color = colors.foreground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${ltr(p.paymentNumber)} · ${f.shortDate(p.paymentDate)} · ${ltr(p.paymentMethod.replace('_', ' '))}",
                style = type.caption,
                color = colors.mutedForeground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column {
            Text(f.money(p.amount, currency), style = type.rowTitle, color = colors.foreground)
            if (p.status != "SUCCESS") {
                LabelBadge(
                    label = when (p.status) {
                        "PENDING_VERIFICATION" -> stringResource(R.string.family_payment_pending_verification)
                        "PENDING" -> stringResource(R.string.family_payment_pending)
                        else -> stringResource(R.string.family_awaiting_verification)
                    },
                    variant = BadgeVariant.Secondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
internal fun NoPermission() {
    Text(stringResource(R.string.finance_no_permission_reports), style = HogwartsTheme.type.body.copy(fontSize = 16.sp, lineHeight = 24.sp), color = HogwartsTheme.colors.mutedForeground)
}

@Composable
private fun LoadFailed(onRetry: () -> Unit) {
    val colors = HogwartsTheme.colors
    Column(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.finance_error), style = HogwartsTheme.type.body, color = colors.mutedForeground)
        Spacer(Modifier.height(12.dp))
        PillButton(stringResource(R.string.finance_retry), onClick = onRetry, variant = PillVariant.Muted)
    }
}
