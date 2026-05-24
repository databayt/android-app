# Epic V2-08: Testing Infrastructure

**Epic ID:** EPIC-V2-08
**Title:** Testing Infrastructure
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P1
**Sprint:** 12-17 (continuous)
**Total Points:** 34

---

## 1. Overview

Currently 104 tests across 11 files. This epic expands to 250+ unit tests, 50+ UI tests, screenshot tests for visual regression, integration tests for offline-first verification, E2E tests, and CI pipeline.

### Business Value
- Catch regressions before they reach users
- Visual regression detection via screenshot comparison
- Confidence to refactor and ship frequently

### Success Criteria
- 250+ unit tests, 50+ UI tests
- Screenshot baselines for key screens in 4 variants
- CI runs all tests on every PR
- Build time under 15 minutes

---

## 2. Stories

### Story V2-08-S01: Unit Test Expansion [8 pts, ongoing]
**Status:** Not Started

**As a** developer,
**I want** unit tests for all ViewModel and UseCase classes,
**So that** business logic is verified.

**Acceptance Criteria:**
- [ ] Every ViewModel: min 5 test cases
- [ ] Every UseCase: min 3 tests (happy, error, edge)
- [ ] Repository tests verify offline-first (cache hit, network success, failure)
- [ ] MockK for mocking, Turbine for Flow testing
- [ ] Target: 250+ unit tests (up from 104)
- [ ] Coverage report per module

---

### Story V2-08-S02: Compose UI Tests [5 pts]
**Status:** Not Started
**Sprint:** 17

**As a** developer,
**I want** UI tests for major screens,
**So that** rendering and interaction are verified.

**Acceptance Criteria:**
- [ ] Tests for: login, dashboard, students, grades, attendance, courses, chat, settings
- [ ] Verify: content, clicks, nav triggers, error/empty/loading states
- [ ] Accessibility: all interactive elements have `contentDescription`
- [ ] Target: 50+ UI tests (up from 11)

---

### Story V2-08-S03: Screenshot Tests [5 pts]
**Status:** Not Started
**Sprint:** 17

**As a** developer,
**I want** screenshot tests for visual regression,
**So that** design changes are intentional.

**Acceptance Criteria:**
- [ ] Key screens in 4 variants: light-LTR, light-RTL, dark-LTR, dark-RTL
- [ ] Baseline screenshots committed to repo
- [ ] CI fails on visual diff above threshold
- [ ] Use Roborazzi (Robolectric 4.14.1 in catalog)

---

### Story V2-08-S04: Integration Tests [8 pts]
**Status:** Not Started
**Sprint:** 17

**As a** developer,
**I want** integration tests for the full data path,
**So that** offline-first architecture is verified end-to-end.

**Acceptance Criteria:**
- [ ] MockWebServer provides recorded responses
- [ ] Tests: fresh fetch -> Room -> subsequent reads from cache
- [ ] Tests: network error returns cached data
- [ ] SyncWorker integration test
- [ ] MutationQueue integration test

---

### Story V2-08-S05: E2E Tests [5 pts]
**Status:** Not Started
**Sprint:** 17

**As a** developer,
**I want** E2E tests simulating real user journeys,
**So that** critical paths work on real devices.

**Acceptance Criteria:**
- [ ] Journey: launch -> login -> dashboard -> grades -> courses -> video -> back -> logout
- [ ] Journey: login -> messaging -> chat -> send message -> back -> logout
- [ ] Run on emulator in CI
- [ ] Use Maestro (YAML-based)

---

### Story V2-08-S06: CI Pipeline [3 pts]
**Status:** Not Started
**Sprint:** 17

**As a** developer,
**I want** CI to run all tests on every PR,
**So that** regressions are caught before merge.

**Acceptance Criteria:**
- [ ] GitHub Actions: lint, unit tests, UI tests, screenshot comparison
- [ ] Build fails on: lint errors, test failures, screenshot diffs
- [ ] Coverage report as PR comment
- [ ] Build time under 15 minutes
