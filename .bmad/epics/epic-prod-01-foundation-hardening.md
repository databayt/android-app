# Epic E01: Foundation Hardening

**Epic ID:** EPIC-PROD-01
**Title:** Foundation Hardening
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0 (Blocking — every other epic depends on this)
**Sprint:** 1-2
**Total Points:** 42

---

## 1. Overview

The MVP shell ships with several silent stubs that would corrupt user data, drop outbound traffic, or fail a clean build. This epic eliminates them.

### Business Value
- Fresh checkout builds clean.
- Schema bumps preserve existing user data.
- Queued mutations actually reach the server.
- Crashlytics, Performance, Analytics events appear in Firebase console.
- Lint and static analysis catch the kind of regressions that cause silent stubs to come back.

### Success Criteria
- [ ] `git clean -fdx && ./gradlew :app:assembleRelease` succeeds.
- [ ] `./gradlew :app:checkStringParity :app:lintRelease detekt spotlessCheck` all green.
- [ ] Room DB v23 → v24 schema bump preserves all rows on real device (verified by `MigrationTest`).
- [ ] Queued attendance mutation delivered within 60s of network reconnect (verified manually).
- [ ] FCM registration token round-trips to server on first auth and rotates on token refresh.
- [ ] Crashlytics dashboard shows test crash with deobfuscated stack within 5 min.

---

## 2. Stories

### Story E01.S01: Commit `google-services.json` for debug + staging + release [2 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 1

**As a** new contributor, **I want** the Firebase plugin to find its config, **So that** a fresh checkout builds without manual setup.

**Acceptance Criteria:**
- [ ] `app/google-services.json` committed (or symlinked to a build-variant-aware path).
- [ ] Fresh checkout `./gradlew :app:processDebugGoogleServices` succeeds without manual file placement.
- [ ] `staging` and `release` flavors point to separate Firebase projects.
- [ ] If license/security forbids committing, gate behind Gradle property `firebase.config.path` and document in `README.md`.

**Files:** `app/google-services.json`, `app/src/{debug,staging,release}/google-services.json` (variant-specific), `app/build.gradle.kts:8` (productFlavors).

---

### Story E01.S02: Replace `fallbackToDestructiveMigration()` with explicit migrations [8 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 1

**As a** user upgrading the app, **I want** my offline data to survive schema bumps, **So that** I don't lose attendance, messages, or grades on every release.

**Acceptance Criteria:**
- [ ] `DatabaseModule.provideDatabase` calls `addMigrations(MIGRATION_7_8, …, MIGRATION_22_23, MIGRATION_23_24)` and removes `fallbackToDestructiveMigration()`.
- [ ] Migrations 19→20, 20→21, 21→22, 22→23 authored to match the actual DB schema delta.
- [ ] Migration 23→24 adds the `lang: String DEFAULT 'ar'` column required by E06.
- [ ] Schema export at `core/database/schemas/{20,21,22,23,24}.json` exists.
- [ ] Detekt rule `NoDestructiveMigrationFallback` added (introduced in E01.S09) to prevent regression.

**Files:** `app/src/main/java/.../di/database-module.kt`, `core/database/.../migration/migration-{19-20,20-21,21-22,22-23,23-24}.kt`, `core/database/schemas/*.json`.

**Refs:** existing migrations at `core/database/.../migration/migration-7-8.kt` etc.

**Verify:** new `androidTest` `MigrationTest` validates each migration round-trip via `MigrationTestHelper`.

---

### Story E01.S03: Wire mutation queue to actually transmit [8 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 1

**As a** teacher marking attendance offline, **I want** the marks to reach the server when I'm back online, **So that** my work isn't lost.

**Acceptance Criteria:**
- [ ] Each entity-type branch in `MutationQueue.process()` (`mutation-queue.kt:59-79`) calls the corresponding Retrofit endpoint via a typed `MutationDispatcher`.
- [ ] `PendingOperationEntity` removed from queue on success.
- [ ] 5xx responses retry with exponential backoff (max 5 attempts, 30s ceiling).
- [ ] 4xx responses (except 401) surface as `SyncStatus.Error` and are dead-lettered after 1 retry.
- [ ] Outbound mutations include the active `tenantId` header (already injected by `TenantInterceptor`).
- [ ] Worker observability: each cycle logs `mutations_processed`, `mutations_succeeded`, `mutations_failed` counters to Firebase Analytics.

**Files:** `core/sync/.../mutation-queue.kt`, `core/sync/.../entity-sync-registry.kt`, new `core/sync/dispatch/{Attendance,Grade,Message,Notification,Profile,DeviceToken}MutationDispatcher.kt`.

**Refs:** `core/sync/.../sync-worker.kt` already orchestrates `mutationQueue.processAll()`. Existing tests at `core/sync/src/test/.../mutation-queue-test.kt`.

