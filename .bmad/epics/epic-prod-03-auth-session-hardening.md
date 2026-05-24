# Epic E03: Auth & Session Hardening

**Epic ID:** EPIC-PROD-03
**Title:** Auth & Session Hardening
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0 (Blocking — feature surfaces depend on auth being solid)
**Sprint:** 2-3
**Total Points:** 34

---

## 1. Overview

Production-grade auth that mirrors hogwarts' Auth.js v5 patterns: cross-subdomain handling, JWT-DB refresh sync (role / schoolId propagation), Credential Manager integration, biometric re-login, and explicit OTP / forgot-password flows once backend (E08) lands.

### Business Value
- Cold start with valid token opens to dashboard in <1s without network.
- Token within 5 min of expiry refreshes silently — users never see auth errors.
- Login via Google + Facebook lands on the right tenant.
- Biometric re-login feels native on supported devices.
- After completing onboarding, the new `schoolId` propagates to the active session within 1s.

### Success Criteria
- [ ] Cold-start time-to-dashboard < 1s with valid cached token (no network call required).
- [ ] Token refreshed silently before next request when within 5 min of expiry.
- [ ] Google + Facebook OAuth flows land authenticated session on correct tenant.
- [ ] Biometric re-login works on devices with `BiometricManager.canAuthenticate() == BIOMETRIC_SUCCESS`.
- [ ] After onboarding completion, dashboard renders with correct tenant context within 1s.

---

## 2. Stories

### Story E03.S01: Migrate to Credential Manager API for password autofill [5 pts]
**Status:** Not Started · **Sprint:** 2

**As a** returning user, **I want** my saved password to autofill on the login screen, **So that** I sign in with one tap.

**Acceptance Criteria:**
- [ ] `feature/auth/.../login-screen.kt` uses `androidx.credentials.CredentialManager` to fetch saved credentials.
- [ ] On launch, attempts a `getCredential` call with `GetPasswordOption`; if a saved credential exists, pre-fills email + password.
- [ ] Falls back gracefully to manual entry on devices below API 28 / when no credentials saved.
- [ ] On successful login, calls `CreatePasswordRequest` to save (with user consent prompt).
- [ ] Existing 7 auth unit tests extended with 3 new Credential Manager tests.

**Files:** `feature/auth/.../login-screen.kt`, `feature/auth/.../login-view-model.kt`, `feature/auth/.../use-case/credential-manager-use-case.kt` (new).

**Refs:** existing `core/security/credential-manager.kt` (currently for biometric blob storage; new use case is for password storage).

---

### Story E03.S02: Migrate Google Sign-In to Credential Manager + GoogleID [5 pts]
**Status:** Not Started · **Sprint:** 2

**As a** returning user, **I want** Google Sign-In to use the modern unified Credential Manager flow, **So that** I get one-tap sign-in across Google ecosystem.

**Acceptance Criteria:**
- [ ] Existing Google sign-in code in `auth-repository-impl.kt` replaced with `GetGoogleIdOption` + `CredentialManager.getCredential(...)`.
- [ ] ID token sent to `POST /api/mobile/auth/google` (already declared in `auth-api.kt:8`).
- [ ] Server completes user provisioning + returns app JWT pair.
- [ ] If user has no Google account on device, button is hidden.
- [ ] Old `com.google.android.gms.auth.api.signin.*` deps removed if no longer used.

**Files:** `feature/auth/.../google-auth-use-case.kt`, `feature/auth/.../auth-repository-impl.kt`, `gradle/libs.versions.toml` (verify `googleid` 1.1.1 is still latest).

---

### Story E03.S03: Add Facebook OAuth via Facebook Android SDK [3 pts]
**Status:** Not Started · **Sprint:** 2

**As a** Facebook-using parent, **I want** to sign in with Facebook, **So that** I don't need a separate password.

**Acceptance Criteria:**
- [ ] `feature/auth/.../facebook-auth-use-case.kt` (new) implements Facebook Android SDK login flow.
- [ ] Access token sent to `POST /api/mobile/auth/facebook` (declared in `auth-api.kt:9`).
- [ ] Facebook App ID + Client Token configured via `BuildConfig` fields (set per build flavor).
- [ ] Login flow handles cancellation and error states gracefully.

**Files:** `feature/auth/.../facebook-auth-use-case.kt`, `feature/auth/.../auth-repository-impl.kt`, `app/src/main/AndroidManifest.xml` (FB activity registration).

**Refs:** server-side `auth.config.ts` notes "Facebook is OAuth2 only (not OIDC)".

---

### Story E03.S04: Implement `whoami` post-login + post-onboarding refresh [5 pts]
**Status:** Not Started · **Sprint:** 2

**As a** newly-onboarded school admin, **I want** my session to immediately reflect my new schoolId, **So that** I don't need to log out and back in.

**Acceptance Criteria:**
- [ ] After successful login OR after returning from an onboarding deep-link, `SessionRepository.refresh()` calls `GET /api/mobile/auth/whoami`.
- [ ] Response shape: `{ userId, role, schoolId?, enabledModules: List<String>, userScope: { studentId?, teacherId?, guardianId?, staffMemberId?, classIds: List<String>, childIds: List<String> } }`.
- [ ] Updates `TenantContext` with the latest values.
- [ ] Mirrors `auth.ts:600-639` JWT-refresh-from-DB pattern.
- [ ] If `whoami` returns `schoolId == null`, redirect user to onboarding.

**Files:** new `feature/auth/.../session-repository.kt`, new `core/network/api/whoami-api.kt`.

**Backend dependency:** E08.S04.

---

### Story E03.S05: Biometric re-login [5 pts]
**Status:** Not Started · **Sprint:** 3

