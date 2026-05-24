# MVP Definition

## Hogwarts Android - Minimum Viable Product

**Version:** 1.0
**Date:** 2025-01-19
**Target Release:** Sprint 4

---

## 1. MVP Scope

### 1.1 Core Value Proposition
> Enable students, teachers, and guardians to access essential school information on mobile with offline support.

### 1.2 MVP Criteria
1. **Must work offline** - All read operations cached locally
2. **Must support all roles** - Student, Teacher, Guardian, Admin
3. **Must be secure** - JWT auth, encrypted storage
4. **Must support RTL** - Arabic as primary language

---

## 2. In-Scope Features (MVP)

### Epic 1: Authentication
| Story | Priority | Effort |
|-------|----------|--------|
| Email/password login | P0 | Done |
| Token storage & refresh | P0 | Done |
| Logout | P0 | 2 pts |
| Session persistence | P0 | 2 pts |

### Epic 2: Dashboard & Navigation
| Story | Priority | Effort |
|-------|----------|--------|
| Role-based dashboard layout | P0 | 5 pts |
| Bottom navigation | P0 | 3 pts |
| Offline indicator | P0 | 2 pts |
| Quick action cards | P1 | 3 pts |

### Epic 3: Student Features (View Only)
| Story | Priority | Effort |
|-------|----------|--------|
| View grades list | P0 | 5 pts |
| View grade details | P0 | 3 pts |
| View attendance summary | P0 | 5 pts |
| View timetable | P0 | 5 pts |

### Epic 4: Teacher Features (View Only)
| Story | Priority | Effort |
|-------|----------|--------|
| View assigned classes | P0 | 3 pts |
| View class roster | P0 | 3 pts |
| View student grades | P0 | 3 pts |

### Epic 5: Guardian Features (View Only)
| Story | Priority | Effort |
|-------|----------|--------|
| View children list | P0 | 3 pts |
| View child's grades | P0 | 3 pts |
| View child's attendance | P0 | 3 pts |

### Epic 6: Notifications (Basic)
| Story | Priority | Effort |
|-------|----------|--------|
| FCM token registration | P0 | 3 pts |
| Push notification display | P0 | 3 pts |
| Notification list screen | P0 | 5 pts |

### Epic 7: Offline Support
| Story | Priority | Effort |
|-------|----------|--------|
| Room entities for all features | P0 | 5 pts |
| Repository offline-first pattern | P0 | 5 pts |
| Background sync (basic) | P0 | 5 pts |

---

## 3. Out-of-Scope (Post-MVP)

| Feature | Reason | Target Version |
|---------|--------|----------------|
| Biometric login | Nice-to-have | v1.1 |
| OAuth (Google/Facebook) | Complexity | v1.2 |
| Teacher attendance marking | Write operation | v1.1 |
| Grade submission | Write operation | v1.1 |
| Fee payment | Third-party integration | v1.1 |
| Real-time chat | Requires WebSocket | v1.2 |
| Assignment submission | File upload complexity | v1.2 |
| Dark mode | Theme work | v1.1 |

---

## 4. Technical MVP Requirements

### 4.1 Architecture (Done)
- [x] Multi-module Gradle project
- [x] MVVM + Clean Architecture
- [x] Hilt dependency injection
- [x] Room database setup
- [x] Retrofit + OkHttp networking
- [x] Navigation Compose

### 4.2 Core Infrastructure (Partial)
- [x] JWT authentication
- [x] Encrypted token storage
- [x] Multi-tenant context (schoolId)
- [x] Design system (Material 3)
- [ ] WorkManager background sync
- [ ] FCM integration

### 4.3 Feature Modules
- [x] feature:auth (login screen done)
- [ ] feature:dashboard
- [ ] feature:grades
- [ ] feature:attendance
- [ ] feature:timetable
- [ ] feature:students
- [ ] feature:messaging

---

## 5. Sprint Plan

### Sprint 1 (Current - 80% Complete)
- [x] Project setup
- [x] Authentication flow
- [x] Login screen UI
- [ ] Logout functionality
- [ ] Session persistence

### Sprint 2
- [ ] Dashboard layout
- [ ] Bottom navigation
- [ ] Grades feature (view)
- [ ] Offline caching

### Sprint 3
- [ ] Attendance feature
- [ ] Timetable feature
- [ ] Teacher class view
- [ ] Guardian children view

### Sprint 4 (MVP Release)
- [ ] FCM notifications
- [ ] Background sync
- [ ] Bug fixes
- [ ] Play Store release

---

## 6. Success Criteria

| Metric | Target |
|--------|--------|
| Login success rate | > 99% |
| Screen load time | < 1 second |
| Offline capability | All read operations |
| Crash-free sessions | > 99.5% |
| RTL layout correctness | 100% |

---

## 7. Risks

| Risk | Mitigation |
|------|------------|
| Backend API not ready | Use mock data |
| FCM setup complexity | Start early Sprint 3 |
| Room schema changes | Migration strategy |
| Play Store rejection | Follow guidelines |

---

## 8. Definition of Done (MVP)

- [ ] All P0 stories completed
- [ ] Unit test coverage > 80%
- [ ] UI tests for all screens
- [ ] No critical bugs
- [ ] Works offline
- [ ] RTL support verified
- [ ] Play Store listing ready
- [ ] APK size < 30MB