**Verify:** Existing 17 mutation-queue tests extended with 5 new entity-type-specific tests.

---

### Story E01.S04: Upload FCM token to backend [3 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 1

**As a** server operator, **I want** every device's FCM token registered, **So that** push notifications can reach the right device.

**Acceptance Criteria:**
- [ ] `notification-handler.kt:73` TODO replaced with `pushTokenRepository.register(token, deviceMeta)`.
- [ ] Token re-uploaded on each token rotation and on app cold start if last upload >30 days ago.
- [ ] Failed uploads queue via `MutationQueue` (DEVICE_TOKEN entity type from E01.S03).
- [ ] On logout, `pushTokenRepository.unregister()` clears the server registration.

**Files:** `core/push/.../notification-handler.kt`, new `core/push/.../push-token-repository.kt`, new `core/network/api/devices-api.kt` (`POST /api/mobile/devices/register`, `DELETE /api/mobile/devices/{id}`), new `core/database/dao/device-token-dao.kt`, new `core/database/entity/device-token-entity.kt`.

**Backend dependency:** E08.S05 (devices endpoint).

---

### Story E01.S05: Reconcile `BuildConfigHelper` drift with compiled `BuildConfig` [2 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 1

**As a** future reader of `BuildConfigHelper`, **I want** one source of truth for the API base URL, **So that** I don't have to chase down two divergent constants.

**Acceptance Criteria:**
- [ ] `app/.../config/build-config-helper.kt` reads `BuildConfig.API_BASE_URL` instead of hardcoding `staging-api.databayt.org` / `api.databayt.org`.
- [ ] `core/network/di/network-module.kt:BASE_URL` constant removed in favor of `BuildConfig.API_BASE_URL`.
- [ ] Both consumers route through one source.
- [ ] CI lint rule fails if any new file hardcodes a `databayt.org` URL outside `app/build.gradle.kts`.

**Files:** `app/build.gradle.kts:33-34`, `app/.../config/build-config-helper.kt`, `core/network/di/network-module.kt`.

---

### Story E01.S06: Reconcile `app/strings.xml` parity drift [1 pt] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 1

**As a** CI runner, **I want** `:app:checkStringParity` to be green, **So that** the build doesn't fail on a single missing translation.

**Acceptance Criteria:**
- [ ] `:app:checkStringParity` passes (currently 47 EN vs 46 AR).
- [ ] Either the EN-only key gets an AR translation or the EN entry is removed.

**Files:** `app/src/main/res/values/strings.xml`, `app/src/main/res/values-ar/strings.xml`.

---

### Story E01.S07: Wire `AppStartupInitializer` into `HogwartsApplication.onCreate` [2 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 1

**As a** developer triaging startup issues, **I want** the existing initializer skeleton actually invoked, **So that** I have one place to add startup hooks.

**Acceptance Criteria:**
- [ ] `AppStartupInitializer` (`app/.../startup/app-startup-initializer.kt`) invoked from `HogwartsApplication.onCreate()`.
- [ ] Registers (a) Timber tree variants per build type, (b) StrictMode in debug only, (c) WorkManager initialization status, (d) Firebase init confirmation.
- [ ] Documented with one-line `// Why:` comments per registration.

**Files:** `app/.../hogwarts-application.kt`, `app/.../startup/app-startup-initializer.kt`.

---

### Story E01.S08: Promote `Compose Text("...")` to a Detekt error [3 pts]
**Status:** Not Started · **Sprint:** 2

**As a** code reviewer, **I want** hardcoded strings inside Composables to fail CI, **So that** they don't leak past review.

**Acceptance Criteria:**
- [ ] New Detekt rule `NoHardcodedComposeText` registered in `config/detekt/detekt.yml`.
- [ ] CI fails on `Text("literal")` calls in any module.
- [ ] All existing violations (~80 estimated) fixed (move to `strings.xml`) or `@Suppress`'d with a TODO comment + linked issue.
- [ ] Custom-rules module compiles and runs in <30s.

**Files:** new `config/detekt/custom-rules/build.gradle.kts`, new `config/detekt/custom-rules/src/main/kotlin/.../NoHardcodedComposeText.kt`, new `config/detekt/detekt.yml`, root `build.gradle.kts` (register Detekt plugin).

---

### Story E01.S09: Add Detekt rules `RequireTenantScope`, `NoLeftRightPadding`, `RequireLangColumn`, `NoDestructiveMigrationFallback` [5 pts]
**Status:** Not Started · **Sprint:** 2

**As a** code reviewer, **I want** the four load-bearing invariants enforced by static analysis, **So that** they don't degrade silently.

