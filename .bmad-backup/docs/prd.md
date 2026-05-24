# Product Requirements Document (PRD)

## Hogwarts Android - Native Mobile Companion App

**Version:** 1.0
**Author:** BMAD PM Agent
**Date:** 2025-01-19
**Status:** Approved

---

## 1. Executive Summary

### 1.1 Vision Statement
> "Education at your fingertips - Native Android companion for Hogwarts school automation"

### 1.2 Problem Statement
Schools using the Hogwarts web platform lack a native mobile experience for:
- Students checking grades and attendance on-the-go
- Teachers marking attendance and communicating with parents
- Guardians monitoring child progress and receiving notifications
- All users needing offline access and push notifications

### 1.3 Solution
A native Android app that integrates seamlessly with the Hogwarts web backend, providing:
- Offline-first architecture with background sync
- Push notifications via Firebase FCM
- Biometric authentication
- Full RTL support for Arabic users
- Multi-tenant isolation (schoolId-scoped data)

---

## 2. Target Users

### 2.1 User Personas

| Persona | Role | Primary Needs |
|---------|------|---------------|
| **Sarah (Student)** | High school student | View grades, check schedule, see attendance |
| **Mr. Ahmed (Teacher)** | Arabic language teacher | Mark attendance, submit grades, message parents |
| **Fatima (Guardian)** | Parent of 2 students | Monitor progress, pay fees, receive notifications |
| **Admin Khalid** | School administrator | View dashboards, manage users, run reports |

### 2.2 User Distribution
- Students: 60%
- Guardians: 25%
- Teachers: 10%
- Admins: 5%

---

## 3. Functional Requirements

### 3.1 Authentication (Epic 1)

| ID | Requirement | Priority | Status |
|----|-------------|----------|--------|
| AUTH-01 | Email/password login via Hogwarts backend | P0 | Done |
| AUTH-02 | JWT token storage in EncryptedSharedPreferences | P0 | Done |
| AUTH-03 | Token refresh mechanism | P0 | Done |
| AUTH-04 | Biometric unlock after initial login | P1 | Pending |
| AUTH-05 | OAuth (Google/Facebook) login | P2 | Pending |
| AUTH-06 | Logout with token invalidation | P0 | Pending |

### 3.2 Dashboard & Navigation (Epic 2)

| ID | Requirement | Priority | Status |
|----|-------------|----------|--------|
| DASH-01 | Role-based dashboard (Student/Teacher/Guardian/Admin) | P0 | Pending |
| DASH-02 | Bottom navigation with role-specific tabs | P0 | Pending |
| DASH-03 | Quick action cards | P1 | Pending |
| DASH-04 | Real-time notifications badge | P1 | Pending |
| DASH-05 | Offline indicator banner | P0 | Pending |

### 3.3 Student Module (Epic 3)

| ID | Requirement | Priority | Status |
|----|-------------|----------|--------|
| STU-01 | View grades by subject | P0 | Pending |
| STU-02 | View attendance history | P0 | Pending |
| STU-03 | View class schedule/timetable | P0 | Pending |
| STU-04 | View assignments | P1 | Pending |
| STU-05 | Submit homework (file upload) | P2 | Pending |

### 3.4 Teacher Module (Epic 4)

| ID | Requirement | Priority | Status |
|----|-------------|----------|--------|
| TCH-01 | Mark attendance for class | P0 | Pending |
| TCH-02 | Submit grades | P0 | Pending |
| TCH-03 | View assigned classes | P0 | Pending |
| TCH-04 | Message individual parents | P1 | Pending |
| TCH-05 | Broadcast to class parents | P1 | Pending |

### 3.5 Guardian Module (Epic 5)

| ID | Requirement | Priority | Status |
|----|-------------|----------|--------|
| GRD-01 | View children's grades | P0 | Pending |
| GRD-02 | View children's attendance | P0 | Pending |
| GRD-03 | Pay fees (payment gateway) | P1 | Pending |
| GRD-04 | Communicate with teachers | P1 | Pending |
| GRD-05 | View school announcements | P0 | Pending |

