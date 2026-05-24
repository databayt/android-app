# Epic E22: Test Infrastructure & Coverage

**Epic ID:** EPIC-PROD-22
**Title:** Test Infrastructure & Coverage
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0
**Sprint:** 14-15
**Total Points:** 55

---

## 1. Overview

Move from 16 unit-test files to ≥75% line coverage on `core/`, ≥60% on `feature/`, with full Compose UI tests on critical flows and Maestro E2E for top user journeys. Closes the test-coverage gap (22 of 25 feature modules currently have zero unit tests).

### Success Criteria
- [ ] Kover reports ≥75% on `core/`, ≥60% on `feature/`.
- [ ] All 25 feature modules have ≥5 ViewModel unit tests each.
- [ ] Top 30 screens have ≥1 Compose UI test (initial render + 1 interaction).
- [ ] Top 8 user journeys have Maestro E2E flows.
- [ ] Migration tests cover v7 → v24 round-trip.
- [ ] CI fails on coverage drop.

---

## 2. Stories

| ID | Title | Pts | Sprint | Notes |
|---|---|---:|---|---|
| E22.S01 | Add Kover coverage targets to root + per-module gate | 3 | 14 | Depends E01.S11 |
| E22.S02 | Establish ViewModel testing harness (per-feature `:test-fixtures`) | 5 | 14 | |
| E22.S03 | Generate ≥5 unit tests per ViewModel for the 22 untested feature modules | 13 | 14-15 | ~110 new tests |
| E22.S04 | Compose UI tests for top 30 screens (LTR + RTL) | 13 | 14-15 | |
| E22.S05 | Room migration tests for v7 → v24 (every step) | 5 | 14 | Depends E01.S02 |
| E22.S06 | Maestro E2E flows for top 8 user journeys | 8 | 15 | |
| E22.S07 | Contract tests (depends E08.S21) | 3 | 15 | |
| E22.S08 | Snapshot tests RTL + LTR (depends E05.S03) | 3 | 15 | |
| E22.S09 | Performance regression tests (depends E23.S02 baseline) | 2 | 15 | |

### Detailed AC: E22.S02 — Test harness
- [ ] Per-feature `:test-fixtures` provides:
  - `runVMTest { … }` Coroutine test scope helper
  - `FakeRepository<T>(...)`, `FakeAbility(allows: List<Pair<Action, Subject>>)`, `FakeTenantContext`
  - `FakeNavController` for nav assertion
- [ ] All ViewModel tests use this harness; no per-module reinvention.

### Detailed AC: E22.S06 — Maestro E2E flows
- [ ] `.maestro/flows/`:
  - `01-login-as-teacher.yaml`
  - `02-mark-attendance.yaml`
  - `03-view-grades.yaml`
  - `04-pay-invoice.yaml`
  - `05-send-message.yaml`
  - `06-take-online-exam.yaml`
  - `07-upload-avatar.yaml`
  - `08-logout-and-relogin.yaml`
- [ ] CI runs against staging build on macOS runners.
- [ ] Flake-detection: each flow runs 3x; >1 failure = fail.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E01.S11 Kover plugin | Required |
| E01.S02 explicit migrations | Required |
| E08.S21 contract tests baseline | Required |
| E05.S03 snapshot tests baseline | Required |
| E23.S02 perf baseline | Required |

---

## 4. DoD

- [ ] All 9 stories merged.
- [ ] CI dashboard shows coverage targets met.
- [ ] All 8 Maestro flows green on every PR.
- [ ] Captain signoff.
