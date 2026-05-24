# Epic E18: Communications — Announcements, Notifications, Messaging

**Epic ID:** EPIC-PROD-18
**Title:** Communications
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P1
**Sprint:** 11-13
**Total Points:** 47

---

## 1. Overview

Announcements + notifications + WhatsApp-flavored messaging end-to-end. Closes the `message-input.kt:107` attachment picker stub and `whatsapp-messages-screen.kt:88` SVG wallpaper stub.

### Success Criteria
- [ ] Conversations + messages real-time (depends on E09).
- [ ] Attachments (image, doc, audio) upload via WorkManager.
- [ ] Notification preferences per channel.
- [ ] Pinned + starred messages.

---

## 2. Stories

| ID | Title | Pts | Sprint | Notes |
|---|---|---:|---|---|
| E18.S01 | Announcements list + detail | 3 | 11 | |
| E18.S02 | Notifications list + filters | 3 | 11 | |
| E18.S03 | Mark notification as read (single + all) | 2 | 11 | |
| E18.S04 | Notification preferences (per channel) | 3 | 11 | |
| E18.S05 | Conversations list (WhatsApp-flavored) | 5 | 12 | |
| E18.S06 | Chat screen with bubbles + reactions | 5 | 12 | |
| E18.S07 | Typing indicators (depends on E09.S03) | 2 | 12 | |
| E18.S08 | Online presence indicators (depends on E09.S03) | 2 | 12 | |
| E18.S09 | Message search (within + across conversations) | 3 | 12 | |
| E18.S10 | Conversation info (members, settings, mute) | 3 | 12 | |
| E18.S11 | **Attachment picker** (image, doc, audio) — fixes `message-input.kt:107` | 5 | 12 | |
| E18.S12 | Voice messages (record + play) | 3 | 13 | |
| E18.S13 | Link previews (OG metadata fetch) | 2 | 13 | |
| E18.S14 | **WhatsApp wallpaper SVG render** — fixes `whatsapp-messages-screen.kt:88` | 2 | 13 | Add `coil-svg` to messaging deps |
| E18.S15 | Real-time message delivery (depends on E09.S04) | 3 | 13 | |
| E18.S16 | Pinned + starred messages | 2 | 13 | |
| E18.S17 | Tests | 2 | 13 | |

### Detailed AC: E18.S11 — Attachment picker
- [ ] Bottom sheet with options: Photo, Document, Audio, Camera.
- [ ] `ActivityResultContracts.GetContent` for picker.
- [ ] Files uploaded via `AttachmentUploadWorker` (WorkManager) to `POST /api/mobile/conversations/{id}/messages` (multipart).
- [ ] Optimistic UI: thumbnail shown immediately with upload progress.
- [ ] Max file size enforced server-side; client warns if file exceeds.

### Detailed AC: E18.S14 — Wallpaper SVG
- [ ] Add `io.coil-kt:coil-svg:2.7.0` to `feature/messaging/build.gradle.kts` deps.
- [ ] Render `whatsapp-bg.svg` (in repo root, move to `feature/messaging/src/main/res/drawable/`).
- [ ] Existing `coil` `SvgDecoder` already registered in `HogwartsApplication.newImageLoader()`.
- [ ] Tile / tint per theme (light/dark).

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E08.S17 announcements + notifications endpoints | Required |
| E08.S18 messaging REST baseline | Required |
| E09 real-time | Required for typing/presence/delivery |
| Existing `feature/messaging/` (51 files), `feature/announcements/`, `feature/notifications/` | Available |

---

## 4. DoD

- [ ] All 17 stories merged.
- [ ] Maestro flow: send message with image attachment → other user receives + sees image inline.
- [ ] Voice message: record → send → receive → playback.
- [ ] Link preview: paste youtube URL → preview card appears.
- [ ] Pin + star + search work.
- [ ] Captain signoff.
