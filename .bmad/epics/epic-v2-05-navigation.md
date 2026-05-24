# Epic V2-05: Type-Safe Navigation Migration

**Epic ID:** EPIC-V2-05
**Title:** Type-Safe Navigation Migration
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P1
**Sprint:** 13-14
**Total Points:** 21

---

## 1. Overview

Navigation currently uses deprecated string-based routes with `navArgument` across 70+ destinations in an 890-line `hogwarts-nav-host.kt`. Migrate to Kotlin serialization-based type-safe navigation (Navigation Compose 2.8+).

### Business Value
- Compile-time route parameter verification eliminates runtime crashes
- Refactor-friendly (rename route -> compiler finds all call sites)
- Modern Android best practice

### Success Criteria
- All 70+ destinations use `@Serializable` route classes
- All navigation calls use typed `navigate(RouteClass)` instead of string interpolation
- Deep links work with typed routes
- All existing tests pass

---

## 2. Stories

### Story V2-05-S01: Define Serializable Route Objects [5 pts]
**Status:** Not Started
**Sprint:** 13

**As a** developer,
**I want** all routes as `@Serializable` data objects/classes,
**So that** navigation is type-safe.

**Acceptance Criteria:**
- [ ] Each feature module replaces `object XxxRoutes { const val ... }` with `@Serializable` classes
- [ ] Routes with params: `@Serializable data class CourseDetail(val courseId: String)`
- [ ] Routes without params: `@Serializable data object Dashboard`
- [ ] Shared `navigation` package in each feature module
- [ ] Compile-time parameter type verification

**Technical Notes:**
- `kotlin-serialization` plugin already applied. Nav Compose 2.8.5 supports type-safe nav.
- Example: `StreamRoutes.COURSE_DETAIL = "stream/course/{courseId}"` -> `@Serializable data class CourseDetail(val courseId: String)`

---

### Story V2-05-S02: Migrate NavHost to Typed Navigation [8 pts]
**Status:** Not Started
**Sprint:** 14

**As a** developer,
**I want** `HogwartsNavHost` to use `composable<Route>`,
**So that** route args are type-checked at compile time.

**Acceptance Criteria:**
- [ ] All `composable(route = "...", arguments = ...)` -> `composable<RouteClass>`
- [ ] All `navController.navigate("string/$args")` -> `navController.navigate(RouteClass(args))`
- [ ] All 70+ destinations migrated
- [ ] No runtime `IllegalArgumentException` from missing args
- [ ] Deep links still work

**Technical Notes:**
- Target: `app/src/main/java/org/hogwarts/android/navigation/hogwarts-nav-host.kt` (890 lines)
- Do incrementally (one feature module at a time)

---

### Story V2-05-S03: Deep Link Handler Migration [5 pts]
**Status:** Not Started
**Sprint:** 14

**As a** user tapping a notification,
**I want** deep links to resolve correctly,
**So that** I land on the expected content.

**Acceptance Criteria:**
- [ ] `deep-link-handler.kt` updated for typed routes
- [ ] `hogwarts://grades/{studentId}` -> typed `Grades(studentId)` route
- [ ] `hogwarts://stream/course/{courseId}` -> typed `CourseDetail(courseId)` route
- [ ] All 14 existing deep link tests pass
- [ ] Push notification taps resolve correctly

---

### Story V2-05-S04: Navigation Tests [3 pts]
**Status:** Not Started
**Sprint:** 14

**Acceptance Criteria:**
- [ ] Test: login -> dashboard
- [ ] Test: dashboard -> each main feature -> back
- [ ] Test: catalog -> detail -> chapters -> video
- [ ] Test: deep link -> correct screen
- [ ] Test: logout -> login (back stack cleared)
- [ ] All tests use `TestNavHostController`
