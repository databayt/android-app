# Architecture Document: Hogwarts Android

## Overview

Hogwarts Android follows **MVVM with Clean Architecture**, featuring offline-first data handling and multi-tenant isolation.

---

## Layer Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                       │
│  Screens → ViewModels → UiState                            │
│  - Observes StateFlow from ViewModel                       │
│  - Dispatches user actions to ViewModel                    │
├─────────────────────────────────────────────────────────────┤
│                    Domain Layer                             │
│  Use Cases → Repository Interfaces                         │
│  - Business logic                                          │
│  - Platform-independent                                    │
├─────────────────────────────────────────────────────────────┤
│                    Data Layer                               │
│  Repositories → DataSources                                │
│  ┌───────────────────┐  ┌───────────────────┐              │
│  │   Remote Source   │  │   Local Source    │              │
│  │    (Retrofit)     │  │     (Room)        │              │
│  └───────────────────┘  └───────────────────┘              │
└─────────────────────────────────────────────────────────────┘
```

---

## Module Structure

### Core Modules

| Module | Purpose |
|--------|---------|
| `core:common` | Shared utilities, Result type, extensions |
| `core:data` | Repository interfaces, sync engine, tenant context |
| `core:database` | Room database, base entities, DAOs |
| `core:network` | Retrofit client, interceptors, DTOs |
| `core:designsystem` | Material 3 theme, components, atoms |
| `core:security` | Encryption, biometric, keystore |

### Feature Modules

| Module | Description |
|--------|-------------|
| `feature:auth` | Login, biometric, session |
| `feature:dashboard` | Role-specific dashboards |
| `feature:students` | Student management |
| `feature:attendance` | Attendance tracking |
| `feature:grades` | Grade management |
| `feature:fees` | Fee statements, payments |
| `feature:timetable` | Schedule viewing |
| `feature:messaging` | Messages, notifications |
| `feature:settings` | Profile, preferences |

---

## Feature Module Structure

Each feature follows standardized file organization:

```
feature/<name>/
├── ui/
│   ├── <Name>Screen.kt           # Main screen composable
│   ├── <Name>ViewModel.kt        # ViewModel with StateFlow
│   ├── <Name>UiState.kt          # Sealed class for UI states
│   └── components/               # Feature-specific composables
│       ├── <Name>Form.kt
│       ├── <Name>List.kt
│       └── <Name>Card.kt
│
├── domain/
│   ├── model/<Name>.kt           # Domain model
│   ├── usecase/
│   │   ├── Get<Name>UseCase.kt
│   │   ├── Create<Name>UseCase.kt
│   │   ├── Update<Name>UseCase.kt
│   │   └── Delete<Name>UseCase.kt
│   └── validation/<Name>Validator.kt
│
├── data/
│   ├── repository/<Name>RepositoryImpl.kt
│   ├── remote/
│   │   ├── <Name>Api.kt          # Retrofit interface
│   │   └── dto/<Name>Dto.kt      # API DTOs
│   └── local/
│       ├── <Name>Dao.kt          # Room DAO
│       └── entity/<Name>Entity.kt
│
└── navigation/<Name>NavGraph.kt
```

---

## Data Flow

### Read Flow (Offline-First)

```
User opens screen
       │
       ▼
┌─────────────────┐
│   ViewModel     │
│  collects Flow  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Repository    │
│  emits cached   │──────┐
│  data first     │      │
└────────┬────────┘      │
         │               │
         ▼               ▼
┌─────────────┐   ┌─────────────┐
│    Room     │   │   Network   │
│   (Local)   │   │  (Remote)   │
└──────┬──────┘   └──────┬──────┘
       │                 │
       │    ┌────────────┘
       │    │ Update cache
       ▼    ▼
┌─────────────────┐
│  Flow emits     │
│  updated data   │
└─────────────────┘
```

### Write Flow (Offline Capable)

```
User submits form
       │
       ▼
┌─────────────────┐
│   ViewModel     │
│  calls UseCase  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Repository    │
│  checks online  │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
 Online    Offline
    │         │
    ▼         ▼
┌────────┐ ┌────────────┐
│ Submit │ │ Queue in   │
│ to API │ │ PendingOps │
└────┬───┘ └──────┬─────┘
     │            │
     ▼            ▼
┌─────────────────────┐
│  Update Room cache  │
└─────────────────────┘
         │
         ▼
┌─────────────────────┐
│  WorkManager syncs  │
│  pending ops later  │
└─────────────────────┘
```

---

## Multi-Tenant Architecture

### TenantContext

```kotlin
@Singleton
class TenantContext @Inject constructor(
    private val authRepository: AuthRepository
) {
    val schoolId: String?
        get() = authRepository.currentUser?.schoolId

    fun requireSchoolId(): String =
        schoolId ?: throw UnauthorizedException("No school context")
}
```

### Repository Pattern

```kotlin
class StudentRepositoryImpl @Inject constructor(
    private val api: StudentApi,
    private val dao: StudentDao,
    private val tenantContext: TenantContext
) : StudentRepository {

    override fun getStudents(): Flow<List<Student>> {
        val schoolId = tenantContext.requireSchoolId()

        return dao.getStudentsBySchool(schoolId)
            .onStart {
                // Refresh from network in background
                refreshFromNetwork(schoolId)
            }
            .map { entities -> entities.map { it.toDomain() } }
    }
}
```

### Room Entity

```kotlin
@Entity(
    tableName = "students",
    indices = [Index(value = ["schoolId"])]
)
data class StudentEntity(
    @PrimaryKey val id: String,
    val schoolId: String,  // MANDATORY
    val givenName: String,
    val familyName: String,
    // ...
)
```

### Room Query

```kotlin
@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE schoolId = :schoolId")
    fun getStudentsBySchool(schoolId: String): Flow<List<StudentEntity>>

    // NEVER query without schoolId filter
}
```

---

## Network Architecture

### API Client Setup

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tenantInterceptor: TenantInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(tenantInterceptor)
            .certificatePinner(certificatePinner)
            .build()
    }
}
```

