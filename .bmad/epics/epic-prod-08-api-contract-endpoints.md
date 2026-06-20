# Epic E08: API Contract & Endpoints

**Epic ID:** EPIC-PROD-08
**Title:** API Contract & Endpoints
**Status:** Not Started
**Owner:** BMAD Dev Agent (mobile) + Web team (server)
**Priority:** P0 (Blocking — every feature epic depends on this)
**Sprint:** 4-7
**Total Points:** 89

---

## 1. Overview

Land the ~90 missing backend mobile endpoints. Specify each via OpenAPI 3.1; codegen Retrofit interfaces; replace hand-written `*-api.kt`. Feature stories in Phase D depend on these endpoints landing in the same sprint.

**Approximately half the points are server-side work** in the hogwarts repo, not kotlin-app. Splitting into a track that runs in parallel with feature completion. Mobile-side codegen + contract tests stay here; server endpoint implementations go into hogwarts repo issues.

### Business Value
- Unblocks every feature — without this, modules show empty/cached state.
- Single source of truth via OpenAPI prevents mobile/web drift.
- Pact contract tests catch breaking changes at PR time, not at runtime.

### Success Criteria
- [ ] All 25 `*-api.kt` interfaces are codegen'd from OpenAPI.
- [ ] Every endpoint returns `ApiResult`-shaped responses.
- [ ] MockWebServer contract tests on Android pass for top 30 endpoints.
- [ ] Pact contract files published; web producer verifies on every web PR.
- [ ] Manual smoke: every feature module renders real data against staging.

---

## 2. Stories

### Story E08.S01: Define OpenAPI 3.1 spec [8 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 4

**As a** mobile + web team, **I want** one source of truth for the mobile API, **So that** drift between client and server is impossible.

**Acceptance Criteria:**
- [ ] Spec at `hogwarts/spec/mobile.yaml` (or `socket-server/openapi/mobile.yaml`).
- [ ] ~90 endpoints documented with request/response schemas, headers, error codes.
- [ ] Components reused: `ApiResult`, `Pagination`, `Tenant`, `User`, `Role`, error code enums.
- [ ] Stored in hogwarts repo (server is authoritative); mobile pulls via git submodule or curl in build.
- [ ] CI lints YAML on every PR (Spectral or similar).

**Files:** `hogwarts/spec/mobile.yaml` (new in hogwarts repo).

---

### Story E08.S02: Codegen Retrofit interfaces from OpenAPI [5 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 4

**As a** Android developer, **I want** Retrofit interfaces auto-generated, **So that** I don't hand-maintain 25+ API files.

**Acceptance Criteria:**
- [ ] Gradle plugin `org.openapi.generator` 7.x produces `core/network/src/main/generated/.../api/*.kt`.
- [ ] Hand-written `*-api.kt` files migrated to extend or be replaced by generated ones.
- [ ] Generated interfaces use `kotlinx.serialization` (`@Serializable`).
- [ ] CI fails if hand-edited file conflicts with regenerated.
- [ ] Build cache hits when spec hasn't changed (sub-second).

**Files:** `core/network/build.gradle.kts`, `gradle/libs.versions.toml` (openapi-generator plugin), generated files (gitignored).

---

### Story E08.S03: Implement `/api/mobile/auth/*` (8 endpoints) [8 pts] · **Phase: Pilot v1** *(server-side)*
**Status:** Not Started · **Sprint:** 4

**As a** mobile user, **I want** registration, password reset, OAuth exchange, biometric, and school listing, **So that** I can authenticate any way the platform supports.

