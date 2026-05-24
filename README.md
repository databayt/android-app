# Hogwarts Android

Native Android companion app for the Hogwarts school management platform.

## Overview

Hogwarts Android provides mobile access to the Hogwarts school automation system for:
- **Students**: View grades, attendance, schedule, pay fees
- **Teachers**: Mark attendance, submit grades, communicate with parents
- **Guardians**: Monitor child progress, receive notifications, pay fees
- **Admins**: Overview dashboards, system notifications

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.0+ |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room (offline-first) |
| Network | Retrofit + OkHttp |
| Auth | JWT + Biometric |
| i18n | Arabic (RTL), English (LTR) |

## Project Structure

```
kotlin-app/
├── app/                    # Main application module
├── core/                   # Shared core modules
│   ├── common/            # Utilities, Result type
│   ├── data/              # Repository interfaces, sync
│   ├── database/          # Room database
│   ├── network/           # Retrofit, interceptors
│   ├── designsystem/      # Material 3 theme
│   └── security/          # Encryption, biometric
├── feature/               # Feature modules
│   ├── auth/              # Authentication
│   ├── dashboard/         # Role-specific dashboards
│   ├── students/          # Student management
│   ├── attendance/        # Attendance tracking
│   ├── grades/            # Grade management
│   ├── fees/              # Fee statements
│   ├── timetable/         # Schedule
│   ├── messaging/         # Notifications
│   └── settings/          # Profile, settings
└── docs/                  # Documentation
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 35

### Setup

1. Clone the repository:
```bash
git clone https://github.com/your-org/kotlin-app.git
cd kotlin-app
```

2. Open in Android Studio and sync Gradle

3. Run the app:
```bash
./gradlew installDebug
```

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedDebugAndroidTest

# Code quality
./gradlew ktlintCheck
./gradlew detekt
```

## BMAD Workflow

This project follows the BMAD (Business-Model-Architecture-Development) methodology.

### Agent Commands

| Command | Purpose |
|---------|---------|
| `/analyst` | Requirements analysis |
| `/pm` | Product management |
| `/architect` | Architecture decisions |
| `/sm` | Story breakdown |
| `/dev` | Implementation |
| `/qa` | Testing |
| `/status` | Workflow status |
| `/next` | Phase advancement |

See `.claude/commands/` for detailed agent instructions.

## Architecture

### Layer Architecture

```
UI Layer (Compose)
    ↓
Domain Layer (Use Cases)
    ↓
Data Layer (Repository → DataSources)
```

### Multi-Tenant Safety

**CRITICAL**: Every API call and Room query MUST include `schoolId` for tenant isolation.

```kotlin
// ✅ CORRECT
@Query("SELECT * FROM students WHERE schoolId = :schoolId")
fun getStudents(schoolId: String): Flow<List<StudentEntity>>

// ❌ WRONG - No tenant isolation
@Query("SELECT * FROM students")
fun getStudents(): Flow<List<StudentEntity>>
```

### Offline-First

1. Show cached data from Room immediately
2. Fetch from network in background
3. Update cache and UI
4. Queue mutations when offline
5. Sync via WorkManager when online

## Contributing

1. Read `CLAUDE.md` for workflow guidelines
2. Follow feature-based module structure
3. Use semantic tokens (never hardcode colors)
4. Write unit tests (80%+ coverage)
5. Test RTL mode (Arabic)

## License

Proprietary - All rights reserved
