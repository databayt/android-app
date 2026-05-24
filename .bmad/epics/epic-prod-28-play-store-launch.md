# Epic E28: Play Store Launch

**Epic ID:** EPIC-PROD-28
**Title:** Play Store Launch
**Status:** Not Started
**Owner:** BMAD Dev Agent + growth + revenue
**Priority:** P0 (final gate)
**Sprint:** 16
**Total Points:** 18

---

## 1. Overview

Public Play Store launch. Listings in en + ar with localized screenshots. Privacy + Data Safety filed. Soft-launch in 1 country, then staged rollout.

### Success Criteria
- [ ] Listings live in en + ar.
- [ ] 8 localized screenshots per locale (phone + tablet).
- [ ] 30s promo video.
- [ ] Privacy Policy URL + Data Safety form approved by Play Console.
- [ ] Soft-launch successful in pilot country (≥100 installs / 30 days, crash-free ≥99%).
- [ ] Staged rollout to global (1% → 10% → 50% → 100%).

---

## 2. Stories

| ID | Title | Pts | Sprint |
|---|---|---:|---|
| E28.S01 | Play Console listing (en + ar) — title, short description, full description | 3 | 16 |
| E28.S02 | Screenshots: 8 per locale × 2 form factors (phone + 7" tablet) | 3 | 16 |
| E28.S03 | Promo video (30s, en + ar voiceovers) | 3 | 16 |
| E28.S04 | Privacy Policy URL + content (en + ar) hosted at ed.databayt.org/{en,ar}/privacy | 2 | 16 |
| E28.S05 | Data Safety form (data collected, purpose, sharing, security) | 2 | 16 |
| E28.S06 | Pre-launch report cleanup (fix any flagged crashes/ANRs) | 2 | 16 |
| E28.S07 | Soft-launch in 1 country (Sudan or Saudi Arabia) | 2 | 16 |
| E28.S08 | Staged rollout 1% → 10% → 50% → 100% | 1 | 16 |

### Detailed AC: E28.S05 — Data Safety
- [ ] Filed via Play Console:
  - Personal info: Name, Email, User ID — collected, processed for app function, encrypted in transit, user can request deletion (E10.S08)
  - App activity: Page views, App interactions — collected for analytics (Firebase)
  - Device info: Crash logs, Diagnostics — collected for app stability
  - Photos and videos: avatar uploads, message attachments — user-initiated only
  - Audio: voice messages — user-initiated only
  - Location: optional for geo-fence attendance — only if enabled by school
  - Health & fitness: NONE
- [ ] All marked "encrypted in transit".
- [ ] All marked "user can request deletion".

### Detailed AC: E28.S07 — Soft-launch
- [ ] Initial country: Sudan (pilot tenant King Fahad). Alternative: Saudi Arabia.
- [ ] Monitor for 30 days: crash-free rate, install count, uninstall rate, reviews.
- [ ] Gate global rollout on: crash-free ≥99%, no severe reviews, ≥50 active users.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| All prior epics complete | Required |
| E27 Release Pipeline | Required |
| E26 Observability for monitoring rollout | Required |
| Privacy Policy hosted on ed.databayt.org | Required (web team) |
| Designer for screenshots + promo video | Required (growth team) |

---

## 4. DoD

- [ ] All 8 stories merged.
- [ ] Listings live in Play Console.
- [ ] Pre-launch report green.
- [ ] Soft-launch metrics achieved (≥99% crash-free, ≥100 installs in 30 days).
- [ ] Global rollout reaches 100%.
- [ ] **v1.0.0 launched.** Captain + growth + revenue signoff.
