# Epic E17: LMS — Stream, Lessons, Courses

**Epic ID:** EPIC-PROD-17
**Title:** LMS — Stream, Lessons, Courses
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P1
**Sprint:** 11-13
**Total Points:** 55

---

## 1. Overview

Full LMS feature parity with web. Replaces V2-06. Adds Media3 for video playback (not currently in deps despite `feature/stream/` referencing it), offline downloads, Stripe-gated paid courses, and certificate generation.

### Success Criteria
- [ ] Video lessons play smoothly (Media3 ExoPlayer).
- [ ] Picture-in-Picture works on supported devices.
- [ ] Offline downloads of videos + lesson assets.
- [ ] Stripe-gated paid courses unlock on purchase (reuses E16.S04 infra).
- [ ] Course completion certificate downloads.

---

## 2. Stories

| ID | Title | Pts | Sprint | Notes |
|---|---|---:|---|---|
| E17.S01 | Add Media3 (ExoPlayer 1.x) to libs.versions.toml + feature/stream deps | 3 | 11 | |
| E17.S02 | Course catalog with category filters + search | 5 | 11 | |
| E17.S03 | Course detail + chapter list + enrollment CTA | 5 | 11 | |
| E17.S04 | **Video lesson player (Media3, PiP, captions, speed)** | 13 | 11-12 | |
| E17.S05 | Text + markdown lessons | 3 | 12 | |
| E17.S06 | Lesson quizzes (gates progress) | 5 | 12 | |
| E17.S07 | Course progress tracking + sync | 5 | 12 | Heartbeat to server |
| E17.S08 | Course completion certificate | 5 | 12 | |
| E17.S09 | **Offline downloads** (videos + lesson assets) | 8 | 13 | DownloadManager + ExoDownload |
| E17.S10 | Stripe-gated paid courses | 3 | 13 | Reuses E16.S04 |
| E17.S11 | Tests | 1 | 13 | |

### Detailed AC: E17.S04 — Video player
- [ ] `androidx.media3:media3-exoplayer:1.x` + `media3-ui` + `media3-session`.
- [ ] Custom Compose `VideoPlayer` Composable wrapping `PlayerView`.
- [ ] PiP supported on Android 12+ via `setPictureInPictureParams`.
- [ ] Captions / subtitles toggle (server returns SRT/VTT URL).
- [ ] Playback speed: 0.5x, 1x, 1.25x, 1.5x, 2x.
- [ ] Resume from last position (server-synced).
- [ ] DRM (Widevine) if course content is DRM-protected.

### Detailed AC: E17.S09 — Offline downloads
- [ ] User taps "Download" on a course → all videos + assets queue via `androidx.media3:media3-exoplayer-download`.
- [ ] WiFi-only by default (configurable).
- [ ] Progress + cancel UI.
- [ ] Storage location: `context.filesDir/lms-downloads/{courseId}/`.
- [ ] Auto-delete after 30 days idle.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E08.S19 LMS endpoints | Required |
| E16.S04 Stripe SDK | Required for E17.S10 |
| Media3 1.x — new dep | Required |
| Existing `feature/stream/` (43 files), `feature/lessons/` (18 files) skeletons | Available |

---

## 4. DoD

- [ ] All 11 stories merged.
- [ ] Maestro flow: enroll in course → play video → exit → resume from same position.
- [ ] PiP smoke test on Pixel 8.
- [ ] Download course offline → enable airplane mode → play successfully.
- [ ] Buy paid course via Stripe test card → unlock content.
- [ ] Complete course → download certificate.
- [ ] Captain signoff.
