# Epic E20: Role-Specific Portals (Guardian, Teacher, Admin)

**Epic ID:** EPIC-PROD-20
**Title:** Role-Specific Portals
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P1
**Sprint:** 12-13
**Total Points:** 39

---

## 1. Overview

Bespoke role-specific entry-point flows. The `feature/guardian/` (11 screens), `feature/teacher/` (6 screens), `feature/admin/` (5 screens) modules are scaffolded; this epic fills them in with live data + role-specific UX.

### Success Criteria
- [ ] Guardian can switch between children seamlessly.
- [ ] Teacher has a complete classroom-management cockpit.
- [ ] Admin can do school-wide queries from mobile (read-only mostly; bulk admin still desktop).

---

## 2. Stories

| ID | Title | Pts | Sprint |
|---|---|---:|---|
| E20.S01 | Guardian children list + child switcher (top of dashboard) | 3 | 12 |
| E20.S02 | Per-child attendance/grades/fees/timetable views (4 views × child) | 8 | 12 |
| E20.S03 | Guardian-teacher meeting booking | 3 | 12 |
| E20.S04 | Consent forms (sign + return) | 3 | 12 |
| E20.S05 | Trip permission slips | 3 | 13 |
| E20.S06 | Communication preferences (per child) | 2 | 13 |
| E20.S07 | Teacher classes (list + detail) | 3 | 13 |
| E20.S08 | Teacher batch attendance — extended from E13.S01 | 3 | 13 |
| E20.S09 | Teacher grade entry — depends on E14.S01 | 2 | 13 |
| E20.S10 | Teacher schedule (today's classes + today's tasks) | 2 | 13 |
| E20.S11 | Admin dashboard (school-wide metrics) | 2 | 13 |
| E20.S12 | School info edit (name, logo, contact, currency) | 2 | 13 |
| E20.S13 | Staff directory | 2 | 13 |
| E20.S14 | School stats (admissions, attendance %, financial overview) | 1 | 13 |

### Detailed AC: E20.S01 — Child switcher
- [ ] Top of dashboard shows current child's avatar + name + grade.
- [ ] Tap → bottom sheet with all guardian's children → tap child → all per-child views switch context.
- [ ] State persists across sessions (DataStore).

### Detailed AC: E20.S03 — Meeting booking
- [ ] Guardian sees teacher's available slots (from teacher's calendar).
- [ ] Books slot → teacher gets notification + calendar event.
- [ ] Guardian can cancel up to 24h before.

### Detailed AC: E20.S04 — Consent forms
- [ ] List of pending forms.
- [ ] Open form → renders content (with E06 translation if needed) → sign with finger / type name.
- [ ] Submit → server records + notifies admin.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E08.S20 (admission, events, library) for some role surfaces | Required |
| E12 (SIS) for student data | Required |
| E13 (attendance), E14 (grades), E16 (fees), E15 (timetable) | Required for guardian per-child views |
| Existing `feature/{guardian,teacher,admin}/` skeletons | Available |

---

## 4. DoD

- [ ] All 14 stories merged.
- [ ] Maestro flow: guardian with 2 kids → switch between → see different attendance for each.
- [ ] Maestro flow: teacher books a meeting with guardian → guardian receives notification → confirms.
- [ ] Captain signoff.