### 3.6 Messaging & Notifications (Epic 6)

| ID | Requirement | Priority | Status |
|----|-------------|----------|--------|
| MSG-01 | Firebase FCM push notifications | P0 | Pending |
| MSG-02 | In-app notification center | P0 | Pending |
| MSG-03 | Real-time chat with teachers/parents | P1 | Pending |
| MSG-04 | Notification preferences | P1 | Pending |

### 3.7 Offline Support & Sync (Epic 7)

| ID | Requirement | Priority | Status |
|----|-------------|----------|--------|
| OFF-01 | Room database for local caching | P0 | Done |
| OFF-02 | Background sync with WorkManager | P0 | Pending |
| OFF-03 | Optimistic UI updates | P1 | Pending |
| OFF-04 | Conflict resolution strategy | P1 | Pending |
| OFF-05 | Offline mutation queue | P1 | Pending |

---

## 4. Non-Functional Requirements

### 4.1 Performance

| Metric | Target |
|--------|--------|
| App startup time | < 2 seconds |
| Screen navigation | < 300ms |
| API response display | < 1 second (cached), < 3 seconds (network) |
| Memory usage | < 150MB |
| APK size | < 30MB |

### 4.2 Security

| Requirement | Implementation |
|-------------|----------------|
| Token storage | EncryptedSharedPreferences |
| Network security | TLS 1.3, Certificate pinning |
| Biometric | BiometricPrompt API |
| Multi-tenant isolation | schoolId in all queries |

### 4.3 Accessibility

| Requirement | Implementation |
|-------------|----------------|
| RTL support | Full Arabic/English |
| Screen readers | TalkBack compatibility |
| Font scaling | System preference respect |
| Color contrast | WCAG AA compliance |

### 4.4 Compatibility

| Requirement | Value |
|-------------|-------|
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |
| Supported ABIs | arm64-v8a, armeabi-v7a, x86_64 |

---

## 5. Technical Architecture

### 5.1 Stack

| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (type-safe) |
| DI | Hilt |
| Network | Retrofit + OkHttp + kotlinx.serialization |
| Database | Room |
| Preferences | DataStore |
| Background | WorkManager |
| Push | Firebase Cloud Messaging |

### 5.2 Architecture Pattern
- MVVM + Clean Architecture
- Repository pattern with offline-first strategy
- Single source of truth (Room database)

### 5.3 Integration Points

| Integration | Method |
|-------------|--------|
| Hogwarts Backend | REST API (JWT auth) |
| Mobile Auth | POST /api/mobile/auth |
| Firebase | FCM for push notifications |

---

## 6. Success Metrics

| Metric | Target |
|--------|--------|
| Crash-free rate | > 99.5% |
| App store rating | > 4.5 stars |
| DAU/MAU ratio | > 40% |
| Offline usage | 30% of sessions |
| Push notification opt-in | > 80% |

---

## 7. Milestones

### MVP (v1.0)
- Authentication (email/password)
- Role-based dashboard
- View-only features (grades, attendance, schedule)
- Push notifications
- Offline caching

### v1.1
- Biometric login
- Teacher attendance marking
- Guardian fee payment

### v1.2
- Real-time messaging
- OAuth login
- Assignment submission

---

## 8. Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Backend API changes | High | Version API, contract tests |
| Multi-tenant data leak | Critical | schoolId in all queries, code review |
| Offline sync conflicts | Medium | Last-write-wins + manual resolution |
| FCM delivery issues | Medium | Fallback to in-app polling |

---

## 9. Approval

| Role | Name | Date |
|------|------|------|
| Product Manager | BMAD PM | 2025-01-19 |
| Tech Lead | BMAD Architect | 2025-01-19 |
| Stakeholder | - | Pending |
