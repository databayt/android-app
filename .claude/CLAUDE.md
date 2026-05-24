# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Quick Start

**Hogwarts Android** is a native companion app for the Hogwarts school automation platform, providing mobile access for students, teachers, guardians, and admins.

### Build Commands

```bash
./gradlew build              # Full build
./gradlew assembleDebug      # Debug APK
./gradlew installDebug       # Install on device/emulator
./gradlew testDebugUnitTest  # Unit tests
./gradlew connectedDebugAndroidTest  # Instrumented tests
./gradlew ktlintCheck        # Lint check
./gradlew detekt             # Static analysis
```

### Critical Rules

1. **Always include schoolId** in all API calls and Room queries (multi-tenant isolation)
2. **Follow feature-based structure** - each feature has standardized files
3. **Use semantic tokens** - never hardcode colors (`MaterialTheme.colorScheme`)
4. **Use semantic typography** - `MaterialTheme.typography`, not hardcoded styles
5. **Offline-first** - show cached data, sync in background
6. **kebab-case file naming** - `login-screen.kt` not `LoginScreen.kt`

---

## Vision

> "Education at your fingertips - Native Android companion for Hogwarts school automation"

### What It Does

- **Students**: View grades, attendance, schedule, pay fees
- **Teachers**: Mark attendance, submit grades, communicate with parents
- **Guardians**: Monitor child progress, receive notifications, pay fees
- **All users**: Real-time messaging, push notifications, offline access

### Integration with Hogwarts Platform

| Component | Android App | Web App (Hogwarts) |
|-----------|-------------|-------------------|
| Backend | Consumes API | Provides API (Next.js) |
| Auth | JWT tokens | NextAuth v5 (issues tokens) |
| Database | Room (offline cache) | PostgreSQL via Prisma |
| Notifications | Firebase FCM | Web Push / Email |

---

## Tech Stack

### Core

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Kotlin | 2.0+ |
| Min SDK | 26 (Android 8.0) | - |
| Target SDK | 35 (Android 15) | - |
| Build System | Gradle (Kotlin DSL) | 8.5+ |
| UI Framework | Jetpack Compose | 1.7+ |
| Architecture | MVVM + Clean Architecture | - |

### Jetpack Libraries

| Library | Purpose |
|---------|---------|
| Compose | Declarative UI |
| Navigation | Type-safe navigation |
| Room | SQLite database (offline) |
| DataStore | Preferences storage |
| WorkManager | Background sync |
| Hilt | Dependency injection |
| Lifecycle | ViewModels, Flows |

### Networking

| Library | Purpose |
|---------|---------|
| Retrofit | REST API client |
| OkHttp | HTTP client + interceptors |
| Kotlinx.serialization | JSON parsing |
| Coil | Image loading |

### Security

| Component | Technology |
|-----------|------------|
| Auth | JWT tokens from Hogwarts backend |
| Token Storage | EncryptedSharedPreferences |
| Biometric | BiometricPrompt API |
| Certificate Pinning | OkHttp CertificatePinner |

### Internationalization

| Feature | Implementation |
|---------|----------------|
| Languages | Arabic (RTL), English (LTR) |
| Direction | CompositionLocalLayoutDirection |
| Strings | res/values/, res/values-ar/ |

---

## Architecture Patterns

### Layer Architecture

```
┌─────────────────────────────────────────┐
│           UI Layer (Compose)            │
│  Screens → ViewModels → UiState         │
├─────────────────────────────────────────┤
│          Domain Layer                   │
│  Use Cases → Repository Interfaces      │
├─────────────────────────────────────────┤
│           Data Layer                    │
│  Repositories → DataSources             │
│  ┌─────────────┐  ┌─────────────┐       │
│  │   Remote    │  │   Local     │       │
│  │  (Retrofit) │  │   (Room)    │       │
│  └─────────────┘  └─────────────┘       │
└─────────────────────────────────────────┘
```

### Component Hierarchy (Atomic Composition)

```
1. Material 3 Primitives (core/designsystem/component/)
   └─ hogwarts-button.kt, hogwarts-card.kt, hogwarts-text-field.kt

2. Atoms (core/designsystem/atom/)
   └─ status-badge.kt, user-avatar.kt, empty-state.kt, offline-banner.kt

3. Feature Components (feature/<name>/ui/components/)
   └─ student-card.kt, attendance-row.kt, fee-item.kt

4. Screens (feature/<name>/ui/<feature>-screen.kt)
   └─ Full page compositions
```

### Semantic Token Usage (MANDATORY)

```kotlin
// ❌ WRONG - Hardcoded colors
Surface(color = Color.White)
Text(color = Color.Black)

// ✅ CORRECT - Semantic tokens
Surface(color = MaterialTheme.colorScheme.background)
Text(color = MaterialTheme.colorScheme.onBackground)
```

