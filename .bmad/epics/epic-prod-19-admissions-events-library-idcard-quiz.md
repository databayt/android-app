# Epic E19: Admissions, Events, Library, ID Card, Quiz Game

**Epic ID:** EPIC-PROD-19
**Title:** Domain Round-out
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P2
**Sprint:** 12-13
**Total Points:** 47

---

## 1. Overview

Round out the remaining 5 domain modules. Each is a smaller surface area than the core SIS/attendance/grading modules but completes the feature parity story.

### Success Criteria
- [ ] Each module's existing skeleton (3-7 screens) connected to live backend.
- [ ] Public surfaces (admission status check, tour booking) work without auth.
- [ ] Apple Wallet + Google Wallet passes for ID card.

---

## 2. Stories

| ID | Title | Pts | Sprint | Notes |
|---|---|---:|---|---|
| E19.S01 | Admission application list (admin) | 3 | 12 | |
| E19.S02 | Admission application form (multi-step) | 8 | 12 | |
| E19.S03 | Admission status check (public, OTP-gated) | 3 | 12 | |
| E19.S04 | Tour booking (public) | 3 | 12 | |
| E19.S05 | Events list + calendar | 3 | 12 | |
| E19.S06 | Event detail + RSVP | 3 | 12 | |
| E19.S07 | Library catalog with search | 3 | 13 | |
| E19.S08 | Book detail + borrow flow | 3 | 13 | |
| E19.S09 | My borrowings + due-date alerts | 3 | 13 | |
| E19.S10 | Digital ID card | 5 | 13 | |
| E19.S11 | ID card wallet (Apple Wallet pass + Google Wallet pass) | 5 | 13 | |
| E19.S12 | Quiz game hub + practice mode | 3 | 13 | |
| E19.S13 | Quiz timed challenge + leaderboard | 3 | 13 | |
| E19.S14 | Tests | 1 | 13 | |

### Detailed AC: E19.S02 — Admission form
- [ ] Multi-step wizard with savedstate.
- [ ] Sections: applicant info, parent info, prior school, documents (upload), declaration.
- [ ] Per-step validation; can save draft.
- [ ] Submit → `POST /api/mobile/admission/applications`.

### Detailed AC: E19.S11 — Wallet passes
- [ ] Server endpoint returns `.pkpass` (Apple) or Google Wallet JWT.
- [ ] Client opens system intent → user adds to wallet.
- [ ] QR code on pass validates at school gate (kiosk).

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E08.S20 module endpoints | Required |
| Existing `feature/{admission,events,library,idcard,quizgame}/` skeletons | Available |

---

## 4. DoD

- [ ] All 14 stories merged.
- [ ] Maestro flow: parent submits admission → admin reviews → status check shows accepted.
- [ ] Wallet pass: add to Google Wallet → QR validates at kiosk.
- [ ] Captain signoff.
