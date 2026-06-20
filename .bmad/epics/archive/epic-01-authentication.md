# Epic 1: Authentication

**Epic ID:** EPIC-01
**Title:** Authentication & Security
**Status:** In Progress
**Owner:** BMAD Dev Agent
**Sprint:** 1

---

## 1. Overview

Implement secure authentication flow integrating with Hogwarts web backend's mobile API.

### Business Value
- Enable users to securely access their school data
- Maintain session across app restarts
- Support multi-tenant isolation from login

### Success Criteria
- Users can login with email/password
- Tokens stored securely in EncryptedSharedPreferences
- Automatic token refresh before expiry
- Multi-tenant context established on login

---

## 2. Stories

### Story 1.1: Email/Password Login [DONE]
**Status:** Completed
**Points:** 5

**As a** user,
**I want** to login with my email and password,
**So that** I can access my school data.

**Acceptance Criteria:**
- [x] Login screen with email and password fields
- [x] Call POST /api/mobile/auth endpoint
- [x] Handle success response with tokens
- [x] Handle error responses (401, 422)
- [x] Navigate to dashboard on success

**Technical Notes:**
- Endpoint: `POST https://ed.databayt.org/api/mobile/auth`
- Response: `{ access_token, refresh_token, expires_at, user }`

---

### Story 1.2: Token Storage [DONE]
**Status:** Completed
**Points:** 3

**As a** user,
**I want** my login session to persist,
**So that** I don't have to login every time.

**Acceptance Criteria:**
- [x] Store access_token in EncryptedSharedPreferences
- [x] Store refresh_token in EncryptedSharedPreferences
- [x] Store user data (id, email, schoolId, role)
- [x] Clear tokens on logout

**Technical Notes:**
- Use `androidx.security.crypto.EncryptedSharedPreferences`
- Key alias: `hogwarts_master_key`

---

### Story 1.3: Token Refresh [DONE]
**Status:** Completed
**Points:** 3

**As a** user,
**I want** my session to automatically refresh,
**So that** I stay logged in.

**Acceptance Criteria:**
- [x] Detect token expiry before API calls
- [x] Call PUT /api/mobile/auth with refresh token
- [x] Update stored tokens
- [x] Handle refresh failure (redirect to login)

**Technical Notes:**
- Use OkHttp Authenticator for automatic retry
- Refresh when expires_at - current_time < 5 minutes

---

### Story 1.4: Logout
**Status:** Ready for Dev
**Points:** 2

**As a** user,
**I want** to logout from the app,
**So that** my data is protected.

**Acceptance Criteria:**
- [ ] Logout button in settings/profile
- [ ] Clear all tokens and user data
- [ ] Clear Room database
- [ ] Navigate to login screen
- [ ] Call POST /api/auth/signout (optional)

**Tasks:**
1. Add logout button to settings screen
2. Create LogoutUseCase
3. Clear TokenStorage
4. Clear Room database (HogwartsDatabase.clearAllTables())
5. Navigate to login with clearing back stack

---

### Story 1.5: Session Persistence
**Status:** Ready for Dev
**Points:** 2

**As a** user,
**I want** to stay logged in after closing the app,
**So that** I can quickly access my data.

**Acceptance Criteria:**
- [ ] Check token validity on app start
- [ ] Navigate directly to dashboard if valid
- [ ] Navigate to login if expired/missing
- [ ] Handle case where refresh fails

**Tasks:**
1. Create SplashScreen with auth check
2. Read tokens from EncryptedSharedPreferences
3. Validate expires_at timestamp
4. Attempt refresh if needed
5. Route to appropriate screen

---

### Story 1.6: Biometric Unlock [POST-MVP]
**Status:** Backlog
**Points:** 5

**As a** user,
**I want** to unlock the app with fingerprint,
**So that** login is faster and more secure.

**Acceptance Criteria:**
- [ ] Show biometric prompt after initial login
- [ ] Store biometric preference
- [ ] Use BiometricPrompt API
- [ ] Fallback to password if biometric fails

---

### Story 1.7: OAuth Login [POST-MVP]
**Status:** Backlog
**Points:** 8

**As a** user,
**I want** to login with Google or Facebook,
**So that** I don't need to remember another password.

**Acceptance Criteria:**
- [ ] Google Sign-In button
- [ ] Facebook Login button
- [ ] Handle OAuth flow with redirect
- [ ] Exchange OAuth token for app JWT

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| Mobile auth endpoint | Done |
| EncryptedSharedPreferences | Configured |
| OkHttp Authenticator | Configured |
| Navigation Compose | Configured |

---

## 4. Technical Architecture

```
feature/auth/
├── ui/
│   ├── login-screen.kt [DONE]
│   ├── login-view-model.kt [DONE]
│   ├── login-ui-state.kt [DONE]
│   └── splash-screen.kt [TODO]
├── domain/
│   ├── usecase/
│   │   ├── login-use-case.kt [DONE]
│   │   ├── logout-use-case.kt [TODO]
│   │   └── check-session-use-case.kt [TODO]
│   └── repository/
│       └── auth-repository.kt [DONE]
└── data/
    ├── repository/
    │   └── auth-repository-impl.kt [DONE]
    ├── remote/
    │   ├── auth-api.kt [DONE]
    │   └── dto/auth-dto.kt [DONE]
    └── local/
        └── token-storage.kt [DONE]
```

---

## 5. Risks

| Risk | Mitigation |
|------|------------|
| Token leak in logs | Mask tokens in logging interceptor |
| Biometric not available | Graceful fallback |
| Refresh race condition | Synchronized refresh |
