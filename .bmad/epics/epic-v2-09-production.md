# Epic V2-09: Production Readiness & Performance

**Epic ID:** EPIC-V2-09
**Title:** Production Readiness & Performance
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P2
**Sprint:** 16-17
**Total Points:** 28

---

## 1. Overview

Final polish for production release: R8 optimization, Firebase Crashlytics/Analytics, performance tuning, Play Store configuration, accessibility audit, edge-to-edge polish, and offline connectivity UX.

### Business Value
- Crash-free production app with monitoring
- Performance meets user expectations (sub-2s cold start)
- Accessible to users with disabilities
- Ready for Google Play Store distribution

### Success Criteria
- Release APK under 25MB
- Cold start under 2 seconds
- No critical accessibility issues
- Play Store listing ready in English and Arabic

---

## 2. Stories

### Story V2-09-S01: ProGuard/R8 Optimization [3 pts]
**Status:** Not Started
**Sprint:** 17

**Acceptance Criteria:**
- [ ] R8 rules for: serialization, Retrofit, Room, Hilt
- [ ] Release APK under 25MB
- [ ] No runtime crashes from R8 in release build
- [ ] Mapping file uploaded to Crashlytics

---

### Story V2-09-S02: Crashlytics & Analytics [5 pts]
**Status:** Not Started
**Sprint:** 17

**Acceptance Criteria:**
- [ ] Crashlytics with deobfuscated stack traces
- [ ] Non-fatal error logging: API failures, Room migrations, token refresh
- [ ] Analytics: screen views, login, feature usage, enrollment
- [ ] User properties: role, schoolId (anonymized), locale
- [ ] `google-services.json` for production Firebase

---

### Story V2-09-S03: Performance Optimization [5 pts]
**Status:** Not Started
**Sprint:** 17

**Acceptance Criteria:**
- [ ] Cold start under 2s (`FullyDrawnReporter`)
- [ ] `LazyColumn` with proper `key` params everywhere
- [ ] No unnecessary recompositions (verified with Layout Inspector)
- [ ] Images: Coil with disk caching
- [ ] Room queries use proper indices
- [ ] StrictMode in debug builds

---

### Story V2-09-S04: App Signing & Play Store [3 pts]
**Status:** Not Started
**Sprint:** 17

**Acceptance Criteria:**
- [ ] Upload key and app signing key configured
- [ ] Play Store listing: title, description, screenshots (EN + AR)
- [ ] Content rating questionnaire completed
- [ ] Target API 35, versionCode/versionName strategy documented

---

### Story V2-09-S05: Accessibility Audit [5 pts]
**Status:** Not Started
**Sprint:** 17

**Acceptance Criteria:**
- [ ] All interactive elements have `contentDescription`
- [ ] Images: descriptive or marked decorative
- [ ] Color contrast WCAG AA (4.5:1 text, 3:1 large)
- [ ] Touch targets minimum 48dp
- [ ] TalkBack reads content meaningfully
- [ ] No critical issues from Accessibility Scanner
- [ ] Logical focus order

---

### Story V2-09-S06: Edge-to-Edge Polish [3 pts]
**Status:** Not Started
**Sprint:** 17

**Acceptance Criteria:**
- [ ] All screens respect system bar insets
- [ ] Transparent status bar with correct icon color
- [ ] Navigation bar padding via `WindowInsets.navigationBars`
- [ ] Cutout/notch handling
- [ ] Gesture nav doesn't conflict with app swipes

---

### Story V2-09-S07: Offline Connectivity UX [4 pts]
**Status:** Not Started
**Sprint:** 16

**As a** user with intermittent connectivity,
**I want** clear feedback about connection status and data freshness,
**So that** I understand what data I'm seeing.

**Acceptance Criteria:**
- [ ] Offline banner at top when network lost (uses `OfflineBanner` atom)
- [ ] Auto-dismiss when connection restored
- [ ] "Last synced X minutes ago" on data screens
- [ ] Sync-in-progress indicator (uses `SyncStatusBanner` atom)
- [ ] Pull-to-refresh triggers immediate sync
- [ ] Stale data (>1hr) shown with subtle indicator
