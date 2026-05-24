# Epic E15: Timetable & Curriculum

**Epic ID:** EPIC-PROD-15
**Title:** Timetable & Curriculum
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P1
**Sprint:** 10-11
**Total Points:** 34

---

## 1. Overview

Read-only timetable + lesson plan + curriculum tooling. Teachers create lesson plans; students/parents view schedule.

### Success Criteria
- [ ] Every role sees their relevant timetable view (student: own; teacher: theirs + per-class; admin: school-wide).
- [ ] Teachers can create + edit lesson plans tied to timetable slots.
- [ ] Curriculum map visualizes term-level coverage.

---

## 2. Stories

| ID | Title | Pts | Sprint | Notes |
|---|---|---:|---|---|
| E15.S01 | Timetable read view (all roles, role-specific filtering) | 5 | 10 | Existing UI extended |
| E15.S02 | Day/week toggle + smooth swipe between weeks | 3 | 10 | |
| E15.S03 | Lesson plan list (teacher) | 5 | 10 | |
| E15.S04 | Lesson plan create + edit | 5 | 10 | |
| E15.S05 | Curriculum map (term coverage visualization) | 5 | 11 | |
| E15.S06 | Resource browser per lesson (PDFs, links, videos) | 5 | 11 | |
| E15.S07 | Calendar widget (Android home screen widget) | 3 | 11 | |
| E15.S08 | Tests | 3 | 11 | |

### Detailed AC: E15.S07 — Home widget
- [ ] Glance widget showing today's classes.
- [ ] Updates on schedule change via `WorkManager`.
- [ ] Material 3 You theme respect.
- [ ] Tap → opens app to today's timetable.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E08.S14 timetable endpoints | Required |
| E06 DB translation (lesson titles, subject names) | Required |
| Existing `feature/timetable/` (11 files), `feature/lessons/` (18 files) | Available |

---

## 4. DoD

- [ ] All 8 stories merged.
- [ ] Manual: each role sees correct timetable view.
- [ ] Maestro: teacher creates lesson plan → student sees attached resources.
- [ ] Home widget updates within 1h of timetable change.
- [ ] Captain signoff.
