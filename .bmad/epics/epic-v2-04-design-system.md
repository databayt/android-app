# Epic V2-04: Design System -- Apple HIG Compliance

**Epic ID:** EPIC-V2-04
**Title:** Design System Enhancement -- Apple HIG Compliance
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P1
**Sprint:** 12-16
**Total Points:** 44

---

## 1. Overview

The existing design system has Apple-inspired atoms (GlassCard, StatusBadge, etc.) and patterns (Apple Tab Bar, Inset Grouped List). This epic adds missing Apple HIG patterns: large title nav bars, pull-to-dismiss sheets, predictive back, adaptive layouts, haptic integration, and skeleton loading.

### Business Value
- Polished, iOS-quality user experience on Android
- Tablet/foldable support expands addressable devices
- Accessibility compliance (Dynamic Type, touch targets)

### Success Criteria
- Large title collapsing nav bars on all list screens
- Bottom sheets with iOS-style detent behavior
- Adaptive two-pane layouts on tablets
- Skeleton loading during all data fetches

---

## 2. Stories

### Story V2-04-S01: Large Title Collapsing Nav Bar [8 pts]
**Status:** Not Started
**Sprint:** 12

**As a** user,
**I want** iOS-style large titles that collapse on scroll,
**So that** the app feels polished and modern.

**Acceptance Criteria:**
- [ ] Create `AppleLargeTitleScaffold` in `core/designsystem`
- [ ] Large title (34sp, bold) at top, collapses to inline (17sp) on scroll
- [ ] Spring animation during transition
- [ ] Optional search bar revealing on pull-down
- [ ] Applied to: students, grades, attendance, stream catalog, messaging

---

### Story V2-04-S02: Pull-to-Dismiss Bottom Sheets [5 pts]
**Status:** Not Started
**Sprint:** 13

**As a** user,
**I want** bottom sheets with iOS-style detent stops,
**So that** sheets behave predictably.

**Acceptance Criteria:**
- [ ] `AppleBottomSheet` with configurable detents (25%, 50%, 90%)
- [ ] Snaps to nearest detent on release
- [ ] Pull below smallest detent dismisses
- [ ] Drag indicator bar, proportional dim overlay

**Technical Notes:**
- `bottom-sheet-detents.kt` exists -- enhance to match full iOS behavior

---

### Story V2-04-S03: Dynamic Type & Accessibility [5 pts]
**Status:** Not Started
**Sprint:** 14

**As a** user with visual impairments,
**I want** text to scale with accessibility settings,
**So that** I can read comfortably.

**Acceptance Criteria:**
- [ ] App usable at 200% font scale
- [ ] No text truncation on critical screens (login, dashboard, grades)
- [ ] Touch targets minimum 48dp
- [ ] `contentDescription` on all interactive elements
- [ ] TalkBack navigates all screens meaningfully

---

### Story V2-04-S04: Predictive Back Gesture [5 pts]
**Status:** Not Started
**Sprint:** 16

**As a** user on Android 14+,
**I want** predictive back animation,
**So that** I can preview where back will go.

**Acceptance Criteria:**
- [ ] `android:enableOnBackInvokedCallback="true"` in manifest
- [ ] `PredictiveBackHandler` for custom back behavior
- [ ] Preview animation (cross-fade/shrink)
- [ ] Fallback to standard back on Android 13 and below

---

### Story V2-04-S05: Skeleton Loading [5 pts, ongoing]
**Status:** Not Started

**As a** user,
**I want** shimmer skeletons while data loads,
**So that** screens feel fast.

**Acceptance Criteria:**
- [ ] Feature-specific skeletons: `DashboardSkeleton`, `GradesListSkeleton`, `CourseCatalogSkeleton`, `ChatSkeleton`
- [ ] Cross-fade from skeleton to real content
- [ ] Skeleton shape matches real content layout

**Technical Notes:**
- Generic skeletons exist in `skeleton-loading.kt`

---

### Story V2-04-S06: Haptic Feedback Integration [3 pts, ongoing]
**Status:** Not Started

**As a** user,
**I want** subtle haptic feedback on interactions,
**So that** the app feels tactile.

**Acceptance Criteria:**
- [ ] Selection haptic: tab bar, list select, checkbox
- [ ] Impact (light): button press, card tap
- [ ] Notification (success): form submit, payment, lesson complete
- [ ] Notification (error): validation, network error
- [ ] Respects system haptic settings

**Technical Notes:**
- `HapticFeedbackHelper` exists with `impact()`/`notification()`/`selection()`

---

### Story V2-04-S07: Adaptive Layouts [8 pts]
**Status:** Not Started
**Sprint:** 16

**As a** tablet/foldable user,
**I want** two-pane layouts on larger screens,
**So that** screen space is used effectively.

**Acceptance Criteria:**
- [ ] `WindowSizeClass` detection (Compact/Medium/Expanded)
- [ ] Two-pane list-detail for: students, messaging, courses, grades
- [ ] Navigation rail instead of bottom tab bar on expanded width
- [ ] Content max-width constraints on large screens
- [ ] Test on Pixel Fold and 10" tablet emulators

**Technical Notes:**
- Add `material3-adaptive` dependency

---

### Story V2-04-S08: Context Menu & Swipe Actions [5 pts]
**Status:** Not Started
**Sprint:** 15

**As a** user,
**I want** long-press menus and swipe actions,
**So that** I can interact efficiently.

**Acceptance Criteria:**
- [ ] Long-press context menus (Apple-style with blur)
- [ ] Swipe-to-delete: notifications, messages
- [ ] Swipe-to-mark-read: messages, notifications
- [ ] Haptic feedback on context menu appear

**Technical Notes:**
- `context-menu.kt` exists -- enhance
