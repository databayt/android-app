# Exploring Hogwarts Android

## Quick Start

```bash
# 1. Set Java
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"

# 2. Build
./gradlew assembleDebug

# 3. Install on emulator/device
./gradlew installDebug

# 4. Run tests
./gradlew testDebugUnitTest
```

---

## Demo Accounts

All accounts use password **`1234`**.

### Demo School (development)

> Domain: `demo` | Local: `http://demo.localhost:3000`

| Email | Role | What You See |
|-------|------|-------------|
| `admin@databayt.org` | Admin | Full school management: students, staff, classes, stats |
| `teacher@databayt.org` | Teacher | Class list, batch attendance, grade entry, schedule |
| `student@databayt.org` | Student | Grades, attendance, timetable, fees, courses |
| `parent@databayt.org` | Guardian | Children overview, grades, fees, messages |
| `accountant@databayt.org` | Accountant | Fee management, invoices, transactions |
| `staff@databayt.org` | Staff | Staff-level access |

Bulk accounts also exist: `teacher1@databayt.org` through `teacher99@databayt.org`, `student1@databayt.org` through `student999@databayt.org`, `parent1@databayt.org` through `parent1999@databayt.org`.

### King Fahad School (production pilot)

> Domain: `kingfahad` | Production: `https://kingfahad.databayt.org`

| Email | Role |
|-------|------|
| `admin@kingfahad.edu` | Admin |
| `applicant@kingfahad.edu` | Applicant |

### Platform Accounts (no school)

| Email | Role | Purpose |
|-------|------|---------|
| `dev@databayt.org` | Developer | SaaS dashboard, cross-school access |
| `user@databayt.org` | User | Fresh onboarding flow |

---

## Backend API

The mobile app connects to `https://ed.databayt.org/api/`. Currently only `/api/mobile/auth` exists on the backend. All other 127 Retrofit endpoints hit non-existent routes (returns errors). This means:

- **Login works** against the real backend
- **All other data screens** show cached Room data or empty/error states
- You can explore the full UI by reading the code, but live data requires implementing the backend endpoints (Epic V2-01 in the plan)

---

## App Flow

### Auth Flow

```
Splash (holds until session check)
  |
  +-- Has valid token --> Dashboard
  |
  +-- No token --> Welcome
                     |
                     +-- "Login" --> Login Screen
                     |                |-- Email + Password
                     |                |-- Google Sign-In (Credential Manager)
                     |                |-- Facebook (not yet configured)
                     |                |-- Biometric (if set up)
                     |                |-- "Forgot Password" --> Email --> OTP --> New Password
                     |                +-- Success --> Dashboard
                     |
                     +-- "Join" --> Sign Up Screen
                                    |-- First name, Last name, Email, Password, School
                                    +-- Success --> OTP Verify --> Dashboard
```

### Dashboard (role-based)

After login, the dashboard shows different cards based on your role:

| Role | Dashboard Cards |
|------|----------------|
| **Student** | Attendance, Grades, Timetable, Fees, Courses, Events |
| **Teacher** | My Classes, Attendance, Grades, Students, Schedule, Lesson Plans |
| **Guardian** | Children, Attendance, Grades, Fees, Messages, Meetings |
| **Admin** | Students, Staff, Classes, Attendance, Grades, Fees, Reports, Settings |

Bottom tabs: **Home** | **Messages** | **Notifications** | **Settings**

### Key Feature Flows

#### Fees (6 screens)
```
Dashboard --> Fees --> Fee Balance Dashboard
                         |
                         +-- Invoice List --> Invoice Detail --> Payment Processing --> Receipt
                         |
                         +-- Transaction History
```

#### Stream / LMS (7 screens)
```
Dashboard --> Course Catalog --> Course Detail --> Chapter List
                                                      |
                                                      +-- Video Lesson (Media3 player)
                                                      +-- Text Lesson (HTML/markdown)
                                                      +-- Quiz
                                                      |
                                                  Course Progress --> Certificate
```

#### Exams (6 screens)
```
Dashboard --> Exams --> Exam Detail --> Online Exam --> Results --> Certificate
                 |
                 +-- Question Bank
                 +-- Quick Quiz
```

#### Teacher Flow (5 screens)
```
Dashboard --> My Classes --> Class Detail --> Batch Attendance
                                        |--> Grade Entry
                                        |--> Student Roster --> Student Detail
```

#### Guardian Flow (7+ screens)
```
Dashboard --> Children List --> Child --> Attendance
                                    |--> Grades
                                    |--> Fees
                                    |--> Timetable
                                    |--> Messages
                                    |--> Meeting Booking
                                    |--> Consent Forms
```

#### Admission (3 screens)
```
Dashboard --> Applications --> New Application Form
                          |--> Application Status (track existing)
```

#### Quiz Game (6 screens)
```
Dashboard --> Game Hub --> Practice Mode
                     |--> Timed Challenge
                     |--> Tournament
                     |--> Leaderboard
                     |--> Achievements
                     +--> [any mode] --> Quiz Session
```

---

## Project Structure