**Acceptance Criteria:**
- [ ] `POST /api/mobile/auth/register` → `AuthResponseDto`.
- [ ] `POST /api/mobile/auth/reset` → sends OTP email.
- [ ] `POST /api/mobile/auth/verify-otp` → validates 6-digit OTP.
- [ ] `POST /api/mobile/auth/new-password` → resets password.
- [ ] `POST /api/mobile/auth/google` accepts `{ idToken }`, validates server-side, issues JWT.
- [ ] `POST /api/mobile/auth/facebook` accepts `{ accessToken }`, validates, issues JWT.
- [ ] `POST /api/mobile/auth/biometric-enroll` returns opaque blob.
- [ ] `POST /api/mobile/auth/biometric-login` redeems blob, returns JWT pair.
- [ ] `GET /api/mobile/auth/schools?email=...` returns `[{ id, name, nameEn?, logoUrl, address, city }]`.
- [ ] All return `ApiResult` shape.
- [ ] Rate-limited per IP and per email.

**Refs:** existing hogwarts auth at `src/auth.ts`, `src/auth.config.ts`. Mirror Auth.js v5 logic, mobile-specific JWT payload.

---

### Story E08.S04: Implement `/api/mobile/auth/whoami` [3 pts] · **Phase: Pilot v1** *(server-side)*
**Status:** Not Started · **Sprint:** 4

**As a** mobile client, **I want** to refresh my session context after login or onboarding, **So that** I always have current `schoolId`, `role`, `enabledModules`, and `userScope`.

**Acceptance Criteria:**
- [ ] `GET /api/mobile/auth/whoami` returns `{ userId, role, schoolId?, enabledModules: List<String>, userScope: { studentId?, teacherId?, guardianId?, staffMemberId?, classIds: List<String>, childIds: List<String> } }`.
- [ ] Reads from JWT + DB (mirrors `auth.ts:600-639` pattern).
- [ ] If user has no `schoolId`, response includes `schoolId: null` → client routes to onboarding.

---

### Story E08.S05: Implement `/api/mobile/devices/*` (FCM tokens) [3 pts] · **Phase: Pilot v1** *(server-side)*
**Status:** Not Started · **Sprint:** 4

**As a** mobile push system, **I want** tokens registered + unregistered + tracked per device, **So that** notifications reach the right device.

**Acceptance Criteria:**
- [ ] `POST /api/mobile/devices/register` accepts `{ fcmToken, platform: "android", appVersion, deviceModel, osVersion }` → upserts `Device` row.
- [ ] `DELETE /api/mobile/devices/{id}` removes registration on logout.
- [ ] Server-side: stores in new `Device` table (`prisma/models/device.prisma` — new) with FK to `User`.
- [ ] Tokens linked to user; on user logout from device, registration removed.

---

### Story E08.S06: Implement `/api/mobile/dashboard` [3 pts] · **Phase: Pilot v1** *(server-side)*
**Status:** Not Started · **Sprint:** 4

**As a** mobile dashboard, **I want** role-specific summary tiles, **So that** I render correct numbers without 10 separate calls.

**Acceptance Criteria:**
- [ ] `GET /api/mobile/dashboard` returns role-specific aggregate JSON.
- [ ] STUDENT: today's classes count, pending assignments count, recent grade, attendance %, unread messages.
- [ ] TEACHER: classes today, pending grading, students-with-issues count.
- [ ] GUARDIAN: per-child summary array.
- [ ] ADMIN: school-wide stats.
- [ ] All cached server-side (60s TTL via Redis).

---

### Story E08.S07: Implement `/api/mobile/profile` (GET + PUT) [3 pts] · **Phase: Pilot v1** *(server-side)*
**Status:** Not Started · **Sprint:** 4

**As a** mobile user, **I want** to view + edit my profile, **So that** I can update contact info from the field.

**Acceptance Criteria:**
- [ ] `GET /api/mobile/profile` returns full user profile + role-specific fields.
- [ ] `PUT /api/mobile/profile` accepts editable fields only (name, phone, avatar, etc.).
- [ ] `POST /api/mobile/profile/avatar` accepts multipart upload, stores in S3 / Vercel Blob, returns URL.

---

### Story E08.S08: Implement `/api/mobile/dictionary` [3 pts] *(server-side)*
**Status:** Not Started · **Sprint:** 4

**As a** mobile client (E04.S03), **I want** route-scoped UI dictionaries fetchable, **So that** I can render server-driven labels.

