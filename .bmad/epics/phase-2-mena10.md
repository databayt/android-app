# Phase 2 — MENA-10 Polish

**Release phase:** 2 of 3
**Sprints:** P4, P5, P6, P7, P8 (~10 weeks)
**Target points:** ~411
**Target date:** 2026-09-15
**Status:** Not Started (begins after Phase 1 DoD)
**Owner:** Captain (kun) + Samia (R&D)
**Sibling phases:** [phase-1-pilot-v1.md](./phase-1-pilot-v1.md) · [phase-3-launch.md](./phase-3-launch.md)
**Source plan:** [`~/.claude/plans/take-web-app-as-precious-turtle.md`](../../../.claude/plans/take-web-app-as-precious-turtle.md)

---

## Intent

Scale from 1 pilot to 10 schools. Open the write half of the app (teacher attendance + grading), turn on real-time messaging, ship Stripe fee payment, complete OAuth, and start the test-coverage climb. Admission shipped in Phase 1; remaining E19 surfaces (library, ID card, wallet, quiz) stay deferred to Phase 3.

**MENA-10 success metrics** (from team memory):

- 10 pilot schools onboarded by Aug 31, 2026.
- ≥ 1 paid conversion (King Fahad if not already converted in Phase 1) by Oct 31.
- ≥ 3 schools use the admission flow for Sep 2026 intake.
- Crash-free user rate ≥ 99%.

---

## Story selection by theme

### Foundation cleanup (~25 pts)

Sources: [E01](./epic-prod-01-foundation-hardening.md), [E02](./epic-prod-02-multi-tenancy-rbac.md), [E04](./epic-prod-04-ui-internationalization.md), [E06](./epic-prod-06-database-content-translation.md).

| Story | Pts | Why |
|---|---:|---|
| E01.S08–S12 | ~13 | Detekt `NoHardcodedComposeText` + `RequireTenantScope`, Spotless, Kover, lint promotion |
| E02.S03–S05, S07, S10 | ~17 | Full RBAC ability layer for writes (Action/Subject taxonomy, predicate layer, `@SessionScoped`, Detekt sweep, cross-tenant integration test) |
| E04.S03 / S04 / S07 | ~10 | Server-driven dictionary + per-module string-parity build task + docs |
| E06.S04 / S08 / S09 / S11 / S12 | ~15 | Live translate API endpoint + LRU eviction + Detekt enforcement + full migration of 40–60 display sites + observability dashboard |

### Teacher write surfaces (~80 pts)

Sources: [E12](./epic-prod-12-sis-students-staff-subjects-classes.md), [E13](./epic-prod-13-attendance-behaviour.md), [E14](./epic-prod-14-grading-exams-report-cards.md).

| Story | Pts | Why |
|---|---:|---|
| E13.S01, S02, S04, S05, S10, S11, S12 | ~32 | Single + bulk attendance marking, QR code attendance, excuse workflow, interventions, settings, real-time, tests |
| E14.S01, S02, S03, S07, S10, S11, S12 | ~19 | **Grade entry — fixes the fake `submit-grade-use-case.kt:20` stub**; view, summary, results, report-card view, charts, tests |
| E12.S01, S02, S05, S06, S07, S09, S10, S12, S13 | ~29 | Students + staff + subjects + classes (list, detail, translation sweep, tests) |

### Real-time messaging (~80 pts)

Sources: [E09](./epic-prod-09-real-time-layer.md), [E08](./epic-prod-08-api-contract-endpoints.md), [E18](./epic-prod-18-communications.md).

| Story | Pts | Why |
|---|---:|---|
| E09.S01–S05, S08, S09 | ~36 | Socket.IO server + auth + presence + reconnection + real-time attendance + test harness |
| E08.S18 | 3 | Messaging REST baseline |
| E18.S01–S08, S10, S11, S15–S17 | ~38 | Conversations + chat + typing + presence + attachments + delivery + pin/star + tests |

### Fee payment (~33 pts)

Source: [E16](./epic-prod-16-fees-payments-finance.md).

| Story | Pts | Why |
|---|---:|---|
| E16.S01–S06, S09, S11 | ~33 | Balance, invoice list/detail, **Stripe flow**, receipt PDF, transactions, multi-currency, tests |

### Auth completion (~30 pts)

Sources: [E03](./epic-prod-03-auth-session-hardening.md), [E10](./epic-prod-10-auth-onboarding-profile.md).

| Story | Pts | Why |
|---|---:|---|
| E03.S02, S03, S07 | ~11 | Google OAuth, Facebook OAuth, sign-up flow |
| E10.S02, S03, S04, S06, S07 | ~19 | OTP SMS auto-fill, forgot-password nav, school selector UI, avatar upload, change password |

### Guardian polish (~11 pts)

Source: [E20](./epic-prod-20-role-specific-portals.md).

| Story | Pts | Why |
|---|---:|---|
| E20.S01, S02 | ~11 | Children switcher + per-child views |

### Security + observability + release (~25 pts)

Sources: [E25](./epic-prod-25-security-hardening.md), [E26](./epic-prod-26-observability-crash-reporting.md), [E27](./epic-prod-27-release-pipeline-distribution.md), [E08](./epic-prod-08-api-contract-endpoints.md).

| Story | Pts | Why |
|---|---:|---|
| E25.S03, S04, S05, S07 | ~14 | Play Integrity, RootBeer, server rate-limiting, audit log |
| E26.S05, S09 | ~5 | ANR tracking, shake-to-report |
| E27.S02, S05 | ~6 | Nightly staging build to App Distribution, Play Console API |
| E08.S21 | 5 | MockWebServer contract tests |

### Test infra start (~25 pts)

Source: [E22](./epic-prod-22-test-infrastructure-coverage.md).

| Story | Pts | Why |
|---|---:|---|
| E22.S02, S03, S05 | ~25 | Test harness + 110 unit tests for untested modules + migration tests |

---

## Definition of Done

- [ ] 10 pilot schools onboarded.
- [ ] ≥ 1 paid conversion (King Fahad if not earlier).
- [ ] Teacher attendance + grade-entry verified end-to-end against staging by Ali (QA).
- [ ] Stripe payment in test mode for ≥ 1 school's fee structure.
- [ ] Socket.IO messaging stable; reconnection under flaky network verified.
- [ ] Crash-free user rate ≥ 99%.
- [ ] Test coverage rises from < 5% → ≥ 35% (target for Phase 2 exit).
- [ ] Admission flow used by ≥ 3 schools for Sep 2026 intake.

---

## Phase boundary

When Phase 2 ships, the next manifest is [`phase-3-launch.md`](./phase-3-launch.md). Phase 3 is the public Play Store launch and v1.0.0 gate.