```
kotlin-app/
  app/                          # Main app module
    main-activity.kt            # Single-activity entry point
    main-view-model.kt          # Auth state, session, locale
    hogwarts-application.kt     # Hilt, WorkManager, StrictMode
    navigation/
      hogwarts-nav-host.kt      # 200 routes, all wired here

  core/                         # Shared infrastructure
    common/                     # Result type, LocaleFormatter
    connectivity/               # Network state monitor
    data/                       # AppPreferences (DataStore), TenantContext
    database/                   # Room DB: 42 entities, DAOs
    designsystem/               # Theme, fonts, atoms, Apple HIG
    network/                    # Retrofit, OkHttp, auth interceptor
    push/                       # Firebase FCM
    security/                   # TokenManager, CredentialManager, BiometricHelper
    sync/                       # WorkManager sync, conflict resolution

  feature/                      # 25 feature modules
    <feature>/
      ui/                       # Screens, ViewModels, UiState
        <feature>-screen.kt
        <feature>-view-model.kt
        <feature>-ui-state.kt
        components/             # Feature-specific composables
      domain/
        model/                  # Domain models
        usecase/                # Business logic
        validation/             # Input validators
      data/
        repository/             # Data access (offline-first)
        remote/                 # Retrofit API interface + DTOs
        local/                  # Room DAOs + entities
      navigation/               # Nav graph registration
```

---

## Browsing the Code

### Start Here

| Want to understand... | Read this file |
|----------------------|----------------|
| How the app launches | `app/.../main-activity.kt` |
| Auth state & session | `app/.../main-view-model.kt` |
| All navigation routes | `app/.../navigation/hogwarts-nav-host.kt` |
| Theme & RTL | `core/designsystem/.../theme/theme.kt` |
| Typography (Rubik + Arabic) | `core/designsystem/.../theme/typography.kt` |
| Color tokens | `core/designsystem/.../theme/color.kt` |
| Token storage | `core/security/.../token-manager.kt` |
| Database schema | `core/database/.../hogwarts-database.kt` |
| API endpoints | `feature/*/data/remote/*-api.kt` (any module) |

### Design System

```bash
# Reusable atoms (17):
ls core/designsystem/src/main/java/.../atom/
# -> empty-state, form-error, hogwarts-button, hogwarts-card,
#    hogwarts-text-field, loading-indicator, offline-banner,
#    search-bar, social-auth-buttons, status-badge, sync-status-banner,
#    user-avatar, divider-with-text, ...

# Apple HIG patterns (7):
ls core/designsystem/src/main/java/.../apple/
# -> apple-bottom-sheet, apple-icons, apple-inset-grouped-list,
#    apple-large-title-scaffold, apple-shapes, apple-spacing, ...

# Components (6):
ls core/designsystem/src/main/java/.../component/
# -> apple-large-title-scaffold, bottom-sheet-detents,
#    context-menu, haptic-feedback, skeleton-loading, ...
```

### i18n

```bash
# English strings (1,351 total):
find . -path "*/values/strings.xml" -not -path "*/build/*"

# Arabic strings (1,350 total):
find . -path "*/values-ar/strings.xml" -not -path "*/build/*"

# Switch language in-app: Settings > Language > Arabic
# Entire UI flips to RTL with Noto Sans Arabic font
```

### Tests

```bash
# All test files:
find . -name "*test*.kt" -path "*/test/*" -not -path "*/build/*"

# Run specific module tests:
./gradlew :feature:auth:testDebugUnitTest
./gradlew :feature:dashboard:testDebugUnitTest
./gradlew :core:sync:testDebugUnitTest
```

| Module | Tests | What's Covered |
|--------|-------|---------------|
| auth | 111 | Login, signup, forgot password, OTP, validators, all use cases |
| dashboard | 12 | ViewModel state, role-based cards |
| grades | 16 | Grade list, summary, GPA calculation |
| attendance | 14 | Mark attendance, status tracking |
| sync | 31 | Sync workers, conflict resolution, mutation queue |
| app (deep links) | 14 | Deep link routing to correct screens |

---

## Key Architectural Patterns

### Multi-Tenant Isolation
Every API call and Room query includes `schoolId`. The `TenantContext` provides it from the JWT claims.

### Offline-First
1. Show cached Room data immediately
2. Fetch from API in background
3. Update UI when fresh data arrives
4. Queue mutations when offline (PendingOperationDao)

### MVVM + Clean Architecture
```
Screen (Compose) --> ViewModel (StateFlow) --> UseCase --> Repository
                                                              |
                                                     +--------+--------+
                                                     |                 |
                                                  Remote           Local
                                                (Retrofit)        (Room)
```

### Type-Safe Navigation
All routes are `@Serializable` data objects/classes. No string-based routing.

```kotlin
// Route definition
@Serializable data class CourseDetail(val courseId: String)

// Navigate
navController.navigate(CourseDetail(courseId = "abc123"))
```

---

## What's Working vs. Not Yet

| Area | Status | Notes |
|------|--------|-------|
| Login (email/password) | Backend exists | Works against `ed.databayt.org` |
| Google Sign-In | Credential Manager wired | Needs `google_web_client_id` in strings |
| All 98 screens | UI complete | Render correctly, show placeholders without backend |
| RTL / Arabic | Fully translated | 1,350 Arabic strings, Noto Sans Arabic font |
| Room database | 42 entities | Schema ready, offline cache works |
| 127 API endpoints | Retrofit interfaces defined | Backend routes not yet created (Epic V2-01) |
| Real-time messaging | UI exists | Socket.IO not yet integrated (Epic V2-07) |
| Video player | Media3 imported | PiP, downloads not yet implemented (Epic V2-06) |
| Push notifications | FCM service exists | Needs `google-services.json` for production |
| Release APK | 8.2 MB, R8 minified | Builds successfully |
