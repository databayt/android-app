# Epic 7: Offline Support & Sync

**Epic ID:** EPIC-07
**Title:** Offline Support & Background Sync
**Status:** Partially Started
**Owner:** BMAD Dev Agent
**Sprint:** 2-4

---

## 1. Overview

Implement offline-first architecture with Room database caching and WorkManager background sync.

### Business Value
- App works in areas with poor connectivity
- Faster perceived performance (show cached data)
- Seamless experience regardless of network state

### Success Criteria
- All read operations work offline
- Data syncs automatically when online
- Visual indicator for stale data
- Write operations queued when offline

---

## 2. Stories

### Story 7.1: Room Database Schema [PARTIAL]
**Status:** In Progress
**Points:** 5

**As a** developer,
**I want** Room entities for all features,
**So that** data can be cached locally.

**Acceptance Criteria:**
- [x] HogwartsDatabase configured
- [x] User entity (from auth)
- [ ] GradeEntity
- [ ] AttendanceEntity
- [ ] TimetableEntity
- [ ] NotificationEntity
- [ ] All entities include schoolId

**Tasks:**
1. Create GradeEntity and GradeDao
2. Create AttendanceEntity and AttendanceDao
3. Create TimetableEntity and TimetableDao
4. Create NotificationEntity and NotificationDao
5. Add migrations for schema changes

---

### Story 7.2: Repository Offline-First Pattern
**Status:** Ready for Dev
**Points:** 5

**As a** developer,
**I want** repositories to follow offline-first,
**So that** users see data immediately.

**Acceptance Criteria:**
- [ ] Return cached data immediately
- [ ] Fetch fresh data in background
- [ ] Update cache on successful fetch
- [ ] Emit loading/error/success states
- [ ] Handle network errors gracefully

**Pattern:**
```kotlin
fun getGrades(): Flow<Resource<List<Grade>>> = flow {
    // 1. Emit cached data immediately
    emit(Resource.Loading(cache = gradesDao.getAll().first()))

    // 2. Fetch fresh data
    try {
        val remote = api.getGrades()
        gradesDao.insertAll(remote.map { it.toEntity() })
        emit(Resource.Success(gradesDao.getAll().first()))
    } catch (e: Exception) {
        emit(Resource.Error(e, cache = gradesDao.getAll().first()))
    }
}
```

**Tasks:**
1. Create Resource sealed class
2. Implement pattern in GradesRepository
3. Implement pattern in AttendanceRepository
4. Implement pattern in TimetableRepository
5. Create reusable networkBoundResource helper

---

### Story 7.3: Background Sync with WorkManager
**Status:** Ready for Dev
**Points:** 5

**As a** user,
**I want** data to sync in background,
**So that** I have fresh data when opening the app.

**Acceptance Criteria:**
- [ ] Periodic sync every 15 minutes
- [ ] Sync on app open if stale
- [ ] Respect battery optimization
- [ ] Sync only on WiFi option
- [ ] Show sync status in settings

**Tasks:**
1. Create SyncWorker class
2. Configure WorkManager constraints
3. Schedule periodic sync
4. Trigger sync on app resume
5. Add sync preference settings

---

### Story 7.4: Stale Data Indicator
**Status:** Ready for Dev
**Points:** 2

**As a** user,
**I want** to know if data is stale,
**So that** I can manually refresh.

**Acceptance Criteria:**
- [ ] Show "Last updated: X minutes ago"
- [ ] Highlight if data > 1 hour old
- [ ] Manual refresh button
- [ ] Pull-to-refresh on all lists

**Tasks:**
1. Store lastSynced timestamp per entity
2. Create LastUpdatedText component
3. Add to all list screens
4. Implement pull-to-refresh

---

### Story 7.5: Offline Mutation Queue [POST-MVP]
**Status:** Backlog
**Points:** 8

**As a** user,
**I want** to perform actions offline,
**So that** they sync when I'm back online.

**Acceptance Criteria:**
- [ ] Queue write operations when offline
- [ ] Show pending operations indicator
- [ ] Sync queue when online
- [ ] Handle conflicts
- [ ] Retry failed operations

---

### Story 7.6: Conflict Resolution [POST-MVP]
**Status:** Backlog
**Points:** 5

**As a** user,
**I want** conflicts to be resolved,
**So that** data integrity is maintained.

**Acceptance Criteria:**
- [ ] Detect version conflicts
- [ ] Last-write-wins default
- [ ] Manual resolution for critical data
- [ ] Show conflict notification

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| Room database | Configured |
| WorkManager | Not started |
| Connectivity observer | Not started |

---

## 4. Technical Architecture

### Offline-First Flow
```
┌─────────────────────────────────────────┐
│           UI Layer                      │
│  observes Flow<Resource<T>>             │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│           Repository                    │
│  networkBoundResource {                 │
│    loadFromDb = { dao.getAll() }        │
│    fetchFromNetwork = { api.get() }     │
│    saveResult = { dao.insertAll(it) }   │
│  }                                      │
└────────────────┬────────────────────────┘
                 │
     ┌───────────┴───────────┐
     ▼                       ▼
┌─────────────┐       ┌─────────────┐
│    Room     │       │   Retrofit  │
│  (Local)    │       │  (Remote)   │
└─────────────┘       └─────────────┘
```

### WorkManager Sync
```
┌─────────────────────────────────────────┐
│           SyncWorker                    │
│  - Runs every 15 min                    │
│  - Constraints: Network required        │
│  - Syncs: grades, attendance, schedule  │
└─────────────────────────────────────────┘
```

---

## 5. Key Files

```
core/data/
├── util/
│   ├── resource.kt
│   └── network-bound-resource.kt
└── sync/
    ├── sync-worker.kt
    └── sync-manager.kt

core/database/
├── hogwarts-database.kt
├── dao/
│   ├── grade-dao.kt
│   ├── attendance-dao.kt
│   └── timetable-dao.kt
└── entity/
    ├── grade-entity.kt
    ├── attendance-entity.kt
    └── timetable-entity.kt
```

---

## 6. Resource Sealed Class

```kotlin
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
```

---

## 7. Risks

| Risk | Mitigation |
|------|------------|
| Room migrations | Incremental schema, fallback |
| WorkManager restrictions | Expedited work for critical |
| Battery drain | Respect battery saver mode |
| Large data sets | Incremental sync, pagination |