**Acceptance Criteria:**
- [ ] `GET /api/mobile/dictionary?lang=ar&module=fees` returns `{ keys: { "fees.title": "...", "fees.dueDate": "..." } }`.
- [ ] Sourced from web's existing `dictionaries/{ar,en}/{module}.json` files.
- [ ] Cache headers: `Cache-Control: max-age=3600, public`.
- [ ] Module list documented; unknown module returns `404`.

---

### Story E08.S09: Implement `/api/mobile/students` CRUD [5 pts] *(server-side)*
**Status:** Not Started · **Sprint:** 5

**As a** mobile admin or teacher, **I want** student CRUD endpoints, **So that** SIS works mobile.

**Acceptance Criteria:**
- [ ] `GET /api/mobile/students?limit=20&offset=0&q=&grade=&status=` paginated, filterable.
- [ ] `GET /api/mobile/students/{id}` full detail.
- [ ] `POST /api/mobile/students` creates (ADMIN only).
- [ ] `PUT /api/mobile/students/{id}` updates (ADMIN only, or TEACHER for limited fields).
- [ ] `DELETE /api/mobile/students/{id}` soft-deletes (ADMIN only).
- [ ] `POST /api/mobile/students/bulk-import` accepts CSV multipart.
- [ ] All scope by `schoolId` from JWT.
- [ ] DTO shapes match mobile entity (E12).

---

### Story E08.S10: Implement `/api/mobile/{staff,teachers,classes,sections,subjects,classrooms}` (read + update) [8 pts] *(server-side)*
**Status:** Not Started · **Sprint:** 5

**As a** mobile SIS feature, **I want** the supporting taxonomies, **So that** I can render dropdowns and detail screens.

**Acceptance Criteria:**
- [ ] Each resource: `GET` list (paginated), `GET /{id}` detail.
- [ ] Limited PUT for admin: classes/sections/classrooms editable inline.
- [ ] Subjects + grade levels return localized via E06 pattern.
- [ ] All scope by `schoolId`.

---

### Story E08.S11: Implement `/api/mobile/attendance/*` (~12 endpoints) [8 pts] · **Phase: Pilot v1** *(server-side)*
**Status:** Not Started · **Sprint:** 5

**As a** mobile attendance feature, **I want** complete read+write+QR+excuse+intervention endpoints, **So that** the largest module (E13) works end-to-end.

**Acceptance Criteria:**
- [ ] `GET /api/mobile/attendance/student/{id}?from=&to=`, `GET /api/mobile/attendance/class/{id}?date=`.
- [ ] `POST /api/mobile/attendance/mark`, `POST /api/mobile/attendance/bulk`.
- [ ] `POST /api/mobile/attendance/qr-session` (teacher creates), `POST /api/mobile/attendance/qr-scan` (student scans).
- [ ] `POST /api/mobile/attendance/excuse` (submit excuse with attachments).
- [ ] `GET/POST /api/mobile/attendance/interventions`.
- [ ] `GET /api/mobile/attendance/analytics?studentId=...`.
- [ ] `GET /api/mobile/attendance/badges/{studentId}` (gamification).
- [ ] `POST /api/mobile/attendance/hall-pass`.

---

### Story E08.S12: Implement `/api/mobile/grades/*` (~6 endpoints) [5 pts] · **Phase: Pilot v1** *(server-side)*
**Status:** Not Started · **Sprint:** 5

**Acceptance Criteria:**
- [ ] `GET /api/mobile/grades/student/{id}?term=`, `GET /api/mobile/grades/summary/{id}`.
- [ ] `POST /api/mobile/grades` (teacher single), `POST /api/mobile/grades/bulk` (teacher batch).
- [ ] `GET /api/mobile/report-cards/{studentId}?term=`.
- [ ] `GET /api/mobile/report-cards/{id}/pdf`.

---

### Story E08.S13: Implement `/api/mobile/exams/*` (~7 endpoints) [5 pts] *(server-side)*
**Status:** Not Started · **Sprint:** 5

