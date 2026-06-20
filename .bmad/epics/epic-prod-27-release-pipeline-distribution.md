# Epic E27: Release Pipeline & Distribution

**Epic ID:** EPIC-PROD-27
**Title:** Release Pipeline & Distribution
**Status:** Not Started
**Owner:** BMAD Dev Agent + ops
**Priority:** P0
**Sprint:** 15-16
**Total Points:** 29

---

## 1. Overview

One-button release. Signed bundles. Multi-track distribution (internal, alpha, beta, prod). Mapping uploads. Reproducible builds.

### Success Criteria
- [ ] Tagged release `vX.Y.Z` triggers signed bundle + mapping upload + Play Internal track distribution.
- [ ] CI <8 min wall-clock per PR.
- [ ] Nightly staging build to Firebase App Distribution.
- [ ] Dependency vulnerability scan weekly.
- [ ] Reproducible builds verifiable.

---

## 2. Stories

| ID | Title | Pts | Sprint |
|---|---|---:|---|
| E27.S01 | GitHub Actions: PR build + tests + lint + Detekt **(Phase: Pilot v1)** | 5 | 15 |
| E27.S02 | GitHub Actions: nightly staging build → Firebase App Distribution | 3 | 15 |
| E27.S03 | GitHub Actions: tagged release → Play Internal Testing **(Phase: Pilot v1)** | 5 | 15 |
| E27.S04 | Signing key in GitHub Secrets + rotation procedure **(Phase: Pilot v1)** | 2 | 15 |
| E27.S05 | Play Console API integration via `gradle-play-publisher` | 3 | 16 |
| E27.S06 | Dependency vulnerability scanning (Dependabot + OWASP dependency-check) | 2 | 16 |
| E27.S07 | Renovate for dependency updates | 2 | 16 |
| E27.S08 | Release notes auto-generation from conventional commits | 2 | 16 |
| E27.S09 | Build cache (Gradle remote cache) | 3 | 16 |
| E27.S10 | Reproducible builds verification | 2 | 16 |

### Detailed AC: E27.S01 — PR workflow
- [ ] `.github/workflows/pr.yaml`:
  - Checkout
  - Setup JDK 17, Gradle cache restore
  - `./gradlew :app:assembleDebug :app:lintDebug detekt spotlessCheck :koverXmlReport`
  - Upload coverage to Codecov
  - Comment on PR with size delta + coverage delta
- [ ] Wall-clock <8 min on `ubuntu-latest` runner.
- [ ] Concurrent runs cancel previous pending run for same PR.

### Detailed AC: E27.S03 — Release workflow
- [ ] `.github/workflows/release.yaml` triggered on tag `v*.*.*`:
  - Build signed `app-release.aab` with `playStore` flavor + signing config from secrets
  - Upload mapping to Crashlytics + Play Console
  - Upload bundle to Play Internal Testing track
  - Generate release notes from conventional commits since prior tag
  - Create GitHub Release with notes + AAB + mapping artifacts
- [ ] Manual approval gate before promote to alpha → beta → prod.

### Detailed AC: E27.S04 — Signing
- [ ] Upload key in GitHub Encrypted Secrets: `SIGNING_KEY_BASE64`, `SIGNING_KEY_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_ALIAS_PASSWORD`.
- [ ] Rotation procedure documented at `docs/release/signing-key-rotation.md`.
- [ ] Backup of signing key in 1Password / Keychain (offline).

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| Play Console API access | Required (ops) |
| Firebase App Distribution access | Required |
| GitHub repo secrets | Required |
| `gradle-play-publisher` 3.x | New dep |

---

## 4. DoD

- [ ] All 10 stories merged.
- [ ] Tagged release E2E: `git tag v0.9.0` → workflow runs → AAB on Play Internal track.
- [ ] PR workflow <8 min on average.
- [ ] Captain + ops signoff.
