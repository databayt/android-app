# Epic 6: Messaging & Notifications

## Overview

Implement push notifications and in-app messaging features.

## Goals

- Firebase Cloud Messaging setup
- Push notification handling
- In-app notification center
- Direct messaging (future)

## Stories

### 6.1 Firebase FCM Setup
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Firebase project integration
- [ ] FCM token registration
- [ ] Token refresh handling
- [ ] Send token to backend
- [ ] Handle token on login/logout

**Files**:
- `app/google-services.json`
- `app/src/main/java/.../messaging/HogwartsFirebaseService.kt`
- `core/network/src/main/java/.../messaging/FcmTokenManager.kt`
- `core/data/src/main/java/.../messaging/NotificationRepository.kt`

### 6.2 Push Notification Handling
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Receive notifications when app closed
- [ ] Receive notifications when app open
- [ ] Display notification in system tray
- [ ] Custom notification channels
- [ ] Deep link to relevant screen
- [ ] Notification click handling

**Notification Types**:
- Attendance alerts (ABSENT, LATE)
- Grade posted
- Fee due reminder
- Announcements
- Direct messages

**Files**:
- `app/src/main/java/.../messaging/NotificationHandler.kt`
- `app/src/main/java/.../messaging/NotificationChannels.kt`
- `app/src/main/res/values/notification_strings.xml`

### 6.3 In-App Notification Center
**Status**: Not Started

**Acceptance Criteria**:
- [ ] List of all notifications
- [ ] Unread indicator
- [ ] Mark as read
- [ ] Delete notification
- [ ] Filter by type
- [ ] Pull-to-refresh
- [ ] Empty state
- [ ] Cached for offline

**API Endpoint**: `GET /api/notifications?userId={id}`

**Files**:
- `feature/messaging/ui/NotificationCenterScreen.kt`
- `feature/messaging/ui/NotificationCenterViewModel.kt`
- `feature/messaging/ui/components/NotificationItem.kt`
- `feature/messaging/ui/components/NotificationFilter.kt`
- `feature/messaging/domain/model/Notification.kt`
- `feature/messaging/domain/usecase/GetNotificationsUseCase.kt`
- `feature/messaging/domain/usecase/MarkNotificationReadUseCase.kt`
- `feature/messaging/data/repository/NotificationRepositoryImpl.kt`
- `feature/messaging/data/remote/NotificationApi.kt`
- `feature/messaging/data/local/NotificationDao.kt`

### 6.4 Direct Messaging (Socket.IO)
**Status**: Not Started
**Priority**: P1 (Post-MVP)

**Acceptance Criteria**:
- [ ] Socket.IO connection management
- [ ] Real-time message delivery
- [ ] Conversation list
- [ ] Message thread view
- [ ] Send text messages
- [ ] Typing indicator
- [ ] Online status
- [ ] Offline message queue

**Files**:
- `feature/messaging/ui/ConversationsScreen.kt`
- `feature/messaging/ui/ChatScreen.kt`
- `feature/messaging/ui/ChatViewModel.kt`
- `feature/messaging/ui/components/MessageBubble.kt`
- `feature/messaging/domain/model/Message.kt`
- `feature/messaging/domain/model/Conversation.kt`
- `feature/messaging/data/remote/SocketManager.kt`
- `feature/messaging/data/repository/ChatRepositoryImpl.kt`

## Dependencies

- Epic 1 (Authentication - for user token)
- Epic 2 (Dashboard - for notification badge)

## Technical Notes

### FCM Token Registration

```kotlin
class HogwartsFirebaseService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        // Send to backend
        repository.registerFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Handle notification
        notificationHandler.handle(message)
    }
}
```

### Notification Channels

```kotlin
object NotificationChannels {
    const val ATTENDANCE = "attendance"
    const val GRADES = "grades"
    const val FEES = "fees"
    const val MESSAGES = "messages"
    const val ANNOUNCEMENTS = "announcements"
}
```

### Notification Payload

```json
{
  "type": "ATTENDANCE_ABSENT",
  "title": "Attendance Alert",
  "body": "Ahmed was marked absent today",
  "data": {
    "studentId": "student-123",
    "date": "2024-01-15"
  }
}
```

### Socket.IO Events

```kotlin
// Connect
socket.connect()

// Listen for messages
socket.on("message") { data -> }

// Send message
socket.emit("message", messagePayload)

// Typing indicator
socket.emit("typing", conversationId)
```

## Risks

- FCM token refresh edge cases
- Background notification on Android 13+
- Socket.IO reconnection handling
- Battery optimization killing service
