# Epic 7: Offline Support & Sync

## Overview

Implement robust offline-first architecture with background sync.

## Goals

- Room database for all cached data
- WorkManager background sync
- Conflict resolution strategy
- Offline mutation queue

## Stories

### 7.1 Room Database Schema
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Database class with all entities
- [ ] Migrations strategy
- [ ] Index optimization
- [ ] schoolId on all entities
- [ ] Sync metadata (lastSyncAt, isDirty)
- [ ] Type converters for dates, enums

**Entities**:
- UserEntity
- StudentEntity
- AttendanceEntity
- GradeEntity
- FeeEntity
- TimetableEntity
- NotificationEntity
- PendingOperationEntity

**Files**:
- `core/database/src/main/java/.../HogwartsDatabase.kt`
- `core/database/src/main/java/.../entity/*.kt`
- `core/database/src/main/java/.../dao/*.kt`
- `core/database/src/main/java/.../converter/DateConverter.kt`
- `core/database/src/main/java/.../converter/EnumConverter.kt`

### 7.2 WorkManager Background Sync
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Periodic sync (every 15 minutes when online)
- [ ] Sync on network restore
- [ ] Expedited sync for critical data
- [ ] Battery-aware scheduling
- [ ] Progress notification
- [ ] Sync constraints (network, battery)

**Files**:
- `core/data/src/main/java/.../sync/SyncWorker.kt`
- `core/data/src/main/java/.../sync/SyncScheduler.kt`
- `core/data/src/main/java/.../sync/SyncConstraints.kt`
- `app/src/main/java/.../di/WorkerModule.kt`

### 7.3 Conflict Resolution
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Server-wins strategy for reads
- [ ] Last-write-wins for mutations
- [ ] Timestamp comparison
- [ ] Merge strategy for lists
- [ ] Conflict notification to user
- [ ] Manual conflict resolution UI

**Files**:
- `core/data/src/main/java/.../sync/ConflictResolver.kt`
- `core/data/src/main/java/.../sync/MergeStrategy.kt`
- `feature/settings/ui/ConflictResolutionScreen.kt`

### 7.4 Offline Mutation Queue
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Queue mutations when offline
- [ ] Persist queue in Room
- [ ] Process queue when online
- [ ] Retry failed operations
- [ ] Max retry limit
- [ ] Clear stale operations
- [ ] Mutation status UI

**Operations**:
- Mark attendance
- Submit grades
- Update profile
- Send message

**Files**:
- `core/database/src/main/java/.../entity/PendingOperationEntity.kt`
- `core/database/src/main/java/.../dao/PendingOperationDao.kt`
- `core/data/src/main/java/.../sync/OfflineQueueManager.kt`
- `core/data/src/main/java/.../sync/OperationProcessor.kt`
- `feature/settings/ui/SyncStatusScreen.kt`
- `feature/settings/ui/components/PendingOperationCard.kt`

## Dependencies

- Epic 1 (Authentication)
- All feature epics (for entity definitions)

## Technical Notes

### Database Schema

```kotlin
@Database(
    entities = [
        UserEntity::class,
        StudentEntity::class,
        AttendanceEntity::class,
        GradeEntity::class,
        FeeEntity::class,
        TimetableEntryEntity::class,
        NotificationEntity::class,
        PendingOperationEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter::class, EnumConverter::class)
abstract class HogwartsDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun gradeDao(): GradeDao
    abstract fun feeDao(): FeeDao
    abstract fun timetableDao(): TimetableDao
    abstract fun notificationDao(): NotificationDao
    abstract fun pendingOperationDao(): PendingOperationDao
}
```

### Sync Metadata

```kotlin
interface SyncableEntity {
    val lastSyncAt: Instant
    val isDirty: Boolean
    val serverVersion: Long
}

@Entity
data class StudentEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    // ... fields
    override val lastSyncAt: Instant,
    override val isDirty: Boolean,
    override val serverVersion: Long
) : SyncableEntity
```

### Pending Operation

```kotlin
@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: OperationType,
    val entityType: String,
    val entityId: String,
    val payload: String,  // JSON serialized
    val createdAt: Instant,
    val retryCount: Int = 0,
    val lastError: String? = null,
    val status: OperationStatus = OperationStatus.PENDING
)

enum class OperationType { CREATE, UPDATE, DELETE }
enum class OperationStatus { PENDING, PROCESSING, FAILED, COMPLETED }
```

### WorkManager Setup

```kotlin
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncEngine: SyncEngine
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            syncEngine.syncAll()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        fun schedulePeriodic(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "periodic_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
```

### Conflict Resolution

```kotlin
class ConflictResolver {
    fun resolve(local: SyncableEntity, remote: SyncableEntity): SyncableEntity {
        // Server wins for version conflicts
        return if (remote.serverVersion > local.serverVersion) {
            remote
        } else if (local.isDirty) {
            // Local has unsaved changes, merge or prompt user
            mergeOrPrompt(local, remote)
        } else {
            remote
        }
    }
}
```

## Risks

- Database migration complexity
- Large sync payloads
- Battery drain from frequent syncs
- Race conditions in queue processing
- Data consistency across offline sessions