### Auth Interceptor

```kotlin
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Chain): Response {
        val token = tokenManager.accessToken
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
```

### Tenant Interceptor

```kotlin
class TenantInterceptor @Inject constructor(
    private val tenantContext: TenantContext
) : Interceptor {

    override fun intercept(chain: Chain): Response {
        val schoolId = tenantContext.schoolId
        val request = if (schoolId != null) {
            chain.request().newBuilder()
                .addHeader("X-School-Id", schoolId)
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
```

---

## UI Architecture

### Composable Hierarchy

```
1. Material 3 Primitives (core/designsystem/component/)
   └─ HogwartsButton, HogwartsCard, HogwartsTextField

2. Atoms (core/designsystem/atom/)
   └─ StatusBadge, UserAvatar, EmptyState, OfflineBanner

3. Feature Components (feature/<name>/ui/components/)
   └─ StudentCard, AttendanceRow, GradeItem

4. Screens (feature/<name>/ui/<Name>Screen.kt)
   └─ Full page compositions
```

### ViewModel Pattern

```kotlin
@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val getStudentsUseCase: GetStudentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentsUiState>(StudentsUiState.Loading)
    val uiState: StateFlow<StudentsUiState> = _uiState.asStateFlow()

    init {
        loadStudents()
    }

    private fun loadStudents() {
        viewModelScope.launch {
            getStudentsUseCase()
                .catch { e -> _uiState.value = StudentsUiState.Error(e.message) }
                .collect { students ->
                    _uiState.value = StudentsUiState.Success(students)
                }
        }
    }
}
```

### UiState Pattern

```kotlin
sealed interface StudentsUiState {
    object Loading : StudentsUiState
    data class Success(val students: List<Student>) : StudentsUiState
    data class Error(val message: String?) : StudentsUiState
}
```

---

## Navigation

### Type-Safe Navigation

```kotlin
// Navigation routes
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Students : Screen("students")
    data class StudentDetail(val id: String) : Screen("students/$id")
}

// NavHost
@Composable
fun HogwartsNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) { LoginScreen() }
        composable(Screen.Dashboard.route) { DashboardScreen() }
        composable(
            route = "students/{studentId}",
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            StudentDetailScreen(
                studentId = backStackEntry.arguments?.getString("studentId")
            )
        }
    }
}
```

---

## Offline Sync

### WorkManager Setup

```kotlin
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncEngine: SyncEngine
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            syncEngine.syncPendingOperations()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry()
            else Result.failure()
        }
    }
}
```

### Pending Operations

```kotlin
@Entity(tableName = "pending_operations")
data class PendingOperation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: OperationType,
    val entityType: String,
    val entityId: String,
    val payload: String,  // JSON
    val createdAt: Long = System.currentTimeMillis()
)

enum class OperationType { CREATE, UPDATE, DELETE }
```

---

## Security

### Token Storage

```kotlin
class TokenManager @Inject constructor(
    private val encryptedPrefs: EncryptedSharedPreferences
) {
    var accessToken: String?
        get() = encryptedPrefs.getString("access_token", null)
        set(value) = encryptedPrefs.edit().putString("access_token", value).apply()

    var refreshToken: String?
        get() = encryptedPrefs.getString("refresh_token", null)
        set(value) = encryptedPrefs.edit().putString("refresh_token", value).apply()
}
```

### Biometric Auth

```kotlin
class BiometricHelper @Inject constructor(
    private val context: Context
) {
    fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Hogwarts")
            .setSubtitle("Use biometric to access your account")
            .setNegativeButtonText("Use password")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
```

---

## Testing Strategy

### Layer Testing

| Layer | Test Type | Tools |
|-------|-----------|-------|
| Domain | Unit | JUnit, MockK |
| Data | Unit + Integration | JUnit, MockK, Room Testing |
| UI | UI | Compose Testing, Espresso |

### Test Coverage Targets

| Module | Target |
|--------|--------|
| Domain | 90%+ |
| Data | 85%+ |
| UI | 75%+ |
| Overall | 80%+ |

---

## ADRs

### ADR-001: Offline-First Architecture
- **Decision**: Room as primary data source, network as refresh
- **Rationale**: Schools often have poor connectivity
- **Consequences**: More complex sync logic, better UX

### ADR-002: Multi-Module Structure
- **Decision**: Separate core and feature modules
- **Rationale**: Build time optimization, clear boundaries
- **Consequences**: More boilerplate, better maintainability

### ADR-003: StateFlow over LiveData
- **Decision**: Use Kotlin StateFlow for reactive UI
- **Rationale**: Better coroutine integration, type safety
- **Consequences**: Requires lifecycle awareness

---

## References

- [Hogwarts Web CLAUDE.md](/Users/abdout/hogwarts/CLAUDE.md)
- [Hogwarts PATTERNS.md](/Users/abdout/hogwarts/PATTERNS.md)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
