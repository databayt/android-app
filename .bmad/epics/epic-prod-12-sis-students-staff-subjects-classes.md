# Epic E12: SIS — Students, Staff, Subjects, Classes

**Epic ID:** EPIC-PROD-12
**Title:** SIS Core Surfaces
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0
**Sprint:** 8-9
**Total Points:** 47

---

## 1. Overview

Full CRUD on the core SIS entities for ADMIN + STAFF; read-only views for TEACHER + GUARDIAN.

### Success Criteria
- [ ] Admin can manage students, staff, subjects, classes from mobile.
- [ ] Teacher sees their classes + roster.
- [ ] Bulk CSV import works.
- [ ] All translatable fields use E06 `LocalizedText`.

---

## 2. Stories (each 3-5 pts unless noted)

| ID | Title | Pts | Sprint |
|---|---|---:|---|
| E12.S01 | Students list (admin) — pagination, search, filters by grade/section/status | 5 | 8 |
| E12.S02 | Student detail screen — academic info, contacts, attendance summary | 3 | 8 |
| E12.S03 | Student create + edit (admin) | 5 | 8 |
| E12.S04 | Bulk student import via CSV (admin) — picks file, uploads, shows progress + errors | 5 | 9 |
| E12.S05 | Staff/teacher list (admin) | 5 | 8 |
| E12.S06 | Staff detail + assignments view | 3 | 9 |
| E12.S07 | Subjects list per role (server-translated names via E06) | 3 | 9 |
| E12.S08 | Subject detail + curriculum link | 3 | 9 |
| E12.S09 | Classes (sections) list | 3 | 9 |
| E12.S10 | Class detail + roster | 3 | 9 |
| E12.S11 | Class assignment to teachers (admin) | 3 | 9 |
| E12.S12 | Per-entity translation via E06 helpers (sweep) | 3 | 9 |
| E12.S13 | Tests: 5 unit per VM, 1 Compose per screen | 3 | 9 |

### Common Acceptance Criteria (every list/detail story)
- [ ] List screen: paginated (20/page), search debounced 300ms, filters chips, pull-to-refresh, empty + error + loading states.
- [ ] Detail screen: read-only by default; edit button visible only when `Ability.can(Update, Subject, resource)`.
- [ ] Form screen: Zod-equivalent validation, error rendering per field, optimistic save with rollback on error.
- [ ] All translatable fields rendered via `LocalizedText`.
- [ ] All money/date/number values via E07 formatters.

### Files (typical per module)
- `feature/students/.../{list,detail,form}-screen.kt`
- `feature/students/.../viewmodel/{list,detail,form}-view-model.kt`
- `feature/students/.../data/students-repository-impl.kt`
- Existing files extended (don't redo what's already there).

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E02 RBAC + ability | Required |
| E06 DB translation | Required |
| E07 formatters | Required |
| E08.S09, E08.S10 SIS endpoints | Required |
| Existing `feature/students/`, `feature/teacher/`, `feature/admin/` modules | Available skeletons |

---

## 4. DoD

- [ ] All 13 stories merged.
- [ ] Maestro flow: admin creates student → teacher views in roster → student appears in their own profile.
- [ ] CSV bulk import test: import 1000-row CSV → verify rows in DB → handle malformed input gracefully.
- [ ] Captain signoff.
