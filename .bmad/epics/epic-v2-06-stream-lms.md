# Epic V2-06: Stream / LMS Full Implementation

**Epic ID:** EPIC-V2-06
**Title:** Stream / LMS Full Implementation
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0 (Major Deliverable)
**Sprint:** 14-16
**Total Points:** 55

---

## 1. Overview

The Stream module has domain models, navigation (8 screens), API stubs, and Room entities. This epic completes it with a full-featured Media3 video player, offline downloads, Stripe payments, progress sync, certificates, and quiz enhancement.

### Business Value
- Students learn on mobile with video courses, offline support
- Schools monetize courses via Stripe payments
- Progress syncs across devices for seamless learning
- Certificates prove achievement

### Success Criteria
- Browse -> enroll -> watch video (PiP) -> complete quiz -> certificate -> download offline
- Stripe payment flow for paid courses
- Video position resumes across sessions
- 100% completion triggers certificate

---

## 2. Stories

### Story V2-06-S01: Media3 Video Player Enhancement [8 pts]
**Status:** Not Started
**Sprint:** 15

**As a** student watching a course video,
**I want** PiP, fullscreen, quality selection, and resume,
**So that** the viewing experience is excellent.

**Acceptance Criteria:**
- [ ] PiP mode when leaving screen (Android 8+)
- [ ] Fullscreen toggle with landscape orientation
- [ ] Quality/resolution selection (HLS/DASH adaptive)
- [ ] Resume from last position (persisted in `LessonProgressEntity`)
- [ ] Progress bar with chapter markers
- [ ] Playback speed control
- [ ] Media notification with playback controls (play/pause/seek)
- [ ] Audio-only mode toggle

**Technical Notes:**
- `video-lesson-screen.kt` already imports Media3/ExoPlayer
- Add `media3-session`, `media3-ui` dependencies

---

### Story V2-06-S02: Offline Video Download Manager [13 pts]
**Status:** Not Started
**Sprint:** 16

**As a** student,
**I want** to download videos for offline viewing,
**So that** I can learn without internet.

**Acceptance Criteria:**
- [ ] Download button per video lesson
- [ ] Download queue with progress indicators
- [ ] Background download via WorkManager
- [ ] Downloaded videos in app-specific storage
- [ ] Offline indicator on downloaded lessons
- [ ] Delete per lesson or per course
- [ ] Storage usage display
- [ ] Pause/resume on connectivity change

**Technical Notes:**
- Use Media3 `DownloadManager` and `DownloadService`
- New Room entity: `DownloadedLessonEntity(lessonId, courseId, localPath, downloadState, sizeBytes, downloadedAt)`

---

### Story V2-06-S03: Course Enrollment with Stripe [8 pts]
**Status:** Not Started
**Sprint:** 15

**As a** student,
**I want** to pay for courses with Stripe,
**So that** I can access premium content.

**Acceptance Criteria:**
- [ ] Course detail shows price and "Enroll" button
- [ ] Free courses: immediate enrollment
- [ ] Paid courses: Stripe PaymentSheet flow
- [ ] Backend creates PaymentIntent, returns clientSecret
- [ ] Mobile presents PaymentSheet for card collection
- [ ] On success, backend confirms enrollment
- [ ] Receipt/confirmation screen

**Technical Notes:**
- Add Stripe Android SDK dependency
- Flow: mobile -> backend (PaymentIntent) -> clientSecret -> PaymentSheet -> confirm enrollment

---

### Story V2-06-S04: Progress Tracking & Sync [5 pts]
**Status:** Not Started
**Sprint:** 15

**As a** student,
**I want** progress to sync across devices,
**So that** I can continue learning seamlessly.

**Acceptance Criteria:**
- [ ] Video position saved every 30s to `LessonProgressEntity`
- [ ] Synced to backend on: lesson complete, app backgrounded, manual sync
- [ ] Course progress % calculated from completed/total lessons
- [ ] Progress bar on course cards in catalog
- [ ] Per-chapter completion view
- [ ] Offline progress queued via `PendingOperationDao`

---

### Story V2-06-S05: Certificate Generation & Viewing [5 pts]
**Status:** Not Started
**Sprint:** 16

**As a** student who completed a course,
**I want** to view and download my certificate,
**So that** I can prove my achievement.

**Acceptance Criteria:**
- [ ] Certificate screen after 100% completion
- [ ] Displays: student name, course name, date, verification code
- [ ] "Download PDF" fetches from backend
- [ ] In-app PDF viewing (PdfRenderer)
- [ ] "Share" via Android sharesheet
- [ ] Verification QR code

---

### Story V2-06-S06: Text Lesson & Quiz Enhancement [5 pts]
**Status:** Not Started
**Sprint:** 15

**As a** student,
**I want** rich text lessons and interactive quizzes,
**So that** I learn through multiple modalities.

**Acceptance Criteria:**
- [ ] Text lesson renders HTML/markdown from backend
- [ ] Embedded images in text lessons
- [ ] Quiz: one question at a time with progress indicator
- [ ] Multiple choice with radio buttons (Apple design)
- [ ] Immediate feedback (correct/incorrect with explanation)
- [ ] Quiz score saved as `LessonProgress`
- [ ] Retry option if below passing threshold

---

### Story V2-06-S07: Stream Module Tests [8 pts]
**Status:** Not Started
**Sprint:** 16

**Acceptance Criteria:**
- [ ] Unit tests for all stream use cases and ViewModels
- [ ] Repository tests with MockWebServer
- [ ] Room DAO tests for course/chapter/lesson/enrollment/progress
- [ ] UI tests for catalog and detail screens
- [ ] Integration: enroll -> watch -> complete -> progress -> certificate

---

### Story V2-06-S08: Anti-Download Video Protection [3 pts]
**Status:** Not Started
**Sprint:** 16

**Acceptance Criteria:**
- [ ] `FLAG_SECURE` on video lesson Window
- [ ] Video URLs signed with expiration
- [ ] Downloaded content encrypted at rest
- [ ] Handle 403 from expired URLs gracefully

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| V2-01-S04 (Stream API endpoints) | Required |
| Media3 libraries | In version catalog |
| Stripe SDK | Not yet added |
| Room course entities | Done |
