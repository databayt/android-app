# Epic E11: Dashboard & Navigation

**Epic ID:** EPIC-PROD-11
**Title:** Dashboard & Navigation
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0
**Sprint:** 7-8
**Total Points:** 26

---

## 1. Overview

Role-aware dashboard with all 19 tiles routed and polished. Search, dock, deep-links, app shortcuts complete. Mirrors hogwarts sidebar role matrix at `src/components/template/platform-sidebar/config.ts`.

### Success Criteria
- [ ] Every dashboard tile routes to a working screen (no empty `onClick`).
- [ ] Tiles filter by role (per hogwarts matrix).
- [ ] Tile order respects server `enabledModules`.
- [ ] All 10 deep-link destinations route correctly on cold start.
- [ ] 4 dynamic app shortcuts per role.

---

## 2. Stories

### Story E11.S01: Wire 6 dashboard TODO tiles [5 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] `feature/dashboard/.../home-tile-spec.kt` `onClick` lambdas for notifications, exams, assignments, library, events, profile point at their existing target screens.
- [ ] Each transition uses standard Material 3 motion.
- [ ] Empty state when target screen has no data.

**Files:** `feature/dashboard/.../home-tile-spec.kt`, `home-screen.kt`.

---

### Story E11.S02: Dashboard search pill functional [3 pts]
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] `feature/dashboard/.../home-search-pill.kt` opens a global search Composable.
- [ ] Queries `GET /api/mobile/search?q=...` (server-side; new — add to E08.S20 if not already).
- [ ] Results grouped by type (students, classes, announcements, etc.).
- [ ] Tap result deep-links to its detail screen.

---

### Story E11.S03: Role-specific tile filtering [5 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] `home-tile-spec.kt` reads `currentRole` and filters tiles per hogwarts sidebar matrix:
  - DEVELOPER: all + sales + tenants
  - ADMIN: all except DEVELOPER-only
  - TEACHER: dashboard, classes, students, attendance, grades, exams, timetable, announcements, messages, library, stream
  - STUDENT: dashboard, attendance, grades, exams, timetable, announcements, messages, library, stream, fees
  - GUARDIAN: dashboard, children's data, fees, messages, announcements
  - ACCOUNTANT: dashboard, finance, invoices, payments
  - STAFF: dashboard, role-specific surfaces
- [ ] Tile order from server `enabledModules: List<String>`.
- [ ] If `enabledModules` is empty, show all tiles for the role.

**Refs:** hogwarts `src/components/template/platform-sidebar/config.ts`.

---

### Story E11.S04: Quick actions per role (long-press tile) [3 pts]
**Status:** Not Started · **Sprint:** 8

**Acceptance Criteria:**
- [ ] Long-press tile shows context menu (Material 3 `DropdownMenu`).
- [ ] Examples: Teacher long-presses Attendance → "Mark today's class".
- [ ] Examples: Guardian long-presses Fees → "Pay outstanding".

---

### Story E11.S05: Bottom nav bar polish [3 pts]
**Status:** Not Started · **Sprint:** 8

**Acceptance Criteria:**
- [ ] Material 3 `NavigationBar` with 4-5 most-frequent destinations per role.
- [ ] Active state animation; haptic feedback on tap.
- [ ] Existing `HogwartsTabBar` (with shrink-on-scroll) extended.

---

### Story E11.S06: Sidebar drawer for less-frequent destinations [3 pts]
**Status:** Not Started · **Sprint:** 8

**Acceptance Criteria:**
- [ ] Material 3 `ModalNavigationDrawer` opened via hamburger or swipe-from-edge (RTL-aware).
- [ ] All role-permitted destinations listed; bottom nav destinations highlighted.

---

### Story E11.S07: Deep-link audit for cold-start [3 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 8

**Acceptance Criteria:**
- [ ] All 10 existing deep-link destinations route correctly when app cold-starts.
- [ ] Existing 14 unit tests in `app/.../navigation/deep-link-handler-test.kt` extended with 6 cold-start E2E tests using Maestro.
- [ ] Token refresh handled if deep-link arrives with expired token.

**Files:** `app/.../navigation/deep-link-handler.kt`, `app/.../navigation/hogwarts-nav-host.kt`, `.maestro/flows/deep-link-{1..10}.yaml`.

---

### Story E11.S08: App shortcuts (long-press launcher icon) [2 pts]
**Status:** Not Started · **Sprint:** 8

**Acceptance Criteria:**
- [ ] 4 dynamic shortcuts based on role (e.g. teacher: "Mark Attendance", "Today's Classes", "Send Announcement", "Grade Entry").
- [ ] AndroidManifest `<meta-data android:name="android.app.shortcuts"/>`.
- [ ] Update on role change.

**Files:** `app/src/main/res/xml/shortcuts.xml`, `app/.../shortcut/shortcut-manager.kt`.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E02 (8-role enum, ability) | Required |
| E08.S20 search endpoint | Required for E11.S02 |
| Existing `home-tile-spec.kt`, `home-screen.kt` | Available |

---

## 4. DoD

- [ ] All 8 stories merged.
- [ ] Manual: log in as each of 8 roles → confirm correct tiles visible + tile order honors server settings.
- [ ] All 10 deep-links work on cold start.
- [ ] Captain signoff.
