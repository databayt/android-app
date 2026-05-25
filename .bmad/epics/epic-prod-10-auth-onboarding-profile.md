# Epic E10: Auth, Onboarding & Profile Surfaces

**Epic ID:** EPIC-PROD-10
**Title:** Auth, Onboarding & Profile Surfaces
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0
**Sprint:** 7
**Total Points:** 34

---

## 1. Overview

Complete the auth + first-run + profile UI surfaces, building on the auth backend hardening from E03 + E08.

### Success Criteria
- [ ] Every auth screen connects to a real endpoint and handles all documented edge cases.
- [ ] OTP screen auto-fills via SMS Retriever API.
- [ ] Avatar upload via gallery + camera works offline-first (queues if offline).
- [ ] Account deletion flow ships per Google Play 2026 policy.

---

## 2. Stories

### Story E10.S01: Welcome / login / signup flow polish [5 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] Edge cases: invalid email, weak password, account locked, school not found, OAuth account-linking, network offline.
- [ ] Each error maps to a localized `ActionErrorCode`.
- [ ] UI matches Figma; animations honor reduced-motion preference (E24.S06).
- [ ] Existing 7 auth tests extended with 5 new edge-case tests.

**Files:** `feature/auth/.../{welcome,login,signup}-screen.kt`, view-models, tests.

---

### Story E10.S02: OTP screen with SMS auto-fill [3 pts]
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] SMS Retriever API listens for OTP messages with the app's signature hash.
- [ ] Auto-fills 6-digit code; user just taps "Verify".
- [ ] Falls back to manual entry if no SMS arrives within 30s.
- [ ] Resend button enabled after 60s with countdown.

**Files:** `feature/auth/.../verify-screen.kt`, new `feature/auth/.../sms-retriever-helper.kt`.

---

### Story E10.S03: Forgot/reset password [3 pts]
**Status:** Not Started · **Sprint:** 7
**Depends on:** E03.S06.

**Acceptance Criteria:**
- [ ] `forgot-password-screen` → `verify-screen` (OTP) → `new-password-screen` flow.
- [ ] Resend OTP throttled.
- [ ] Success toast routes to login screen.

---

### Story E10.S04: School selector for multi-school users [3 pts]
**Status:** Not Started · **Sprint:** 7
**Depends on:** E03.S08.

**Acceptance Criteria:**
- [ ] After email entry, fetch schools; if >1, show selector with logo + name + city.
- [ ] If exactly 1, auto-proceed.
- [ ] If 0, show "Email not registered" with "Sign up" CTA.

---

### Story E10.S05: Profile view + edit [5 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] `feature/profile/.../profile-screen.kt` shows all editable fields per role.
- [ ] `PUT /api/mobile/profile` (E08.S07) for edits.
- [ ] Optimistic UI: edit shows immediately, persists on save, reverts on error.
- [ ] Field-level validation via Zod-equivalent Kotlinx schemas.

**Files:** `feature/profile/.../profile-screen.kt`, `profile-edit-screen.kt`, view-models.

---

### Story E10.S06: Avatar upload via Coil + WorkManager [5 pts]
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] Pick image from gallery or camera (CameraX already in deps).
- [ ] Upload via `POST /api/mobile/profile/avatar` (multipart).
- [ ] WorkManager `AvatarUploadWorker` with retry on failure.
- [ ] Optimistic UI: local preview shown immediately; replaced with server URL on success.
- [ ] Image cropping to 1:1 with `androidx.activity.compose.rememberLauncherForActivityResult`.

**Files:** new `feature/profile/.../avatar-picker.kt`, `feature/profile/.../worker/avatar-upload-worker.kt`, `core/network/api/profile-api.kt`.

---

### Story E10.S07: Change password [3 pts]
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] `feature/profile/.../change-password-screen.kt` requires current + new password.
- [ ] `POST /api/mobile/profile/change-password` server-side.
- [ ] Success → forced re-login (security).

---

### Story E10.S08: Account deletion (Play Store policy) [5 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 7

**As a** user, **I want** to delete my account from within the app, **So that** the app meets Google Play 2026 self-service deletion policy.

**Acceptance Criteria:**
- [ ] `feature/profile/.../delete-account-screen.kt` with strong warning + password re-entry confirmation.
- [ ] `POST /api/mobile/profile/delete` server soft-deletes with 30-day grace period.
- [ ] User can cancel deletion within grace period.
- [ ] Deletion clears local state via `LogoutOrchestrator` (E02.S09).
- [ ] Privacy policy linked from this screen documents data retention.

---

### Story E10.S09: Privacy + ToS in-app viewer [2 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 7

**Acceptance Criteria:**
- [ ] Settings → "Privacy Policy" / "Terms of Service" opens in-app webview.
- [ ] URL points to `https://ed.databayt.org/{ar,en}/privacy` per current locale.
- [ ] Copy of latest policy bundled in `assets/` for offline display.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E03 auth hardening | Required |
| E08.S07 profile endpoints | Required |
| E08.S03 auth supplementary endpoints | Required |
| Google Play Account Deletion API access | Required (Play Console setup) |

---

## 4. DoD

- [ ] All 9 stories merged.
- [ ] Manual smoke for each flow on Android 11, 13, 15.
- [ ] Account deletion flow approved by Play Console review.
- [ ] Captain signoff documented.
