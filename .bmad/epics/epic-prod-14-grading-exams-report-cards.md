# Epic E14: Grading, Exams & Report Cards

**Epic ID:** EPIC-PROD-14
**Title:** Grading, Exams & Report Cards
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0
**Sprint:** 9-11
**Total Points:** 55

---

## 1. Overview

End-to-end grading + exam-taking + report card delivery. Closes the `submit-grade-use-case.kt:20` fake-response stub.

### Success Criteria
- [ ] Teacher enters grades; saved to live backend (no more fake `GradeRecord`).
- [ ] Online exam delivery is timed and auto-submits.
- [ ] Hall ticket QR generates correctly and validates at exam venue.
- [ ] Report card PDFs render server-side, downloaded via WorkManager.

---

## 2. Stories

| ID | Title | Pts | Sprint | Notes |
|---|---|---:|---|---|
| E14.S01 | Grade entry (teacher) — fixes fake `submit-grade-use-case.kt:20` | 8 | 9 | |
| E14.S02 | Grade view (student/guardian) | 3 | 9 | |
| E14.S03 | Grade summary per subject + GPA | 3 | 9 | |
| E14.S04 | Online exam delivery — timed, autosubmit, anti-cheat heartbeat | 13 | 9-10 | |
| E14.S05 | Exam question bank (admin/teacher) | 5 | 10 | |
| E14.S06 | Exam scheduling + venues (admin) | 5 | 10 | |
| E14.S07 | Exam results view + analytics | 3 | 10 | |
| E14.S08 | Exam certificate (PDF + share) | 5 | 10 | |
| E14.S09 | Hall ticket QR | 3 | 10 | Existing skeleton |
| E14.S10 | Report card view (student/guardian) | 3 | 11 | |
| E14.S11 | Report card progress charts | 3 | 11 | |
| E14.S12 | Tests | 1 | 11 | |

### Detailed AC: E14.S01 — Grade entry
- [ ] Teacher selects class + subject + assessment → roster appears with score input fields.
- [ ] Bulk paste / CSV import.
- [ ] `SubmitGradeUseCase` calls real `POST /api/mobile/grades` (E08.S12) — closes `submit-grade-use-case.kt:20` TODO.
- [ ] Optimistic save with rollback on error.
- [ ] Mark-as-final flag — finalized grades read-only without admin override.

### Detailed AC: E14.S04 — Online exam
- [ ] Student starts exam → server returns paper + start timestamp + duration.
- [ ] Timer counts down; auto-submits at 0.
- [ ] Per-question save (heartbeat to `POST /api/mobile/exams/{id}/submit-answer`) every 30s.
- [ ] Anti-cheat: tab-switch detection, screenshot detection (best-effort), focus-loss timestamp logged to server.
- [ ] Network disconnect: continue locally, sync when reconnected.
- [ ] Submit button confirms — submits all answers + finishes exam.
- [ ] Existing `feature/exams/` skeleton extended; `exam-violation-entity` already exists in DB.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E08.S12, E08.S13 grades + exams endpoints | Required |
| E06 DB translation (subject names, exam titles) | Required |
| Existing `feature/grades/`, `feature/exams/`, `feature/report-cards/` skeletons | Available |

---

## 4. DoD

- [ ] All 12 stories merged.
- [ ] Maestro flow: teacher enters grade → student views in their app within 60s.
- [ ] Maestro flow: student takes 30-min online exam (with simulated network blip) → submits → result available.
- [ ] PDF report card downloads + opens.
- [ ] Captain signoff.
