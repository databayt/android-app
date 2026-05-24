# Epic V2-07: Real-Time Messaging with Socket.IO

**Epic ID:** EPIC-V2-07
**Title:** Real-Time Messaging with Socket.IO
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P2
**Sprint:** 15-16
**Total Points:** 34

---

## 1. Overview

The messaging module has Room entities, screens, and REST API stubs but no real-time delivery. The Hogwarts web app uses a Socket.IO server (Fly.io) with Redis adapter. This epic adds a Socket.IO client for real-time messages, typing indicators, presence, read receipts, and FCM push.

### Business Value
- Real-time chat between teachers, parents, and staff
- No manual refresh needed for new messages
- Online/offline presence awareness
- Push notifications for missed messages

### Success Criteria
- Messages appear instantly without refresh
- Typing indicators visible in real time
- Online/offline status on user avatars
- Push notifications when app is backgrounded

---

## 2. Stories

### Story V2-07-S01: Socket.IO Client Integration [8 pts]
**Status:** Not Started
**Sprint:** 15

**As a** developer,
**I want** a Socket.IO client connected to the Hogwarts messaging server,
**So that** messages are delivered in real time.

**Acceptance Criteria:**
- [ ] `io.socket:socket.io-client` dependency
- [ ] JWT auth on connection
- [ ] Auto-reconnect with exponential backoff (max 5 attempts)
- [ ] Joins user's conversation rooms (`conversation:{id}`)
- [ ] Lifecycle-aware: connected in foreground, disconnected in background
- [ ] Clean disconnect on logout
- [ ] Hilt singleton `SocketManager` in `core/network` or new `core/realtime`

**Technical Notes:**
- Web Socket.IO server: JWT auth, room-based scoping, Redis adapter
- Must handle: `connect`, `disconnect`, `connect_error`, `reconnect` events
- Connection URL: same server or dedicated WebSocket endpoint

---

### Story V2-07-S02: Real-Time Message Delivery [8 pts]
**Status:** Not Started
**Sprint:** 15

**As a** user chatting with someone,
**I want** messages to appear instantly,
**So that** conversations feel live.

**Acceptance Criteria:**
- [ ] `message:new` events update chat screen in real time
- [ ] Insert into Room `MessageEntity` on receive
- [ ] Conversation list updates `lastMessage` and `unreadCount`
- [ ] Optimistic send: local insert -> socket emit -> confirm -> update status
- [ ] Failed sends show retry indicator
- [ ] Deduplicate by ID (socket + REST can deliver same message)

**Technical Notes:**
- `MessageEntity` and `ConversationEntity` in Room. `PendingMessage` handles optimistic sends.

---

### Story V2-07-S03: Typing Indicators & Presence [5 pts]
**Status:** Not Started
**Sprint:** 16

**As a** user in a chat,
**I want** to see typing status and online presence,
**So that** I know when to expect a reply.

**Acceptance Criteria:**
- [ ] Emit `typing:start` on typing (debounced, max once per 3s)
- [ ] Emit `typing:stop` after 5s idle
- [ ] Animated typing indicator ("..." dots)
- [ ] Online/offline presence dot on avatars in conversation list
- [ ] Presence heartbeat every 30s in foreground

---

### Story V2-07-S04: Read Receipts [3 pts]
**Status:** Not Started
**Sprint:** 16

**As a** user,
**I want** to see when messages are read,
**So that** I know the recipient saw them.

**Acceptance Criteria:**
- [ ] On conversation open: emit `messages:read` + POST `api/mobile/conversations/{id}/read`
- [ ] Incoming `messages:read` events update local status
- [ ] Visual read indicator (double-check icon)

**Technical Notes:**
- `ReadReceipt` model exists at `feature/messaging/domain/model/read-receipt.kt`

---

### Story V2-07-S05: FCM Push for Messages [5 pts]
**Status:** Not Started
**Sprint:** 16

**As a** user with app in background,
**I want** push notifications for new messages,
**So that** I don't miss conversations.

**Acceptance Criteria:**
- [ ] FCM token registered via POST `api/mobile/devices`
- [ ] Push for new messages when app backgrounded
- [ ] Shows sender name, message preview
- [ ] Tap deep-links to conversation
- [ ] Notifications grouped per conversation
- [ ] Token refreshed on `onNewToken()`, cleared on logout

**Technical Notes:**
- Firebase deps in `libs.versions.toml`. Channels defined in `strings.xml`.
- `core/push` module exists with directory structure.

---

### Story V2-07-S06: Messaging Tests [5 pts]
**Status:** Not Started
**Sprint:** 16

**Acceptance Criteria:**
- [ ] Unit tests: SocketManager connect/disconnect/reconnect
- [ ] Unit tests: message send (optimistic + confirm)
- [ ] ViewModel tests: ConversationsViewModel, ChatViewModel
- [ ] Room DAO tests: message + conversation queries
- [ ] Integration: send -> receive via mock socket -> verify in Room

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| V2-01-S03 (Messaging API endpoints) | Required |
| Socket.IO server (Fly.io) | Running (web app uses it) |
| Firebase project | Partially configured |
