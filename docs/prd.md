# Product Requirements Document: Hogwarts Android

## Overview

**Product Name**: Hogwarts Android
**Version**: 1.0.0 (MVP)
**Platform**: Android (Native)
**Backend**: Hogwarts Platform API (`https://ed.databayt.org/api/`)

### Vision

> "Education at your fingertips - Native Android companion for Hogwarts school automation"

A native Android app that provides students, teachers, guardians, and administrators with quick mobile access to the Hogwarts school management platform.

---

## User Roles

| Role | Primary Use Cases |
|------|-------------------|
| **Student** | View grades, attendance, schedule, pay fees |
| **Teacher** | Mark attendance, submit grades, communicate with parents |
| **Guardian** | Monitor child progress, receive notifications, pay fees |
| **Admin** | Overview dashboards, system notifications |

---

## Milestones

Three release phases stage delivery against the active pilot timeline. Story-level detail lives in `.bmad/epics/phase-{1,2,3}-*.md`; this section is the customer-facing summary.

### Phase 1 — Pilot v1 (King Fahad Schools, target 2026-07-12)

3 sprints, ~7 weeks, ~296 story points. Slimmest companion that supports the Jun 2026 conversion conversation with Pilot Seat 1.

- Authentication: email/password, JWT, biometric, session persistence, school selector
- Read-only views: grades, attendance, timetable, fee statement, announcements
- Notifications: FCM data + notification dual-channel, in-app center, deep links
- Admission flow: applicant multi-step form, status check (OTP-gated), tour booking, events list
- Arabic-complete UI: native-speaker translation audit, RTL correctness sweep, locale-aware formatting (SDG default)
- Foundation: explicit Room migrations, mutation queue actually transmits, FCM token upload, 8-role enum aligned, OpenAPI 3.1 contract + codegen
- Release: signed APK, Play Internal Testing track, Crashlytics + Performance + Analytics live

### Phase 2 — MENA-10 (target 2026-09-15)

5 sprints, ~10 weeks, ~411 story points. Scale from 1 pilot to 10 schools.

- Teacher write surfaces: mark attendance (single, bulk, QR), grade entry (replacing the fake `SubmitGradeUseCase`), report cards
- Real-time messaging: Socket.IO server + client, presence, typing, attachments, delivery receipts
- Payment: Stripe fee flow, receipt PDF, multi-currency
- OAuth: Google + Facebook sign-in
- Foundation cleanup: Detekt rules enforced, full RBAC ability layer, live translate API endpoint
- Quality climb: test coverage rising from < 5% to ≥ 35%

### Phase 3 — Public Launch v1.0.0 (target 2026-12-20)

8 sprints, ~16 weeks, ~408 story points. Public Play Store release with staged rollout.

- LMS, library, ID card with Apple/Google Wallet passes, quiz
- Full accessibility audit (TalkBack, WCAG AA)
- Performance: Macrobenchmark, baseline profiles
- Test coverage ≥ 80%
- v1.0.0 staged rollout to 100% in Play Store production track

---

## Technical Requirements

### Platform
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 35 (Android 15)
- **Architecture**: MVVM + Clean Architecture
- **Language**: Kotlin 2.0+

### Offline-First
- All read operations must work offline
- Local Room database for caching
- WorkManager for background sync
- Conflict resolution for mutations

### Multi-Tenant
- Every API call includes `schoolId`
- Every Room query filtered by `schoolId`
- No cross-school data leakage

### Internationalization
- Arabic (RTL) - Primary
- English (LTR) - Secondary
- Dynamic language switching

### Security
- JWT stored in EncryptedSharedPreferences
- Biometric unlock via BiometricPrompt
- Certificate pinning for API calls
- ProGuard obfuscation

---

## Success Metrics

| Metric | Target |
|--------|--------|
| Offline availability | 100% for reads |
| App launch time | < 2 seconds |
| Test coverage | 80%+ |
| Crash-free rate | 99.5%+ |
| RTL support | Full |

---

## Constraints

1. **API**: Use existing Hogwarts backend APIs (no new endpoints)
2. **Auth**: JWT tokens from existing NextAuth system
3. **Data**: Prisma models define data schema
4. **Timeline**: Phase 1 (Pilot v1) in 3 sprints / ~7 weeks. Subsequent phases per `.bmad/epics/phase-{2,3}-*.md`.

---

## Dependencies

| Dependency | Source |
|------------|--------|
| API Endpoints | Hogwarts Backend |
| Auth System | NextAuth v5 |
| Push Notifications | Firebase FCM |
| Analytics | PostHog (future) |

---

## Risks

| Risk | Mitigation |
|------|------------|
| API rate limiting | Implement caching, batch requests |
| Offline conflicts | Last-write-wins with timestamps |
| Large data sets | Pagination, lazy loading |
| Auth token expiry | Proactive refresh at 80% lifetime |

---

## Appendix

### API Base URL
- Production: `https://ed.databayt.org/api/`
- Development: `http://localhost:3000/api/`

### Related Documents
- [Architecture](./architecture.md)
- [Production Roadmap Overview](../.bmad/epics/epic-prod-overview.md)
- [Phase 1 — Pilot v1](../.bmad/epics/phase-1-pilot-v1.md)
- [Phase 2 — MENA-10](../.bmad/epics/phase-2-mena10.md)
- [Phase 3 — Public Launch](../.bmad/epics/phase-3-launch.md)
- Legacy `docs/epics/` and V1 epic files archived to `../.bmad/epics/archive/`