### Typography Pattern

```kotlin
// Map to web semantic HTML:
// h1 → headlineLarge
// h2 → headlineMedium
// p  → bodyLarge
// small → bodySmall
// muted → labelMedium with muted color

Text(
    text = "Page Title",
    style = MaterialTheme.typography.headlineLarge
)
```

---

## Feature-Based Structure

Every feature follows this **standardized file structure** (mirroring Hogwarts web pattern):

### File Naming Convention: kebab-case

All Kotlin files use **kebab-case** naming (matching web project conventions):
- `login-screen.kt` not `LoginScreen.kt`
- `auth-repository-impl.kt` not `AuthRepositoryImpl.kt`
- `get-students-use-case.kt` not `GetStudentsUseCase.kt`

```
feature/<feature>/
  # Presentation Layer
  ui/
    <feature>-screen.kt       # Main Composable (like content.tsx)
    <feature>-view-model.kt   # State management
    <feature>-ui-state.kt     # UI state sealed class
    components/               # Feature-specific composables
      <feature>-form.kt       # Form component
      <feature>-list.kt       # List component
      <feature>-card.kt       # Card component

  # Domain Layer
  domain/
    model/<feature>.kt        # Domain model (like types.ts)
    usecase/
      get-<feature>-use-case.kt
      create-<feature>-use-case.kt
      update-<feature>-use-case.kt
      delete-<feature>-use-case.kt
    validation/
      <feature>-validator.kt  # Validation (like validation.ts)

  # Data Layer
  data/
    repository/<feature>-repository-impl.kt  # Like actions.ts
    remote/
      <feature>-api.kt        # Retrofit interface
      dto/<feature>-dto.kt    # API DTOs
    local/
      <feature>-dao.kt        # Room DAO
      entity/<feature>-entity.kt

  # Navigation
  navigation/<feature>-nav-graph.kt
```

---

## Multi-Tenant Safety

**CRITICAL**: Every operation MUST be scoped by `schoolId` for tenant isolation.

### Repository Pattern

```kotlin
// ❌ WRONG - No tenant isolation
class StudentRepositoryImpl : StudentRepository {
    override suspend fun getStudents(): List<Student> {
        return api.getStudents()  // Missing schoolId!
    }
}

// ✅ CORRECT - Tenant isolated
class StudentRepositoryImpl(
    private val tenantContext: TenantContext
) : StudentRepository {
    override suspend fun getStudents(): List<Student> {
        val schoolId = tenantContext.schoolId
            ?: throw UnauthorizedException()
        return api.getStudents(schoolId = schoolId)
    }
}
```

### Room Queries

```kotlin
// ❌ WRONG - Returns all schools' data
@Query("SELECT * FROM students WHERE status = :status")
fun getStudentsByStatus(status: String): Flow<List<StudentEntity>>

// ✅ CORRECT - Scoped by schoolId
@Query("SELECT * FROM students WHERE schoolId = :schoolId AND status = :status")
fun getStudentsByStatus(schoolId: String, status: String): Flow<List<StudentEntity>>
```

### All Entities Include schoolId

```kotlin
@Entity(
    tableName = "students",
    indices = [Index(value = ["schoolId"])]
)
data class StudentEntity(
    @PrimaryKey val id: String,
    val schoolId: String,  // MANDATORY
    // ...
)
```

---

## BMAD Workflow

**BMAD** = "Build More, Architect Dreams" - AI-driven development framework with specialized agents.

### Artifacts Location
- `.bmad/docs/prd.md` - Product Requirements Document
- `.bmad/docs/mvp.md` - MVP Definition
- `.bmad/epics/` - Epic files with stories
- `.bmad/bmm-workflow-status.yaml` - Workflow tracking

### 4 Phases

```
Phase 1: Analysis (/analyst - Mary)
├── Research requirements
├── API documentation
└── Output: .bmad/docs/analysis/

Phase 2: Planning (/pm - John)
├── Write/update PRD
├── Define MVP scope
└── Output: .bmad/docs/, .bmad/epics/

Phase 3: Solutioning (/architect - Winston)
├── Architecture decisions
├── Room schema design
└── Output: .bmad/architecture/

Phase 4: Implementation
├── /sm (Bob) → Create stories
├── /dev (Amelia) → Implement
├── /qa → Test
└── Update .bmad/bmm-workflow-status.yaml
```

### Story Lifecycle

```
ready-for-dev → in-progress → in-review → completed
      │              │              │
      └──────────────┴──────────────┴──→ blocked
```

---

## Agent Commands

### Workflow Commands

| Command | Purpose | When to Use |
|---------|---------|-------------|
| `/analyst` | Requirements analysis | Start of project |
| `/pm` | Product management, PRD | After analysis |
| `/architect` | Architecture decisions | After planning |
| `/sm` | Story breakdown | Before each sprint |
| `/dev` | Implement current story | During implementation |
| `/qa` | Testing and validation | After implementation |

