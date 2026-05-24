# Epic 6: Messaging & Notifications

**Epic ID:** EPIC-06
**Title:** Messaging & Notifications
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Sprint:** 4

---

## 1. Overview

Implement push notifications via Firebase Cloud Messaging and in-app notification center.

### Business Value
- Real-time alerts for important school events
- Reduce missed communications
- Enable teacher-parent messaging (post-MVP)

### Success Criteria
- FCM token registered with backend
- Push notifications display correctly
- In-app notification list
- Notifications work when app is closed

---

## 2. Stories

### Story 6.1: FCM Token Registration
**Status:** Ready for Dev
**Points:** 3

**As a** user,
**I want** to receive push notifications,
**So that** I'm alerted to important events.

**Acceptance Criteria:**
- [ ] Firebase SDK initialized
- [ ] FCM token obtained on app start
- [ ] Token sent to backend API
- [ ] Token refreshed when changed
- [ ] Token cleared on logout

**Tasks:**
1. Add Firebase dependencies
2. Create FirebaseMessagingService
3. Send token to POST /api/mobile/devices
4. Handle token refresh
5. Clear token on logout

---

### Story 6.2: Push Notification Display
**Status:** Ready for Dev
**Points:** 3

**As a** user,
**I want** to see push notifications,
**So that** I'm informed even when app is closed.

**Acceptance Criteria:**
- [ ] Notification appears in system tray
- [ ] Show title and body
- [ ] Deep link to relevant screen
- [ ] Group notifications by type
- [ ] Handle notification tap

**Tasks:**
1. Create NotificationService
2. Build notification with NotificationCompat
3. Create notification channels per type
4. Handle deep link intents
5. Test with app in background/killed

---

### Story 6.3: In-App Notification Center
**Status:** Ready for Dev
**Points:** 5

**As a** user,
**I want** to see all notifications in-app,
**So that** I don't miss anything important.

**Acceptance Criteria:**
- [ ] List of all notifications
- [ ] Mark as read/unread
- [ ] Filter by type
- [ ] Delete notifications
- [ ] Pull-to-refresh
- [ ] Offline cache

**Tasks:**
1. Create NotificationsScreen composable
2. Create NotificationsViewModel
3. Create NotificationItem component
4. Create NotificationEntity and Dao
5. Implement GetNotificationsUseCase
6. Add unread count badge to nav

---

### Story 6.4: Notification Preferences
**Status:** Ready for Dev
**Points:** 3

**As a** user,
**I want** to customize notifications,
**So that** I only receive relevant alerts.

**Acceptance Criteria:**
- [ ] Toggle notifications by type
- [ ] Types: grades, attendance, announcements, messages
- [ ] Quiet hours setting
- [ ] Persist preferences locally

**Tasks:**
1. Create NotificationSettingsScreen
2. Create notification preference categories
3. Store in DataStore
4. Sync with backend (optional)

---

### Story 6.5: Real-Time Chat [POST-MVP]
**Status:** Backlog
**Points:** 13

**As a** user,
**I want** to chat with teachers/parents,
**So that** we can communicate efficiently.

**Acceptance Criteria:**
- [ ] List of conversations
- [ ] Real-time message delivery
- [ ] Typing indicators
- [ ] Read receipts
- [ ] Media attachments

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| Firebase project setup | Not started |
| Backend notification API | Pending |
| Deep linking | Not started |

---

## 4. Firebase Setup

### Required Configuration
1. Create Firebase project
2. Add google-services.json to app/
3. Enable Cloud Messaging
4. Configure notification channels

### Notification Channels
| Channel | Description | Importance |
|---------|-------------|------------|
| grades | Grade updates | High |
| attendance | Attendance alerts | High |
| announcements | School announcements | Default |
| messages | Direct messages | High |
| general | Other notifications | Default |

---

## 5. API Endpoints (Pending)

| Endpoint | Method | Description |
|----------|--------|-------------|
| /api/mobile/devices | POST | Register FCM token |
| /api/mobile/devices/{token} | DELETE | Unregister token |
| /api/mobile/notifications | GET | Get notifications list |
| /api/mobile/notifications/{id}/read | PUT | Mark as read |

---

## 6. Technical Architecture

```
feature/messaging/
├── ui/
│   ├── notifications-screen.kt
│   ├── notification-settings-screen.kt
│   └── components/
│       └── notification-item.kt
├── domain/
│   ├── model/notification.kt
│   └── usecase/
│       ├── get-notifications-use-case.kt
│       └── mark-notification-read-use-case.kt
├── data/
│   └── repository/notifications-repository-impl.kt
└── service/
    └── hogwarts-messaging-service.kt

core/push/
├── di/firebase-module.kt
└── service/firebase-messaging-service.kt
```

---

## 7. Notification Payload Format

```json
{
  "notification": {
    "title": "New Grade Posted",
    "body": "Your Math grade has been updated"
  },
  "data": {
    "type": "grade",
    "entityId": "grade-123",
    "deepLink": "hogwarts://grades/grade-123"
  }
}
```

---

## 8. Risks

| Risk | Mitigation |
|------|------------|
| FCM delivery issues | Fallback polling |
| Notification spam | Rate limiting, preferences |
| Deep link failures | Graceful fallback to home |