**As a** returning user on a trusted device, **I want** to log back in with my fingerprint / face, **So that** I skip typing my password.

**Acceptance Criteria:**
- [ ] After successful password login, prompt user with "Enable biometric sign-in?" dialog.
- [ ] On enable: call `POST /api/mobile/auth/biometric-enroll` → server returns opaque "biometric session blob" → store in `EncryptedSharedPreferences` (file `hogwarts_biometric_creds`, already created).
- [ ] On next app launch with no valid token, prompt biometric → if matches, redeem blob via `POST /api/mobile/auth/biometric-login` → server returns fresh JWT pair.
- [ ] If blob is invalid (e.g. server rotated key), fall back to password login + clear local blob.
- [ ] Settings screen has "Disable biometric sign-in" toggle.
- [ ] Existing `core/security/biometric-helper.kt` and `credential-manager.kt` extended.

**Files:** `core/security/biometric-helper.kt`, `core/security/credential-manager.kt`, new `feature/auth/.../biometric-auth-use-case.kt`, `feature/settings/.../settings-screen.kt` (add toggle).

**Backend dependency:** E08.S03 (biometric endpoints).

---

### Story E03.S06: Forgot-password OTP flow end-to-end [5 pts]
**Status:** Not Started · **Sprint:** 3

**As a** user who forgot their password, **I want** to reset it via email OTP, **So that** I can regain access without contacting support.

**Acceptance Criteria:**
- [ ] Existing UI in `forgot-password-screen.kt`, `verify-screen.kt`, and a new `new-password-screen.kt` connect to:
  - `POST /api/mobile/auth/reset` (start, sends OTP email)
  - `POST /api/mobile/auth/verify-otp` (validates 6-digit OTP)
  - `POST /api/mobile/auth/new-password` (sets new password)
- [ ] OTP screen uses SMS Retriever API for auto-fill (where the OTP is sent via SMS in addition to email).
- [ ] Existing 1 unit test extended with 4 new tests (each endpoint).
- [ ] Rate limiting honored — UI shows "Too many attempts, try again in X min" on 429.

**Files:** `feature/auth/.../forgot-password-screen.kt`, `verify-screen.kt`, new `new-password-screen.kt`, repository.

**Backend dependency:** E08.S03.

---

### Story E03.S07: Sign-up flow [3 pts]
**Status:** Not Started · **Sprint:** 3

**As a** prospective tenant admin, **I want** to create an account directly in the app, **So that** I can start onboarding without needing the web portal.

**Acceptance Criteria:**
- [ ] `feature/auth/.../signup-screen.kt` connects to `POST /api/mobile/auth/register`.
- [ ] Existing `signup-validator-test.kt` extended with 5 edge-case tests (special chars in name, max length, weak password rules, duplicate email, etc.).
- [ ] After successful registration, user lands on onboarding flow (E10.S04 routing to school selector or new-school wizard).

**Files:** `feature/auth/.../signup-screen.kt`, validator tests.

**Backend dependency:** E08.S03.

---

### Story E03.S08: Tenant selector before login (multi-school users) [3 pts]
**Status:** Not Started · **Sprint:** 3

**As a** parent with kids in two schools, **I want** to choose which school to sign into, **So that** the right tenant context loads.

**Acceptance Criteria:**
- [ ] New screen `feature/auth/.../school-select-screen.kt`.
- [ ] After entering email on login screen, calls `GET /api/mobile/auth/schools?email=...` (already exists in `auth-api.kt`).
- [ ] If response has 0 schools, error UI ("Email not registered").
- [ ] If 1 school, auto-proceed to password screen with `schoolId` hint.
- [ ] If 2+ schools, show selector with school logo + name + city; user picks → password screen with `schoolId` hint.
- [ ] `LoginRequest` already accepts `schoolId` field (verify in `feature/auth/.../auth-api.kt`).

**Files:** new `feature/auth/.../school-select-screen.kt`, `school-select-view-model.kt`, nav graph wiring.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| Backend `/api/mobile/auth/*` supplementary endpoints | Required — see E08.S03 |
| Backend `/api/mobile/auth/whoami` | Required — see E08.S04 |
| Existing `TokenManager` + `SessionManager` | Available |
| Existing `BiometricHelper` + `CredentialManager` (security file) | Available |
| Credential Manager 1.3.0 + GoogleID 1.1.1 | Already in `libs.versions.toml` |

---

## 4. Risks

| Risk | Mitigation |
|------|------------|
| Credential Manager unsupported on legacy devices | E03.S01 falls back to manual entry on API < 28 |
| Facebook SDK adds APK size + privacy compliance | Hide button if Facebook App ID not configured; document privacy disclosure in E28.S04 |
| Biometric blob compromise on rooted devices | Server-side rate limit on `biometric-login`; rotate blob on every successful login |
| `whoami` race vs. JWT expiry | If `whoami` returns 401, fall through to refresh-token flow |

---

## 5. Out of Scope

- Phone-number-based sign-in → defer.
- WebAuthn / passkeys → defer (Credential Manager supports it, but server side not ready).
- 2FA / MFA → defer.

---

## 6. Definition of Done

- [ ] All 8 stories merged.
- [ ] Manual smoke: cold start with valid token → dashboard < 1s, no network blocking.
- [ ] Manual smoke: stub a near-expiry token → next API call refreshes silently.
- [ ] Manual smoke: sign in via Google → land on correct tenant with correct role.
- [ ] Manual smoke: enable biometric → kill app → cold start → biometric prompt → unlock.
- [ ] Manual smoke: forgot password → receive OTP via email → reset → log in with new password.
- [ ] Manual smoke: sign in with email having 2 schools → selector appears → pick → land on chosen tenant.
- [ ] Captain signoff documented.