### Utility Commands

| Command | Purpose | When to Use |
|---------|---------|-------------|
| `/status` | Show workflow status | Anytime |
| `/next` | Advance to next phase | After completion |

### Build Commands

```bash
# Build all variants
./gradlew assembleDebug
./gradlew assembleRelease

# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedDebugAndroidTest

# Code quality
./gradlew ktlintCheck
./gradlew detekt
```

---

## Definition of Done

### Story Level

- [ ] All acceptance criteria met
- [ ] Unit tests pass (80%+ coverage)
- [ ] UI tests for new screens
- [ ] No lint warnings
- [ ] Code formatted (ktlint)
- [ ] Works in RTL (Arabic) mode
- [ ] Works offline (if applicable)
- [ ] PR reviewed and approved

### Sprint Level

- [ ] All sprint stories completed
- [ ] Integration tests pass
- [ ] App runs on physical device
- [ ] No crashes in testing
- [ ] README.md updated
- [ ] PROMPT.md updated
- [ ] bmm-workflow-status.yaml updated

### Release Level

- [ ] All features implemented
- [ ] Performance benchmarks met
- [ ] Security review completed
- [ ] Play Store listing ready
- [ ] Screenshots updated
- [ ] Changelog written

---

## Common Gotchas

### 1. Multi-Tenant Isolation

**Always include schoolId** in API calls and Room queries.

### 2. Offline-First Architecture

- Show cached data from Room immediately
- Fetch fresh data in background
- Update UI when data arrives
- Queue mutations when offline

### 3. ViewModel State

```kotlin
// Use StateFlow for UI state
private val _uiState = MutableStateFlow(FeatureUiState())
val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()
```

### 4. Compose Previews

```kotlin
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RTL", locale = "ar")
@Composable
fun FeatureScreenPreview() {
    HogwartsTheme {
        FeatureScreen()
    }
}
```

### 5. Navigation

Use type-safe navigation with Navigation Compose.

### 6. Hilt Injection

```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val useCase: GetFeatureUseCase,
    private val tenantContext: TenantContext
) : ViewModel()
```

### 7. Error Handling

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

---

## Key Files Reference

| File | Purpose |
|------|---------|
| `core/data/tenant/tenant-context.kt` | Multi-tenant context |
| `core/network/auth-interceptor.kt` | JWT injection |
| `core/designsystem/theme/theme.kt` | Material 3 theming |
| `core/database/hogwarts-database.kt` | Room database |
| `app/navigation/hogwarts-nav-host.kt` | Navigation graph |

---

## Reference: Hogwarts Web App

| Web File | Purpose |
|----------|---------|
| `/Users/abdout/hogwarts/CLAUDE.md` | Web orchestration |
| `/Users/abdout/hogwarts/PATTERNS.md` | Web patterns |
| `/Users/abdout/hogwarts/src/auth.ts` | Auth reference |
| `/Users/abdout/hogwarts/prisma/models/` | Data models |

---

## Epic Structure

### v1.0 (Completed -- 431 pts, 107 stories, 16 epics)

| Epic | Title | Status |
|------|-------|--------|
| EPIC-01 through EPIC-16 | Auth, Dashboard, Students, Teachers, Guardians, Messaging, Offline, Fees, Exams, Profile, Notifications, Attendance, Grades, Timetable, Design System, Production | Completed |
| EPIC-17 | Critical Stubs & Security Fixes | In Progress |

### v2.0 (Planned -- 352 pts, 60 stories, 9 epics)

| Epic | Title | Priority | Points | Sprint |
|------|-------|----------|--------|--------|
| V2-01 | Backend Mobile API Layer | P0 | 55 | 12-13 |
| V2-02 | Auth Hardening & Modern Credentials | P0 | 39 | 13-14 |
| V2-03 | Internationalization & RTL | P1 | 42 | 12-14 |
| V2-04 | Design System -- Apple HIG | P1 | 44 | 12-16 |
| V2-05 | Type-Safe Navigation Migration | P1 | 21 | 13-14 |
| V2-06 | Stream / LMS Full Implementation | P0 | 55 | 14-16 |
| V2-07 | Real-Time Messaging (Socket.IO) | P2 | 34 | 15-16 |
| V2-08 | Testing Infrastructure | P1 | 34 | 12-17 |
| V2-09 | Production Readiness & Performance | P2 | 28 | 16-17 |

See `.bmad/epics/` for detailed breakdowns.

---

## Success Metrics

- **Offline-first** - All read operations work offline
- **Multi-tenant safe** - No cross-school data leaks
- **RTL support** - Full Arabic support
- **80%+ test coverage** - Unit and UI tests
- **Sub-second navigation** - Smooth UX
