# Epic 5: Guardian Module

**Epic ID:** EPIC-05
**Title:** Guardian Features
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Sprint:** 3

---

## 1. Overview

Implement guardian-specific features for monitoring children's academic progress.

### Business Value
- Parents stay informed about children's education
- Reduce school-parent communication overhead
- Enable fee payment on mobile (post-MVP)

### Success Criteria
- View all enrolled children
- View each child's grades and attendance
- Receive notifications about children
- Offline support

---

## 2. Stories

### Story 5.1: View Children List
**Status:** Ready for Dev
**Points:** 3

**As a** guardian,
**I want** to see my enrolled children,
**So that** I can monitor their progress.

**Acceptance Criteria:**
- [ ] List of children with photos
- [ ] Show name, grade, section
- [ ] Show quick summary (attendance %, recent grade)
- [ ] Navigate to child detail
- [ ] Offline support

**Tasks:**
1. Create ChildrenListScreen composable
2. Create ChildrenViewModel
3. Create ChildCard component
4. Implement GetChildrenUseCase
5. Create ChildEntity and Dao

---

### Story 5.2: View Child's Grades
**Status:** Ready for Dev
**Points:** 3

**As a** guardian,
**I want** to see my child's grades,
**So that** I can track academic progress.

**Acceptance Criteria:**
- [ ] Select child from list
- [ ] Show all subjects with grades
- [ ] Show grade trend (improving/declining)
- [ ] Show class rank (if available)
- [ ] Offline support

**Tasks:**
1. Create ChildGradesScreen composable
2. Reuse grade components from Epic 3
3. Add child context to API calls
4. Cache grades per child

---

### Story 5.3: View Child's Attendance
**Status:** Ready for Dev
**Points:** 3

**As a** guardian,
**I want** to see my child's attendance,
**So that** I know if they're attending school.

**Acceptance Criteria:**
- [ ] Attendance percentage
- [ ] Calendar view of attendance
- [ ] Recent absence alerts
- [ ] Offline support

**Tasks:**
1. Create ChildAttendanceScreen composable
2. Reuse attendance components from Epic 3
3. Highlight absences/late arrivals
4. Add notification for absences

---

### Story 5.4: Guardian Dashboard
**Status:** Ready for Dev
**Points:** 5

**As a** guardian,
**I want** a dashboard overview,
**So that** I can quickly check all children.

**Acceptance Criteria:**
- [ ] Cards for each child with summary
- [ ] Quick stats (attendance, grades)
- [ ] Recent notifications
- [ ] Quick actions (view child, pay fees)

**Tasks:**
1. Create GuardianDashboard composable
2. Aggregate data for all children
3. Show alert badges for issues
4. Link to detailed views

---

### Story 5.5: Pay Fees [POST-MVP]
**Status:** Backlog
**Points:** 8

**As a** guardian,
**I want** to pay school fees,
**So that** I can fulfill financial obligations.

**Acceptance Criteria:**
- [ ] View pending fees per child
- [ ] Select fee to pay
- [ ] Payment gateway integration
- [ ] Payment confirmation
- [ ] Receipt download

---

### Story 5.6: Communicate with Teachers [POST-MVP]
**Status:** Backlog
**Points:** 5

**As a** guardian,
**I want** to message my child's teacher,
**So that** I can discuss their progress.

**Acceptance Criteria:**
- [ ] List of child's teachers
- [ ] Start conversation
- [ ] View message history
- [ ] Push notification for replies

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| Dashboard (Epic 2) | Not started |
| Student grades (Epic 3) | Not started |
| Student attendance (Epic 3) | Not started |

---

## 4. API Endpoints (Pending)

| Endpoint | Method | Description |
|----------|--------|-------------|
| /api/mobile/guardians/{id}/children | GET | Get guardian's children |
| /api/mobile/students/{id}/summary | GET | Get child summary |

---

## 5. Technical Architecture

```
feature/guardian/
├── ui/
│   ├── children-list-screen.kt
│   ├── child-detail-screen.kt
│   ├── child-grades-screen.kt
│   ├── child-attendance-screen.kt
│   └── components/
│       ├── child-card.kt
│       └── guardian-dashboard.kt
├── domain/
│   ├── model/child.kt
│   └── usecase/
│       └── get-children-use-case.kt
└── data/
    └── repository/guardian-repository-impl.kt
```

---

## 6. Data Models

### Child Entity
```kotlin
@Entity(tableName = "children")
data class ChildEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val guardianId: String,
    val studentId: String,
    val name: String,
    val grade: String,
    val section: String,
    val avatarUrl: String?,
    val lastSynced: Long
)
```

---

## 7. Risks

| Risk | Mitigation |
|------|------------|
| Multiple children complexity | Efficient data aggregation |
| Payment integration | Use established payment SDK |
| Data privacy | Strict parent-child validation |
