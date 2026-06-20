# Epic E09: Real-Time Layer (Socket.IO + FCM)

**Epic ID:** EPIC-PROD-09
**Title:** Real-Time Layer (Socket.IO + FCM)
**Status:** Not Started
**Owner:** BMAD Dev Agent (mobile) + Web team (server)
**Priority:** P1
**Sprint:** 6-7
**Total Points:** 42

---

## 1. Overview

End-to-end real-time messaging, presence, typing, attendance live updates, and notifications. Mirror existing `core/network/socket/socket-manager.kt` (custom reconnection w/ exponential backoff to 10s + 5 attempts + custom 60s heartbeat) to a real backend Socket.IO server. The 16 events `SocketManager` already listens for need to be emitted by the server.

### Business Value
- Parents see attendance updates within 1s of teacher marking.
- Messages feel instant; typing indicators feel native.
- Multi-device users get consistent state.
- Push notifications + Socket.IO together cover both online + offline cases idempotently.

### Success Criteria
- [ ] Socket reconnects within 3s after network drop.
- [ ] Typing indicators show within 200ms.
- [ ] Push notifications for messages arrive in <2s end-to-end.
- [ ] Multi-device delivery is idempotent (same message ID not double-rendered).
- [ ] Cold-resume after long offline: missed messages backfilled via REST `since=cursor`.

---

## 2. Stories

### Story E09.S01: Backend Socket.IO server endpoints [13 pts] *(server-side)*
**Status:** Not Started · **Sprint:** 6

**As a** mobile client, **I want** the 16 events `SocketManager` listens for to actually be emitted by the server, **So that** real-time UX works.

**Acceptance Criteria:**
- [ ] Socket.IO server in `socket-server/` (existing) emits:
  - `message:new`, `message:updated`, `message:deleted`, `message:read`, `message:reaction`, `message:delivered`
  - `conversation:new`, `conversation:updated`, `conversation:archived`, `conversation:participant_added`, `conversation:participant_removed`
  - `typing:start`, `typing:stop`
  - `presence:online`, `presence:offline`, `presence:list`
- [ ] Plus new events for E09.S05: `attendance:marked`, `grade:recorded`, `notification:new`.
- [ ] Auth handshake (E09.S02).
- [ ] Per-tenant rooms; cross-tenant events impossible.

**Files:** `hogwarts/socket-server/...`.

---

### Story E09.S02: Auth handshake for Socket.IO [3 pts] *(server + Android)*
**Status:** Not Started · **Sprint:** 6

**Acceptance Criteria:**
- [ ] Initial connection sends JWT in `auth.token`.
- [ ] Server validates → joins user to rooms: `tenant:{schoolId}`, `user:{userId}`, `conversation:{id}` for each conversation user belongs to.
- [ ] On JWT expiry, server emits `auth:expired`; client refreshes token + reconnects.

---

### Story E09.S03: Presence + typing indicators end-to-end [5 pts]
**Status:** Not Started · **Sprint:** 6

**Acceptance Criteria:**
- [ ] `feature/messaging/.../components/typing-indicator.kt` reads from `SocketManager.typingEvents: Flow<TypingEvent>`.
- [ ] Typing indicator shows within 200ms of remote user typing.
- [ ] Throttled: emit "typing" max once/3s per conversation.
- [ ] Auto "stop typing" after 5s of input idle.
- [ ] Online/offline indicators in `feature/messaging/.../components/online-indicator.kt` reflect `presence:list` snapshot.

**Files:** `feature/messaging/.../components/typing-indicator.kt`, `online-indicator.kt`, `feature/messaging/.../viewmodel/conversation-view-model.kt`.

---

### Story E09.S04: Real-time message delivery (offline → online sync) [8 pts]
**Status:** Not Started · **Sprint:** 6

**Acceptance Criteria:**
- [ ] `MessagingRepository.observeMessages(conversationId)` combines Room + Socket events.
- [ ] On `message:new` event: deduplicate by message ID, upsert to Room, mark notification.
- [ ] On reconnect after disconnect: request `GET /api/mobile/conversations/{id}/messages?since={lastCursor}` REST sync to fill gaps.
- [ ] Optimistic send: insert with `status=Pending`; on socket ACK or REST 201, update to `status=Sent`.
- [ ] On send failure: status=Failed, retry button.

