# Epic 1: Project Setup & Authentication

## Overview

Establish the Android project foundation and implement secure authentication with biometric support.

## Goals

- Set up multi-module Gradle project
- Implement login flow with JWT
- Add biometric authentication
- Handle offline authentication

## Stories

### 1.1 Project Initialization
**Status**: Completed

- Gradle configuration with Kotlin DSL
- Version catalog (libs.versions.toml)
- Module structure (core/, feature/)
- BMAD workflow files

### 1.2 Core Module Structure
**Status**: Not Started

**Acceptance Criteria**:
- [ ] core:common module with Result type, extensions
- [ ] core:data module with repository interfaces
- [ ] core:database module with Room setup
- [ ] core:network module with Retrofit setup
- [ ] core:designsystem module with Material 3 theme
- [ ] core:security module with encryption utilities

**Files**:
- `core/common/build.gradle.kts`
- `core/common/src/main/java/.../result/Result.kt`
- `core/data/build.gradle.kts`
- `core/data/src/main/java/.../tenant/TenantContext.kt`
- `core/database/build.gradle.kts`
- `core/database/src/main/java/.../HogwartsDatabase.kt`
- `core/network/build.gradle.kts`
- `core/network/src/main/java/.../ApiClient.kt`
- `core/designsystem/build.gradle.kts`
- `core/designsystem/src/main/java/.../theme/HogwartsTheme.kt`
- `core/security/build.gradle.kts`
- `core/security/src/main/java/.../EncryptedPrefs.kt`

### 1.3 Authentication UI
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Login screen with email/password fields
- [ ] Loading state during authentication
- [ ] Error state for invalid credentials
- [ ] Remember me checkbox
- [ ] RTL layout support
- [ ] Compose previews (Light, Dark, RTL)

**Files**:
- `feature/auth/ui/LoginScreen.kt`
- `feature/auth/ui/LoginViewModel.kt`
- `feature/auth/ui/LoginUiState.kt`
- `feature/auth/ui/components/LoginForm.kt`

### 1.4 JWT Token Management
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Store tokens in EncryptedSharedPreferences
- [ ] Auth interceptor adds Bearer token
- [ ] Token refresh at 80% lifetime
- [ ] Handle 401 responses (logout)
- [ ] Unit tests for TokenManager

**Files**:
- `core/security/src/main/java/.../TokenManager.kt`
- `core/network/src/main/java/.../AuthInterceptor.kt`
- `feature/auth/data/repository/AuthRepositoryImpl.kt`

### 1.5 Biometric Authentication
**Status**: Not Started

**Acceptance Criteria**:
- [ ] BiometricPrompt integration
- [ ] Fallback to password
- [ ] Check device capability
- [ ] Enable/disable in settings
- [ ] Unit tests

**Files**:
- `core/security/src/main/java/.../BiometricHelper.kt`
- `feature/auth/ui/BiometricPromptScreen.kt`

### 1.6 Session Management
**Status**: Not Started

**Acceptance Criteria**:
- [ ] Persist session across app restarts
- [ ] Offline authentication (cached credentials)
- [ ] Session timeout handling
- [ ] Logout clears all data
- [ ] Multi-tenant context from JWT

**Files**:
- `core/data/src/main/java/.../session/SessionManager.kt`
- `feature/auth/domain/usecase/ValidateSessionUseCase.kt`

## Dependencies

- None (first epic)

## Technical Notes

### Auth Flow

```
1. User enters credentials
2. POST /api/auth/callback/credentials
3. Receive JWT (access + refresh tokens)
4. Store in EncryptedSharedPreferences
5. Decode JWT for user info + schoolId
6. Navigate to role-specific dashboard
```

### Token Structure

```json
{
  "sub": "user-id",
  "email": "user@school.edu",
  "role": "STUDENT",
  "schoolId": "school-123",
  "exp": 1234567890
}
```

## Risks

- API rate limiting on login attempts
- Biometric not available on all devices
- Token refresh timing edge cases
