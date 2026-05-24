# Epic E16: Fees, Payments & Finance

**Epic ID:** EPIC-PROD-16
**Title:** Fees, Payments & Finance
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P1
**Sprint:** 10-12
**Total Points:** 47

---

## 1. Overview

Full fee + invoice + payment + scholarship + fine flow with Stripe SDK integration. Closes `payment-receipt-view-model.kt:54` PDF stub.

### Success Criteria
- [ ] Guardian can pay outstanding invoices via Stripe.
- [ ] Receipt PDF downloads via WorkManager + DownloadManager.
- [ ] Tenant currency honored (E07.S02).
- [ ] Scholarships + fines work end-to-end.

---

## 2. Stories

| ID | Title | Pts | Sprint | Notes |
|---|---|---:|---|---|
| E16.S01 | Fee balance dashboard | 3 | 10 | |
| E16.S02 | Invoice list with filters (paid/unpaid/overdue) | 3 | 10 | |
| E16.S03 | Invoice detail + line items + scholarships applied | 3 | 10 | |
| E16.S04 | **Stripe payment flow** — net new (Stripe SDK not in current deps) | 13 | 11 | |
| E16.S05 | Payment receipt PDF — fixes `payment-receipt-view-model.kt:54` TODO | 5 | 11 | |
| E16.S06 | Transaction history | 3 | 11 | |
| E16.S07 | Scholarship application + status | 5 | 11 | |
| E16.S08 | Fine list + pay | 3 | 12 | |
| E16.S09 | Multi-currency support per tenant | 3 | 12 | Depends on E07.S02 |
| E16.S10 | Payroll view (staff) | 3 | 12 | |
| E16.S11 | Tests | 3 | 12 | |

### Detailed AC: E16.S04 — Stripe payment
- [ ] Add `com.stripe:stripe-android:21.x` to `libs.versions.toml`.
- [ ] On pay tap: client calls `POST /api/mobile/payments` (E08.S16) → server creates Stripe `PaymentIntent` → returns `clientSecret`.
- [ ] Client launches `PaymentSheet` with `clientSecret`.
- [ ] On success: optimistic UI shows "Paid"; server webhook confirms; UI updates with confirmed state.
- [ ] Failure modes: card declined, insufficient funds, 3DS challenge — each handled with localized message.
- [ ] Currency from tenant settings (E07.S02).
- [ ] Apple Pay / Google Pay supported via Stripe's PaymentSheet.

### Detailed AC: E16.S05 — Receipt PDF
- [ ] On payment success, `PaymentReceiptViewModel.downloadReceipt()` enqueues `ReceiptDownloadWorker`.
- [ ] Worker calls `GET /api/mobile/payments/{id}/receipt-pdf`.
- [ ] Saves to `Downloads/Hogwarts/Receipts/{tenantId}/{invoiceId}.pdf` via `DownloadManager`.
- [ ] Notification on completion with "Open" action.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E08.S16 fees + payments endpoints | Required |
| E07.S02 tenant currency | Required |
| Stripe Android SDK 21.x — new dependency | Add to `libs.versions.toml` |
| Existing `feature/fees/` (32 files) skeleton | Available |

---

## 4. DoD

- [ ] All 11 stories merged.
- [ ] Maestro flow: guardian pays $100 invoice → server confirms → receipt PDF downloads → opens.
- [ ] Stripe test cards work for all major decline reasons.
- [ ] Currency: change tenant currency to SAR → fees screen reflects.
- [ ] Captain signoff.
