# Epic V2-02: Auth Hardening & Modern Credentials

**Epic ID:** EPIC-V2-02
**Title:** Auth Hardening & Modern Credential Management
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0
**Sprint:** 13-14
**Total Points:** 39

---

## 1. Overview

Complete auth coupling with the Hogwarts backend. Implement modern Android Credential Manager for Google Sign-In, wire Facebook OAuth, registration, password reset, biometric unlock, and security hardening.

### Business Value
- Users can register, login with social providers, and recover passwords
- Biometric unlock provides fast, secure re-authentication
- Certificate pinning prevents MITM attacks

### Success Criteria
- Full login -> OAuth -> refresh -> logout cycle works against production
- Google Sign-In uses Credential Manager API (not legacy GoogleSignInClient)
- All auth screens functional with real backend endpoints

---

## 2. Stories

### Story V2-02-S01: Credential Manager Google Sign-In [8 pts]
**Status:** Not Started
**Sprint:** 13

**As a** user,
**I want** to sign in with Google via Credential Manager API,
**So that** login follows Android best practices.

**Acceptance Criteria:**
- [ ] Uses `androidx.credentials:credentials:1.3.0` with `GetGoogleIdOption`
- [ ] Sends ID token to POST `api/mobile/auth/google`
- [ ] Handles `GetCredentialException` gracefully
- [ ] Works without Google Play Services (fallback to email/password)
- [ ] Stores resulting JWT pair in TokenManager

**Technical Notes:**
- Libraries in `libs.versions.toml` but not wired
- Rewrite `google-auth-use-case.kt` to use Credential Manager

---

### Story V2-02-S02: Facebook Login [5 pts]
**Status:** Not Started
**Sprint:** 13

**As a** user,
**I want** Facebook sign-in,
**So that** I have an alternative social login.

**Acceptance Criteria:**
- [ ] Facebook Login SDK or Custom Tab OAuth flow
- [ ] Sends access token to POST `api/mobile/auth/facebook`
- [ ] Handles cancellation and errors
- [ ] Stores resulting JWT pair

---

### Story V2-02-S03: Registration Flow [5 pts]
**Status:** Not Started
**Sprint:** 13

**As a** new user,
**I want** to register by selecting my school and providing details,
**So that** I can access the mobile app.

**Acceptance Criteria:**
- [ ] School picker from GET `api/mobile/schools`
- [ ] Form: firstName, lastName, email, password, school
- [ ] Client validation via existing `SignupValidator`
- [ ] On success, auto-login and navigate to dashboard
- [ ] On failure, show field-level errors

**Technical Notes:**
- `signup-screen.kt` and `signup-view-model.kt` already exist

---

### Story V2-02-S04: Forgot Password & OTP [5 pts]
**Status:** Not Started
**Sprint:** 13

**As a** user who forgot my password,
**I want** to reset it via OTP,
**So that** I can regain access.

**Acceptance Criteria:**
- [ ] Email entry -> POST `api/mobile/auth/reset`
- [ ] OTP input (6-digit) -> POST `api/mobile/auth/verify-otp`
- [ ] New password -> POST `api/mobile/auth/new-password`
- [ ] Success navigates to login with banner

**Technical Notes:**
- `forgot-password-screen.kt`, `verify-screen.kt` already exist

---

### Story V2-02-S05: Biometric Unlock [5 pts]
**Status:** Not Started
**Sprint:** 14

**As a** returning user,
**I want** fingerprint/face unlock,
**So that** login is faster and more secure.

**Acceptance Criteria:**
- [ ] Prompt to enable after first login
- [ ] Uses `BiometricPrompt` API
- [ ] On success, retrieves stored credentials -> re-authenticates
- [ ] Falls back to email/password after 3 failures
- [ ] Settings toggle for enable/disable

**Technical Notes:**
- `BiometricHelper` and `CredentialManager` exist in `core/security`

---

### Story V2-02-S06: Session Management [3 pts]
**Status:** Not Started
**Sprint:** 14

**As a** user,
**I want** persistent sessions and clean logout,
**So that** my experience is seamless and secure.

**Acceptance Criteria:**
- [ ] App start: check tokens -> refresh if needed -> dashboard or login
- [ ] Logout clears: TokenManager, CredentialManager, Room, DataStore, FCM token
- [ ] Back stack cleared on logout

---

### Story V2-02-S07: Certificate Pinning & Security [5 pts]
**Status:** Not Started
**Sprint:** 14

**As a** developer,
**I want** TLS cert pinning and no token leakage,
**So that** users are protected against MITM attacks.

**Acceptance Criteria:**
- [ ] OkHttp CertificatePinner with real SHA-256 pins for `ed.databayt.org`
- [ ] Logging interceptor redacts Authorization headers
- [ ] No tokens in Logcat in release builds
- [ ] Cleartext traffic blocked

**Technical Notes:**
- Fulfills existing EPIC-17-05

---

### Story V2-02-S08: Auth Tests [3 pts]
**Status:** Not Started
**Sprint:** 14

**Acceptance Criteria:**
- [ ] Unit tests for all auth use cases and ViewModels
- [ ] Integration test: login -> refresh -> logout
- [ ] Min 85% coverage on auth module