**Acceptance Criteria:**
- [ ] `GET /api/mobile/exams?status=upcoming|past`.
- [ ] `GET /api/mobile/exams/{id}`.
- [ ] `GET /api/mobile/exams/{id}/start` (server returns paper + token).
- [ ] `POST /api/mobile/exams/{id}/submit-answer` (per-question save).
- [ ] `POST /api/mobile/exams/{id}/finish`.
- [ ] `GET /api/mobile/exams/{id}/result`.
- [ ] `GET /api/mobile/exams/{id}/certificate`.

---

### Story E08.S14: Implement `/api/mobile/timetable/*` (~3 endpoints) [3 pts] · **Phase: Pilot v1** *(server-side)*
**Status:** Not Started · **Sprint:** 5

**Acceptance Criteria:**
- [ ] `GET /api/mobile/timetable/{userId}?from=&to=` returns user's schedule.
- [ ] `GET /api/mobile/timetable/class/{classId}` for teachers.
- [ ] `GET /api/mobile/timetable/today` (convenience).

---

### Story E08.S15: Implement `/api/mobile/translate` [3 pts] *(server-side)*
**Status:** Not Started · **Sprint:** 5

**As a** Android client (E06.S04), **I want** to translate text via the server, **So that** the cache is shared between web and mobile.

**Acceptance Criteria:**
- [ ] `POST /api/mobile/translate` `{ text, sourceLang, targetLang }` → `{ translatedText, fromCache, provider }`.
- [ ] Reuses web's `translateWithCache(text, sourceLang, targetLang, schoolId)` from `src/components/translation/actions.ts`.
- [ ] Rate-limited 50 calls/min/user.
- [ ] Returns 400 if `sourceLang == targetLang`.

---

### Story E08.S16: Implement `/api/mobile/{fees,invoices,payments,scholarships,fines}` (~10 endpoints) [5 pts] · **Phase: Pilot v1** *(server-side)*
**Status:** Not Started · **Sprint:** 6

**Acceptance Criteria:**
- [ ] `GET /api/mobile/fees?studentId=`, `GET /api/mobile/fees/summary/{studentId}`.
- [ ] `GET /api/mobile/invoices`, `GET /api/mobile/invoices/{id}`.
- [ ] `POST /api/mobile/payments` (Stripe — creates PaymentIntent → returns clientSecret).
- [ ] `GET /api/mobile/payments/{id}/receipt-pdf`.
- [ ] `GET /api/mobile/scholarships?studentId=`, `POST /api/mobile/scholarships/apply`.
- [ ] `GET /api/mobile/fines`, `POST /api/mobile/fines/{id}/pay`.

---

### Story E08.S17: Implement `/api/mobile/{announcements,notifications}` (~5 endpoints) [5 pts] · **Phase: Pilot v1** *(server-side)*
**Status:** Not Started · **Sprint:** 6

**Acceptance Criteria:**
- [ ] `GET /api/mobile/announcements?type=`, `GET /api/mobile/announcements/{id}`.
- [ ] `GET /api/mobile/notifications?since=`, `POST /api/mobile/notifications/{id}/read`, `POST /api/mobile/notifications/read-all`.

---

### Story E08.S18: Implement `/api/mobile/messaging/*` (REST baseline; real-time in E09) [3 pts] *(server-side)*
**Status:** Not Started · **Sprint:** 6

**Acceptance Criteria:**
- [ ] `GET /api/mobile/conversations`, `POST /api/mobile/conversations`.
- [ ] `GET /api/mobile/conversations/{id}/messages?before={cursor}`.
- [ ] `POST /api/mobile/conversations/{id}/messages` (with attachments).
- [ ] `POST /api/mobile/conversations/{id}/read`.
- [ ] `GET /api/mobile/conversations/search?q=`.

---

### Story E08.S19: Implement `/api/mobile/lms/*` (~8 endpoints) [5 pts] *(server-side)*
**Status:** Not Started · **Sprint:** 6

