# Epic E21: Settings & Personalization

**Epic ID:** EPIC-PROD-21
**Title:** Settings & Personalization
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P2
**Sprint:** 13
**Total Points:** 18

---

## 1. Overview

Settings screen that surfaces all the personal toggles users care about: theme, language, wallpaper, notification preferences, cache management, help, version info, feedback.

### Success Criteria
- [ ] Every toggle has a live effect (no placeholders).
- [ ] Cache + storage size visible; user can clear.
- [ ] Feedback flow goes directly to GitHub Issues via shake-to-report (E26.S09).

---

## 2. Stories

| ID | Title | Pts | Sprint |
|---|---|---:|---|
| E21.S01 | Theme picker (light / dark / system) | 2 | 13 |
| E21.S02 | Language picker (en / ar) — depends on E04.S05 | 2 | 13 |
| E21.S03 | Wallpaper picker (5 wallpapers in `core/designsystem/.../wallpaper/`) | 2 | 13 |
| E21.S04 | Notification preferences per channel (5 channels: attendance, grades, fees, messages, announcements) | 3 | 13 |
| E21.S05 | Cache + storage management (show size; clear cache button) | 3 | 13 |
| E21.S06 | Help & support links (FAQs, contact, privacy, ToS) | 1 | 13 |
| E21.S07 | About / version / build info | 2 | 13 |
| E21.S08 | Diagnostic / send-feedback flow (depends on E26.S09 shake-to-report) | 3 | 13 |

### Detailed AC: E21.S04 — Notification prefs
- [ ] Each of the 5 channels has: enabled/disabled, sound on/off, vibration on/off, priority high/normal/low.
- [ ] Stored per-channel via `NotificationManager.IMPORTANCE_*`.
- [ ] Server-side toggle for "do not disturb" hours.

### Detailed AC: E21.S05 — Storage
- [ ] Surface: app size, cache size, downloads (LMS), translations cache.
- [ ] "Clear cache" button: deletes Coil disk cache + image cache, NOT user data.
- [ ] "Clear LMS downloads" button.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E04.S05 locale switch | Required for E21.S02 |
| E26.S09 shake-to-report | Required for E21.S08 |
| Existing `feature/settings/` (4 files) | Available skeleton |

---

## 4. DoD

- [ ] All 8 stories merged.
- [ ] Each setting persists and applies live.
- [ ] Captain signoff.