**Acceptance Criteria:**
- [ ] All four rules implemented in `config/detekt/custom-rules/src/main/kotlin/...`.
- [ ] Each has a unit test demonstrating both violation and fix.
- [ ] `RequireTenantScope` fires on `@Query` methods that don't reference `:tenantId` (or `tenantId =`) in their SQL.
- [ ] `NoLeftRightPadding` fires on `Modifier.padding(left = …)` / `right =`.
- [ ] `RequireLangColumn` fires on `@Translatable @Entity` classes without a `lang: String` field.
- [ ] `NoDestructiveMigrationFallback` fires on `fallbackToDestructiveMigration()` / `fallbackToDestructiveMigrationOnDowngrade()`.
- [ ] Existing violations cataloged with `// detekt:suppress` + linked issues (no flag-day fixes — let E02 / E06 close them).

**Files:** `config/detekt/custom-rules/src/main/kotlin/.../{RequireTenantScope,NoLeftRightPadding,RequireLangColumn,NoDestructiveMigrationFallback}.kt`, matching `src/test/kotlin/...`.

---

### Story E01.S10: Add Spotless + ktlint with Compose preset [3 pts]
**Status:** Not Started · **Sprint:** 2

**As a** team, **I want** Kotlin formatting auto-applied, **So that** review focuses on logic not whitespace.

**Acceptance Criteria:**
- [ ] `spotless { kotlin { ktlint("1.5.0").editorConfigOverride(mapOf("ktlint_function_naming_ignore_when_annotated_with" to "Composable", …)) } }` registered at root.
- [ ] `./gradlew spotlessCheck` is green; `./gradlew spotlessApply` formats.
- [ ] CI runs `spotlessCheck` on PRs.
- [ ] `.editorconfig` aligned with ktlint defaults.

**Files:** root `build.gradle.kts`, `.editorconfig`.

---

### Story E01.S11: Add Kover + baseline coverage report [2 pts]
**Status:** Not Started · **Sprint:** 2

**As a** team, **I want** coverage measured per-module, **So that** E22's coverage goals are enforceable.

**Acceptance Criteria:**
- [ ] `kover` plugin applied to root + every module.
- [ ] `./gradlew :koverHtmlReport` produces per-module HTML at `build/reports/kover/html/index.html`.
- [ ] `./gradlew :koverXmlReport` for CI consumption.
- [ ] Coverage at baseline (no thresholds yet — set in E22.S01).

**Files:** root `build.gradle.kts`, every module `build.gradle.kts`.

---

### Story E01.S12: Promote `MissingClass` and `InvalidPackage` lint to error in all modules [3 pts]
**Status:** Not Started · **Sprint:** 2

**As a** code reviewer, **I want** missing imports / package mismatches to fail CI, **So that** runtime ClassNotFoundException doesn't ship.

**Acceptance Criteria:**
- [ ] All modules' `lint { error += setOf("HardcodedText", "MissingTranslation", "InvalidPackage", "MissingClass") }`.
- [ ] CI fails on any of the four.
- [ ] Existing violations (if any) fixed before merge.

**Files:** every `*/build.gradle.kts`.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| Firebase project access (debug/staging/release) | **Required for E01.S01** |
| Backend `POST /api/mobile/devices/register` | **Required for E01.S04** — see E08.S05 |
| Existing migration files (`migration-7-8` … `migration-18-19`) | Available |
| Existing custom Gradle task `:app:checkStringParity` | Available |
| Existing `core/sync/.../sync-worker.kt` orchestration | Available |

---

## 4. Risks

| Risk | Mitigation |
|------|------------|
| `google-services.json` license/security policy forbids committing | Gate via Gradle property; document in README; provide debug-only stub |
| Migration regression silently re-introduces `fallbackToDestructiveMigration()` | E01.S09 Detekt rule prevents it |
| Detekt rules add CI time | Run rules in parallel with lint; cache custom-rules JAR |
| Mutation queue retransmission causes duplicate writes | Server-side idempotency keys (E08 includes `Idempotency-Key` header per mutation) |
| FCM token upload fails silently | E01.S04 logs to Crashlytics + retries via mutation queue |

---

## 5. Out of Scope (deferred to other epics)

- Real cert pin values → E25.S01
- Real-time delivery via Socket.IO → E09
- Test coverage targets → E22
- Performance baseline → E23
- Play Integrity → E25.S03

---

## 6. Definition of Done

- [ ] All 12 stories merged via PR with `Closes #N`.
- [ ] `git clean -fdx && ./gradlew :app:assembleRelease detekt spotlessCheck lint` green from a clean checkout.
- [ ] `androidTest` `MigrationTest` validates v7→v24 round-trip.
- [ ] Manual smoke: kill network → mark attendance → restore → mark reaches server within 60s.
- [ ] Manual smoke: trigger test crash → see in Crashlytics within 5 min with deobfuscated stack.
- [ ] Captain signoff documented in closing PR.
