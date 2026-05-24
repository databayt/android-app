# Epic V2-01: Backend Mobile API Layer

**Epic ID:** EPIC-V2-01
**Title:** Backend Mobile API Layer
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0 (Blocking)
**Sprint:** 12-13
**Total Points:** 55

---

## 1. Overview

The mobile app has 90+ Retrofit calls across 25 feature modules targeting non-existent `api/mobile/*` endpoints. Only `POST /api/mobile/auth` (login) and `PUT /api/mobile/auth` (refresh) exist on the Hogwarts backend. This epic creates all missing endpoints.

### Business Value
- Unblocks every feature module in the mobile app
- Enables real data display instead of empty screens
- Creates the contract between mobile and web backend

### Success Criteria
- All mobile Retrofit interfaces have matching backend endpoints
- Endpoints return JSON matching mobile DTO shapes (snake_case)
- All endpoints scope by schoolId from JWT for multi-tenant isolation
- Contract tests pass against staging

---

## 2. Stories

### Story V2-01-S01: Core Data API Endpoints [13 pts]
**Status:** Not Started
**Sprint:** 12

**As a** mobile app user,
**I want** the backend to serve student, grade, attendance, timetable, and fee data,
**So that** core feature modules display real data.

**Acceptance Criteria:**
- [ ] GET `api/mobile/students` + `api/mobile/students/{id}` scoped by schoolId from JWT
- [ ] GET `api/mobile/grades/student/{studentId}` + `api/mobile/grades/summary/{studentId}`
- [ ] GET `api/mobile/attendance/student/{studentId}`, GET `api/mobile/attendance/class/{classId}`
- [ ] POST `api/mobile/attendance/mark`, POST `api/mobile/attendance/bulk`
- [ ] GET `api/mobile/timetable/{userId}`
- [ ] GET `api/mobile/fees`, `api/mobile/fees/summary/{studentId}`, `api/mobile/invoices`
- [ ] All require Bearer token, extract schoolId from JWT claims
- [ ] JSON shapes match mobile DTOs (`@SerialName` snake_case)
- [ ] Pagination via `limit`/`offset` query params

**Technical Notes:**
- Create Next.js API routes under `hogwarts/src/app/api/mobile/`
- DTO contracts in each module's `data/remote/` directory
- Every endpoint scopes by schoolId matching web's `getTenantContext()` pattern

---

### Story V2-01-S02: Auth Supplementary Endpoints [8 pts]
**Status:** Not Started
**Sprint:** 12

**As a** mobile user,
**I want** registration, password reset, OAuth exchange, and school listing,
**So that** I can create accounts and use social login.

**Acceptance Criteria:**
- [ ] POST `api/mobile/auth/register` -> AuthResponseDto
- [ ] POST `api/mobile/auth/reset` -> sends OTP email
- [ ] POST `api/mobile/auth/verify-otp` -> validates OTP
- [ ] POST `api/mobile/auth/new-password` -> resets password
- [ ] POST `api/mobile/auth/google` accepts `{ idToken }` -> validates server-side -> AuthResponseDto
- [ ] POST `api/mobile/auth/facebook` accepts `{ accessToken }` -> validates -> AuthResponseDto
- [ ] GET `api/mobile/schools` -> list of `{ id, name, logoUrl, address }`

**Technical Notes:**
- OAuth: backend receives token, validates server-side, issues JWT pair
- Contract: `feature/auth/data/remote/auth-api.kt`
- Protect `dev@databayt.org` per auth rules

---

### Story V2-01-S03: Messaging & Notification API [8 pts]
**Status:** Not Started
**Sprint:** 13

**As a** mobile user,
**I want** conversation, message, and notification endpoints,
**So that** messaging and notifications work.

**Acceptance Criteria:**
- [ ] GET/POST `api/mobile/conversations`
- [ ] GET `api/mobile/conversations/{id}/messages` with `before` cursor pagination
- [ ] POST `api/mobile/conversations/{id}/messages` sends a message
- [ ] POST `api/mobile/conversations/{id}/read` marks messages as read
- [ ] GET `api/mobile/notifications` paginated
- [ ] POST `api/mobile/notifications/{id}/read`, POST `api/mobile/notifications/read-all`
- [ ] POST `api/mobile/devices` registers FCM token

---

### Story V2-01-S04: Stream/LMS API Endpoints [8 pts]
**Status:** Not Started
**Sprint:** 13

**As a** mobile user,
**I want** course catalog, enrollment, progress, and certificate endpoints,
**So that** the LMS module works end-to-end.

**Acceptance Criteria:**
- [ ] GET `api/mobile/courses` with category/search filters
- [ ] GET `api/mobile/courses/{id}`, GET `api/mobile/courses/{id}/chapters`
- [ ] GET `api/mobile/courses/{courseId}/lessons/{lessonId}` returns signed/expiring contentUrl
- [ ] POST `api/mobile/courses/{id}/enroll`
- [ ] POST `api/mobile/courses/{courseId}/lessons/{lessonId}/progress`
- [ ] GET `api/mobile/courses/{id}/certificate`

**Technical Notes:**
- Contract: `feature/stream/data/remote/stream-api.kt`
- Room entities: `core/database/entity/course-entity.kt`

---

### Story V2-01-S05: Secondary Feature API Endpoints [13 pts]
**Status:** Not Started
**Sprint:** 14

**As a** mobile user,
**I want** all remaining feature modules backed by real endpoints,
**So that** no module has dead API stubs.

**Acceptance Criteria:**
- [ ] Events: GET/POST list, GET detail, POST/DELETE register, GET calendar
- [ ] Admission: GET applications, GET detail, POST create, PUT update
- [ ] Library: GET catalog, GET book detail, GET my-borrowings
- [ ] Subjects: GET catalog, GET detail, GET my-subjects
- [ ] Report Cards: GET list, GET detail, GET PDF
- [ ] Guardian: GET children, GET child detail + attendance/grades/fees/timetable
- [ ] Teacher: GET classes, POST batch attendance, POST grade entry
- [ ] Admin: GET/PUT school info, GET staff, GET stats
- [ ] Profile: GET/PUT profile
- [ ] Exams advanced, Quiz Game, ID Card, Lessons

---

### Story V2-01-S06: API Contract Testing Harness [5 pts]
**Status:** Not Started
**Sprint:** 12

**As a** developer,
**I want** automated contract tests verifying Retrofit interfaces,
**So that** API drift is caught immediately.

**Acceptance Criteria:**
- [ ] MockWebServer unit tests with recorded responses
- [ ] Integration test suite runnable against staging
- [ ] Each API has happy-path + error-path test
- [ ] CI runs on PRs touching `data/remote/`

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| Hogwarts web backend access | Available |
| Mobile auth endpoint | Done (POST/PUT /api/mobile/auth) |
| Prisma models (data source) | Done (70 model files) |

---

## 4. Risks

| Risk | Mitigation |
|------|------------|
| Backend changes break mobile | Contract tests catch drift |
| DTO shape mismatch | Mobile DTOs define the contract; backend must match |
| Performance under load | Pagination, caching, proper indexing |