**Acceptance Criteria:**
- [ ] `GET /api/mobile/lms/courses?category=`, `GET /api/mobile/lms/courses/{id}`.
- [ ] `GET /api/mobile/lms/courses/{id}/chapters`, `GET /api/mobile/lms/lessons/{id}`.
- [ ] Lesson endpoint returns signed/expiring `contentUrl` for video assets.
- [ ] `POST /api/mobile/lms/courses/{id}/enroll`.
- [ ] `POST /api/mobile/lms/lessons/{id}/progress` (heartbeat).
- [ ] `GET /api/mobile/lms/courses/{id}/certificate`.

---

### Story E08.S20: Implement `/api/mobile/{admission,events,library,id-card,quiz}` (~8 endpoints) [5 pts] · **Phase: Pilot v1** *(server-side)*
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] Admission: list + detail + create + status check (public, OTP-gated).
- [ ] Events: list + detail + RSVP.
- [ ] Library: catalog + book detail + my-borrowings + borrow request.
- [ ] ID card: get current student's ID card data + Apple/Google Wallet pass blobs.
- [ ] Quiz: list + start + submit answer + leaderboard.

---

### Story E08.S21: MockWebServer-based contract tests on Android [5 pts]
**Status:** Not Started · **Sprint:** 7

**As a** Android team, **I want** to verify response shapes locally, **So that** I catch drift before integration tests.

**Acceptance Criteria:**
- [ ] `core/network/src/test/.../contract/*.kt` validates response shapes for top 30 endpoints.
- [ ] Each test: enqueue canned response → call API → assert deserialized model is well-formed.
- [ ] Failures on schema drift (e.g. server adds new required field).
- [ ] CI runs on PRs touching `core/network/` or `*/data/remote/`.

**Files:** `core/network/src/test/.../contract/*.kt` × 30+.

---

### Story E08.S22: Pact contract publishing pipeline [3 pts]
**Status:** Not Started · **Sprint:** 7

**As a** team, **I want** Pact-style consumer-driven contracts, **So that** web team's PRs can verify they don't break mobile.

**Acceptance Criteria:**
- [ ] Pact JVM library added to test deps.
- [ ] Mobile is the consumer; tests publish Pact files to a Pact Broker.
- [ ] Web consumer-side verifies on every web PR via GitHub Actions.
- [ ] Broker hosting: Pactflow free tier or self-hosted.

**Files:** `core/network/src/test/.../pact/*.kt`, `.github/workflows/pact-publish.yaml`.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| Hogwarts web backend repo write access | Required |
| Mobile auth login + refresh endpoints | Done |
| Existing 70+ Prisma models | Available |
| Server-side `getTenantContext()` (`src/lib/tenant-context.ts`) | Available |
| Web's CASL ability layer | Available — re-used for endpoint authorization |

---

## 4. Risks

| Risk | Mitigation |
|------|------------|
| Server team velocity bottlenecks all features | Split: codegen + contract tests on Android run independently; server work tracked as separate hogwarts issues |
| OpenAPI spec drift from implementation | CI on hogwarts validates implementation against spec via Spectral or middleware test |
| Codegen produces idiomatic Kotlin? | Generator is mature; minor post-processing acceptable |
| Pact broker hosting cost | Pactflow free tier ≤5 contracts; self-host on Vercel-hosted Postgres if exceeded |

---

## 5. Out of Scope

- WebSocket / Socket.IO endpoints → E09.
- File-upload endpoints (S3 pre-signed URLs) → handled per-feature where needed (avatar, attachments, ID card).
- Webhook endpoints → defer.

---

## 6. Definition of Done

- [ ] All 22 stories merged across mobile + hogwarts.
- [ ] OpenAPI spec lints green; CI catches drift.
- [ ] All 25 mobile API interfaces are codegen'd.
- [ ] MockWebServer tests pass for top 30 endpoints.
- [ ] Pact files publish; web verification job exists.
- [ ] Manual smoke: every feature module renders real data against staging tenant.
- [ ] Captain signoff documented.
