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

## MVP Features (P0)

### Epic 1: Authentication
- [ ] Email/password login
- [ ] JWT token management
- [ ] Biometric authentication (fingerprint/face)
- [ ] Session persistence
- [ ] Offline auth (cached credentials)

### Epic 2: Dashboard
- [ ] Role-specific dashboard layout
- [ ] Quick action cards
- [ ] Notification summary
- [ ] Offline indicator

### Epic 3: Student Features
- [ ] View grades by subject/term
- [ ] View attendance history
- [ ] View timetable/schedule
- [ ] View fee statements

### Epic 4: Teacher Features
- [ ] Class list view
- [ ] Mark attendance (manual)
- [ ] Mark attendance (QR scan)
- [ ] Submit grades

### Epic 5: Guardian Features
- [ ] Children list
- [ ] Child progress overview
- [ ] Attendance alerts

### Epic 6: Notifications
- [ ] Push notifications (FCM)
- [ ] In-app notification center
- [ ] Notification preferences

### Epic 7: Offline Support
- [ ] Cached data display
- [ ] Offline mutation queue
- [ ] Background sync

---

## Post-MVP Features (P1)

### Messaging
- [ ] Direct messaging
- [ ] Group announcements
- [ ] Real-time chat (Socket.IO)

### Payments
- [ ] Fee payment integration
- [ ] Payment history
- [ ] Receipt download

### Advanced Features
- [ ] Dark mode
- [ ] Widget support
- [ ] Calendar integration
- [ ] Document viewing

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
4. **Timeline**: MVP in 4 sprints

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
- [Epics](./epics/)
- [Stories](./stories/)
