# Epic E26: Observability & Crash Reporting

**Epic ID:** EPIC-PROD-26
**Title:** Observability & Crash Reporting
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0
**Sprint:** 15-16
**Total Points:** 26

---

## 1. Overview

Every release is observable. Crash-free rate, ANR rate, Core Vitals, business metrics all dashboarded. Build on existing Crashlytics + Performance + Analytics integrations (already wired in `core/push/`).

### Success Criteria
- [ ] Crashlytics shows crash with deobfuscated stack within 5 min of occurrence.
- [ ] Performance traces for cold start, dashboard render, attendance mark, chat send.
- [ ] Business analytics events fire correctly.
- [ ] User attribution (userId, schoolId, role) on every crash.
- [ ] Crash-free user rate ≥99.5% on rolling 7-day window.
- [ ] Shake-to-report opens GitHub Issues with structured payload.

---

## 2. Stories

| ID | Title | Pts | Sprint |
|---|---|---:|---|
| E26.S01 | Verify Crashlytics integration end-to-end (test crash) | 3 | 15 |
| E26.S02 | Firebase Performance — screen render + network traces | 3 | 15 |
| E26.S03 | Firebase Analytics — business events | 3 | 15 |
| E26.S04 | User attribution (userId, schoolId, role) on Crashlytics | 2 | 15 |
| E26.S05 | ANR rate tracking + alerting | 2 | 15 |
| E26.S06 | Crash-free user rate dashboard | 2 | 15 |
| E26.S07 | Sentry for non-fatal exceptions (parallel to Crashlytics, optional) | 3 | 16 |
| E26.S08 | Logcat to remote (debug-only via OkHttp eventlistener) | 2 | 16 |
| E26.S09 | User feedback / shake-to-report → GitHub Issues | 3 | 16 |
| E26.S10 | Custom dashboards in Firebase Console | 3 | 16 |

### Detailed AC: E26.S03 — Business analytics
- [ ] Events: `attendance_marked`, `grade_viewed`, `fee_paid`, `message_sent`, `course_started`, `course_completed`, `screen_view` (auto), `login`, `logout`, `register`, `tenant_switch`.
- [ ] Each event includes `userRole`, `schoolId` (already wired in `core/push/analytics-tracker.kt`).
- [ ] Custom audience for "active parents" (logged in 7+ days last 30).

### Detailed AC: E26.S09 — Shake-to-report
- [ ] Sensor accelerometer detects shake gesture.
- [ ] Opens dialog: "Report an issue?" → screenshot of current screen + log buffer + device info → submits to GitHub Issues via `POST https://api.github.com/repos/databayt/kotlin-app/issues` with bot token.
- [ ] User can edit description before submit.
- [ ] Issue auto-tagged `report,from-mobile,android`.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E01.S01 `google-services.json` | Required |
| Existing `core/push/{analytics-tracker,crash-reporter}.kt` | Available |
| GitHub bot token for Issue creation | Required (Keychain) |

---

## 4. DoD

- [ ] All 10 stories merged.
- [ ] Test crash → Crashlytics within 5 min.
- [ ] 7-day crash-free dashboard configured in Firebase.
- [ ] Shake-to-report E2E: shake → dialog → submit → GitHub issue created.
- [ ] Captain signoff.