**Files:** `feature/messaging/.../data/messaging-repository-impl.kt`, `core/network/socket/socket-event-models.kt`, `core/database/.../message-entity.kt` (add `status`).

---

### Story E09.S05: Real-time attendance updates [3 pts]
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] When teacher marks attendance, server emits `attendance:marked` to relevant guardian + student rooms.
- [ ] Guardian's `feature/dashboard/.../home-screen.kt` updates "today's attendance" tile within 1s.
- [ ] Student's `feature/attendance/.../attendance-screen.kt` updates if currently viewed.
- [ ] No flicker; uses existing `Flow` reactive pattern.

**Files:** `core/sync/.../entity-sync-registry.kt`, `feature/attendance/.../data/attendance-repository-impl.kt`, `feature/dashboard/.../dashboard-view-model.kt`.

---

### Story E09.S06: FCM data + notification dual-channel [3 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] Server sends data-only FCM payload for online clients (handled by `SocketManager` keep-alive — drops the FCM if socket online).
- [ ] Display notifications for offline clients (handled by `notification-handler.kt`).
- [ ] Idempotency key on every server-side fan-out: same message → same FCM message ID.

---

### Story E09.S07: Background message sync via FCM data payload [3 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] When app backgrounded, FCM data wakes a `MessageSyncWorker`.
- [ ] Worker fetches the new message via REST + writes to Room.
- [ ] UI auto-updates on resume via Room's reactive `Flow`.

**Files:** new `feature/messaging/.../worker/message-sync-worker.kt`, `core/push/.../notification-handler.kt` (handle data-only payload).

---

### Story E09.S08: Reconnection backoff tuning [2 pts]
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] Existing exponential backoff (10s max, 5 attempts) tuned to 30s max with jitter.
- [ ] Test under simulated network flap (airplane mode toggle) — reconnects within 3s of network restore.
- [ ] Backoff resets on successful connection.

**Files:** `core/network/socket/socket-manager.kt`.

---

### Story E09.S09: Real-time test harness [2 pts]
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] `core/network/src/test/.../socket/SocketManagerTest.kt` uses a fake `io.socket.client.Socket` mock.
- [ ] Simulates: reconnection, missed events, recovery, expired auth.
- [ ] 8+ tests covering all 16 event types.

**Files:** `core/network/src/test/.../socket/SocketManagerTest.kt`, `core/network/src/test/.../socket/FakeSocket.kt`.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E08.S01 OpenAPI spec | Required |
| E08.S03 auth `whoami` (for socket reconnect after token refresh) | Required |
| E08.S18 messaging REST baseline | Required |
| E01.S04 FCM token upload | Required |
| Existing `SocketManager` | Available |
| Hogwarts `socket-server/` Node.js app | Available skeleton; needs new event handlers |

---

## 4. Risks

| Risk | Mitigation |
|------|------------|
| Battery drain from socket on cellular | Use `WorkManager` to disconnect socket when app backgrounded >10 min, fall back to FCM |
| Server scaling for sticky sessions | Redis adapter for Socket.IO (multi-instance support); already in `socket-server/` |
| Duplicate message render across multi-device | Idempotency key on message ID; client-side dedup in `MessagingRepository` |
| FCM rate limits | Server-side batching; max 1 FCM per user per 30s for non-critical events |

---

## 5. Out of Scope

- Voice / video calls → defer post v1.0.0.
- WebRTC for screen-sharing → defer.
- Push-to-talk audio → defer.

---

## 6. Definition of Done

- [ ] All 9 stories merged.
- [ ] Manual smoke (2 devices logged in as same user): message sent on device A appears on device B within 2s.
- [ ] Typing indicator shows within 200ms.
- [ ] Kill network for 30s → restore → reconnects within 3s + missed messages backfilled.
- [ ] Background app for 5 min → server message → push notification → tap → opens chat with message rendered.
- [ ] Captain signoff documented.
