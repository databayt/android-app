# Epic E02: Multi-Tenancy & RBAC Core

**Epic ID:** EPIC-PROD-02
**Title:** Multi-Tenancy & RBAC Core
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0 (Blocking)
**Sprint:** 1-3
**Total Points:** 39

---

## 1. Overview

Production-grade tenant isolation and 8-role permission enforcement. Every Room query, every API call, every navigation gate scoped by `tenantId` and the active `Role`. The Android client mirrors hogwarts' RBAC structure with predicate-based ability checks adapted to Kotlin (CASL's Prisma-where expressiveness doesn't translate cleanly).

### Business Value
- Tenant data isolation — provably no cross-tenant leakage.
- Correct UI affordance per role (admins see admin tiles, students don't).
- Audit-ready authorization layer matching the web's enforcement.

### Success Criteria
- [ ] Local `UserRole` enum has all 8 roles matching `prisma/models/auth.prisma:2-11`.
- [ ] CI fails on a Room `@Query` without `tenantId` (Detekt `RequireTenantScope` from E01.S09).
- [ ] A logged-in TEACHER cannot navigate to `/admission` or invoke `createGuardian()`.
- [ ] Logout fully clears EncryptedSharedPreferences, Room, DataStore, in-memory `TenantContext`.
- [ ] `CrossTenantIsolationTest` passes for ≥3 representative DAOs.

---

## 2. Stories

### Story E02.S01: Expand `UserRole` enum to 8 roles [2 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 1

**As a** mobile user with the ACCOUNTANT role on the web, **I want** my role to be honored on mobile, **So that** I can do my job from the field.

**Acceptance Criteria:**
- [ ] `core/data/.../tenant/tenant-context.kt` `UserRole` enum has 8 values: `DEVELOPER, ADMIN, TEACHER, STUDENT, GUARDIAN, ACCOUNTANT, STAFF, USER`.
- [ ] Wire-compatible string identifiers match server (`DEVELOPER`, `ADMIN`, etc.).
- [ ] Rename `SUPER_ADMIN` → `DEVELOPER` everywhere (~12 callsites).
- [ ] Existing `hasRole` / `hasAnyRole` callers updated.
- [ ] `kotlinx.serialization` `@SerialName` on each variant matches the server JSON.

**Files:** `core/data/.../tenant/tenant-context.kt`, `feature/auth/.../session-repository.kt`, all callers.

**Refs:** server enum at `prisma/models/auth.prisma:2-11`. Web mirror at `src/lib/rbac/types.ts:43-62`.

---

### Story E02.S02: Move `SessionManagerImpl` from `app/.../di/repository-module.kt` into `core/security` [3 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 1

**As a** developer auditing security primitives, **I want** session storage in `core/security`, **So that** the auditable surface is one module.

**Acceptance Criteria:**
- [ ] `SessionManagerImpl` lives at `core/security/.../session-manager-impl.kt`.
- [ ] `app/.../di/repository-module.kt` only `@Binds` the interface (no behavior in DI module).
- [ ] EncryptedSharedPreferences file name (`hogwarts_session`) and key set (AES256_GCM/SIV) preserved — no behavior change.
- [ ] `core/security/build.gradle.kts` adds `implementation(libs.androidx.security.crypto)`.

**Files:** `core/security/.../session-manager-impl.kt` (new), `app/.../di/repository-module.kt` (cleanup).

---

### Story E02.S03: Define `Action` and `Subject` taxonomies [3 pts]
**Status:** Not Started · **Sprint:** 2

**As a** future story author, **I want** a fixed vocabulary of actions and subjects, **So that** every authorization check uses the same taxonomy.

**Acceptance Criteria:**
- [ ] New Gradle module `core/auth` registered in `settings.gradle.kts`.
- [ ] `core/auth/.../ability/action.kt` declares `enum class Action { Read, Create, Update, Delete, Export, Import, Manage }`.
- [ ] `core/auth/.../ability/subject.kt` declares `sealed interface Subject` with 30+ object subjects: `Student`, `Teacher`, `Guardian`, `Class`, `Section`, `SubjectEntity`, `Attendance`, `Grade`, `Exam`, `Invoice`, `Announcement`, `Course`, `Lesson`, `Book`, `Event`, `ReportCard`, `IdCard`, `Conversation`, `Notification`, `Fee`, `Payment`, `Scholarship`, `Fine`, `Schedule`, `LessonPlan`, `Application`, `Tour`, `Transport`, `Hostel`, `Lead`, `School`.
- [ ] Each `Subject` has a companion mapper to / from a string identifier matching server-side CASL subject names.

**Files:** `core/auth/build.gradle.kts`, `core/auth/.../ability/action.kt`, `core/auth/.../ability/subject.kt`, `settings.gradle.kts`.

**Refs:** hogwarts subjects at `src/lib/rbac/types.ts` (`AppSubjects` union).

---

### Story E02.S04: Implement predicate-based `Ability` interface and per-role builders [8 pts]
**Status:** Not Started · **Sprint:** 2

**As a** ViewModel, **I want** to ask "can the user do X to Y", **So that** I can show/hide UI affordances correctly.

**Acceptance Criteria:**
- [ ] `interface Ability { fun can(action: Action, subject: Subject, resource: Any? = null): Boolean }`.
- [ ] 8 role policy files: `core/auth/.../policies/{developer,admin,teacher,student,guardian,accountant,staff,user}-policy.kt`.
- [ ] Each policy is a `(ctx: AbilityContext) -> Ability` factory.
- [ ] `AbilityContext` carries `userId, role, schoolId, studentId?, teacherId?, guardianId?, staffMemberId?, classIds: Set<String>, childIds: Set<String>`.
- [ ] `AbilityRegistry.forContext(ctx)` returns the configured `Ability`.
- [ ] Predicates evaluate over the resource (e.g. `TeacherPolicy` checks `(resource as? AttendanceEntity)?.classId in ctx.classIds` for `Update Attendance`).
- [ ] Parameterized unit test covers all 8 roles × all 30+ subjects × all 7 actions = ~1700 cases (with `null` resource for the simplest path).

**Files:** `core/auth/.../ability/{ability,ability-context,ability-registry}.kt`, 8 policy files, parameterized test.

**Refs:** hogwarts policy files at `src/lib/rbac/policies/{admin,teacher,student,guardian,accountant,staff,developer,user}.ts`.

---

### Story E02.S05: Hilt-scope `Ability` per-session via `@SessionScoped` [3 pts]
**Status:** Not Started · **Sprint:** 2

**As a** ViewModel, **I want** `Ability` injected by Hilt, **So that** I don't recompute it on every screen.

**Acceptance Criteria:**
- [ ] Custom Hilt component `@SessionScoped` (alias for `@Singleton` cleared on logout).
- [ ] `Ability` provided via `@Provides @SessionScoped fun provideAbility(context: AbilityContext): Ability = AbilityRegistry.forContext(context)`.
- [ ] `SessionScopeManager.refresh()` rebuilds the scope on login / logout / role-change events.
- [ ] ViewModels inject `Ability` directly (no need to manually thread through nav args).

**Files:** `core/auth/di/ability-module.kt`, `core/auth/scope/session-scope-manager.kt`.

---

### Story E02.S06: Rewrite `TenantContext` to surface tenant + role + scoping helpers [5 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 2

**As a** repository, **I want** a single source of truth for the active tenant + user scope, **So that** I never have to manually thread `schoolId`, `userId`, `classIds`, etc. through method parameters.

**Acceptance Criteria:**
- [ ] `TenantContext` exposes `activeSchool: StateFlow<TenantInfo?>`, `activeRole: StateFlow<Role?>`, `userScope: StateFlow<UserScope>`.
- [ ] `requireSchoolId(): String` throws `NoTenantContextException` if null (existing behavior preserved).
- [ ] `UserScope` data class carries `userId`, `studentId?`, `teacherId?`, `guardianId?`, `staffMemberId?`, `classIds: Set<String>`, `childIds: Set<String>`.
- [ ] On login, `SessionRepository.refresh()` (E03.S04) populates all fields from `whoami` response.
- [ ] Backward compatible — existing `hasRole`, `hasAnyRole` keep working.

**Files:** `core/data/.../tenant/tenant-context.kt`, `core/data/.../tenant/user-scope.kt`, `core/data/.../tenant/tenant-info.kt` (extended with `nameEn` for E06.S05).

**Refs:** hogwarts `getPolicyContext()` at `src/lib/rbac/context.ts`.

---

### Story E02.S07: Detekt `RequireTenantScope` rule fully enforced across every existing DAO [5 pts]
**Status:** Not Started · **Sprint:** 3

**As a** code reviewer, **I want** every DAO query to filter by `tenantId`, **So that** cross-tenant data leakage is impossible.

**Acceptance Criteria:**
- [ ] All 26 DAOs surveyed for `@Query` methods.
- [ ] Each query's SQL string parsed; absence of `tenantId =` (or `:tenantId`) flags violation.
- [ ] ~3-5 violations expected; all fixed (add `WHERE tenantId = :tenantId`).
- [ ] Detekt rule (from E01.S09) graduates from warning to error on this module.
- [ ] Net: zero violations.

**Files:** every `core/database/dao/*-dao.kt`, plus migration to add the missing `tenantId`-filter where queries were missing it.

---

### Story E02.S08: `TenantInterceptor` reads from flow-backed `TenantContext` [2 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 3

**As a** UI, **I want** network requests to reflect the current tenant within 1 frame of switch, **So that** stale-tenant requests don't fly after logout.

**Acceptance Criteria:**
- [ ] `core/network/interceptor/tenant-interceptor.kt` reads tenant from a flow-backed `TenantProvider` rather than a snapshot at construction.
- [ ] `TenantProvider` exposes `tenantId: StateFlow<String?>`; interceptor calls `.value` per request (synchronous read OK since `StateFlow.value` is immediate).
- [ ] On logout, interceptor's reads return null → request fails closed (no stale `X-School-Id` header).

**Files:** `core/network/interceptor/tenant-interceptor.kt`, `core/data/.../tenant/tenant-provider.kt`.

---

### Story E02.S09: Logout fully clears tenant + ability + DataStore + secure prefs + Room [3 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 3

**As a** shared-device user, **I want** logout to leave no trace of my data, **So that** the next user starts clean.

**Acceptance Criteria:**
- [ ] `MainViewModel.logout()` calls a single `LogoutOrchestrator.execute()`.
- [ ] Orchestrator: invokes `authApi.logout()` → `tokenManager.clear()` → `sessionManager.clear()` → `tenantContext.reset()` → `database.clearAllTables()` → `dataStore.edit { it.clear() }` → `pushTokenRepository.unregister()` → emits `SessionState.Unauthenticated`.
- [ ] Each step wrapped in `try/catch`; failure of one step doesn't prevent others.
- [ ] Returns to `AuthGraph`; back-press doesn't restore session.
- [ ] Manual smoke test: log in → bookmark a screen → logout → log in as a different user → bookmark resolves to new user's data only.

**Files:** `app/.../main-view-model.kt`, new `core/auth/logout/logout-orchestrator.kt`.

---

### Story E02.S10: Cross-tenant integration test [5 pts]
**Status:** Not Started · **Sprint:** 3

**As a** team, **I want** automated proof of tenant isolation, **So that** we can confidently claim it in audits.

**Acceptance Criteria:**
- [ ] New `androidTest` `CrossTenantIsolationTest` at `app/src/androidTest/.../tenant/`.
- [ ] In-memory Room DB seeded with two `TenantInfo` rows (`tenant-a`, `tenant-b`) + 5 `StudentEntity` rows per tenant.
- [ ] Asserts `studentDao.observeStudents("tenant-a")` returns only tenant-a's 5 students.
- [ ] Repeated for `AttendanceDao`, `GradeDao`, `MessageDao` (3 representative DAOs).
- [ ] CI runs this on every PR touching `core/database/`.

**Files:** `app/src/androidTest/.../tenant/CrossTenantIsolationTest.kt`.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E01.S09 Detekt custom rules | Required for E02.S07 |
| Hilt 2.54 | Available |
| Existing `TenantContext` skeleton | Available at `core/data/.../tenant/tenant-context.kt` |
| Backend `whoami` endpoint | Required for E02.S06 (full population) — see E08.S04 |

---

## 4. Risks

| Risk | Mitigation |
|------|------------|
| Predicate ability layer drifts from server CASL | E02.S04 parameterized test; hogwarts policy files are the spec |
| `TenantInterceptor` flow read on every request adds overhead | `StateFlow.value` is O(1); negligible |
| Logout orchestrator races with active requests | Cancel `OkHttpClient` dispatcher in-flight calls before clearing token |
| SUPER_ADMIN → DEVELOPER rename breaks deserialization | `@SerialName("DEVELOPER", "SUPER_ADMIN")` (latter as alias for transition) |

---

## 5. Out of Scope

- Server-side authorization enforcement → that's already done in hogwarts (`src/lib/rbac/`).
- Audit log emission → E25.S07.
- Per-resource permissions UI explorer (developer surface) → defer.

---

## 6. Definition of Done

- [ ] All 10 stories merged.
- [ ] All 8 roles enumerated, serialized, and tested.
- [ ] `CrossTenantIsolationTest` passes in CI.
- [ ] Manual smoke: log in as TEACHER → confirm `/admission` not visible in nav, repository call returns `Unauthorized`.
- [ ] Manual smoke: log in as DEVELOPER → confirm `/sales` and platform-admin surfaces visible.
- [ ] Detekt `RequireTenantScope` green across all 26 DAOs.
- [ ] Captain signoff documented.
