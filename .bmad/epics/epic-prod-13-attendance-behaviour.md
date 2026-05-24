# Epic E13: Attendance & Behaviour

**Epic ID:** EPIC-PROD-13
**Title:** Attendance & Behaviour
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0 (pilot-critical)
**Sprint:** 8-10
**Total Points:** 55

---

## 1. Overview

Production-grade attendance for the pilot. The largest feature module by use cases (9 already exist). Closes ~15 existing TODOs in `feature/attendance/`.

### Success Criteria
- [ ] Teacher can mark attendance manually, in bulk, and via QR.
- [ ] Geo-fence + biometric methods configurable per school.
- [ ] Excuse workflow end-to-end.
- [ ] Real-time updates to guardians (E09.S05).
- [ ] All 9 use cases connected to live backend (closes `attendance-repository-impl.kt:159` TODO).

---

## 2. Stories

| ID | Title | Pts | Sprint | Notes |
|---|---|---:|---|---|
| E13.S01 | Mark single + bulk attendance (teacher) — wires existing TODO | 8 | 8 | Closes `attendance-repository-impl.kt:159` |
| E13.S02 | QR code session + scan — uses CameraX + ML Kit barcode-scanning | 8 | 8 | Already in deps |
| E13.S03 | Geo-fence attendance (optional, per school setting) | 5 | 9 | Uses Play Services Location |
| E13.S04 | Excuse workflow (student/parent submits, admin approves) | 5 | 9 | |
| E13.S05 | Chronic-absence interventions — wires `interventions-screen.kt:309,324` TODOs | 5 | 9 | Alert parent + refer counselor actions |
| E13.S06 | Attendance gamification (badges, streaks, competitions) | 5 | 10 | |
| E13.S07 | Kiosk mode (public attendance kiosk) | 5 | 10 | |
| E13.S08 | Hall pass (teacher issues, kiosk verifies) | 3 | 10 | |
| E13.S09 | Attendance analytics dashboard | 3 | 10 | |
| E13.S10 | Attendance method settings (admin configures) | 2 | 10 | |
| E13.S11 | Real-time attendance updates | 3 | 10 | Depends on E09.S05 |
| E13.S12 | Tests: 5 unit per VM, 2 Compose per screen | 3 | 10 | |

### Detailed AC: E13.S01 — Mark attendance
- [ ] Teacher selects class → roster loads → toggle each student PRESENT / ABSENT / LATE / EXCUSED.
- [ ] Bulk action: "Mark all PRESENT" with one tap.
- [ ] Saves optimistically; queues offline if no network (via E01.S03 mutation queue).
- [ ] Existing `MarkAttendanceUseCase` + `MarkBulkAttendanceUseCase` wired to live `POST /api/mobile/attendance/mark` + `/bulk`.
- [ ] Existing 13 unit tests + 4 Compose tests extended with 8 new edge-case tests.

### Detailed AC: E13.S02 — QR
- [ ] Teacher creates QR session: pick class → server returns QR payload → display 60s rotating QR.
- [ ] Student opens "Scan QR" → CameraX + `BarcodeScanning.getClient()` reads QR → submits → server marks present.
- [ ] Anti-cheating: QR rotates every 30s; server validates session+timestamp+student in roster.

### Detailed AC: E13.S05 — Interventions
- [ ] `interventions-screen.kt:309` "Alert parent" → opens compose-message screen pre-filled with parent contact + template.
- [ ] `interventions-screen.kt:324` "Refer counselor" → creates intervention record + notifies counselor role.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E08.S11 attendance endpoints | Required |
| E09.S05 real-time attendance | Required for E13.S11 |
| E01.S03 mutation queue | Required for offline marking |
| CameraX 1.4.0 + ML Kit barcode-scanning 17.3.0 | Already in deps |
| Play Services Location 21.3.0 | Already in deps |
| Existing `feature/attendance/` (48 files) | Available skeleton |

---

## 4. DoD

- [ ] All 12 stories merged.
- [ ] Maestro flow: teacher marks 30 students → guardian sees update on dashboard within 2s.
- [ ] Maestro flow: teacher creates QR → student scans → mark recorded.
- [ ] Manual: turn off network → mark 5 students → reconnect → marks reach server within 60s.
- [ ] Captain signoff.
