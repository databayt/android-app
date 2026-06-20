# Epic E25: Security Hardening

**Epic ID:** EPIC-PROD-25
**Title:** Security Hardening
**Status:** Not Started
**Owner:** BMAD Dev Agent + Security review
**Priority:** P0
**Sprint:** 14-15
**Total Points:** 29

---

## 1. Overview

Production-grade security. Pin certs to real values. Detect rooted/jailbroken devices. Enforce Play Integrity. Audit auditable actions.

### Success Criteria
- [ ] Real cert pin values in `SecurityConfig` for `*.databayt.org`.
- [ ] Play Integrity blocks devices that fail device integrity check.
- [ ] Sensitive actions emit audit log events to server.
- [ ] OWASP Mobile Top 10 reviewed; gaps tracked.
- [ ] No secrets in logs (verified by custom Timber tree).

---

## 2. Stories

| ID | Title | Pts | Sprint | Notes |
|---|---|---:|---|---|
| E25.S01 | Real cert pin values in `SecurityConfig` **(Phase: Pilot v1)** | 3 | 14 | |
| E25.S02 | `network_security_config.xml` `<pin-set>` block uncommented | 2 | 14 | |
| E25.S03 | Play Integrity API integration | 5 | 14 | Server validation |
| E25.S04 | Root/Jailbreak detection (RootBeer) — warn but not block | 3 | 14 | |
| E25.S05 | Rate limiting on auth endpoints | 3 | 14 | Server-side |
| E25.S06 | OWASP Mobile Top 10 audit + gap tracking | 5 | 15 | |
| E25.S07 | Audit log emit (client-side actions) | 3 | 15 | |
| E25.S08 | Sensitive data redaction in logs (custom Timber tree) **(Phase: Pilot v1)** | 2 | 15 | |
| E25.S09 | Disable debugger in release build (verify) | 1 | 15 | |
| E25.S10 | DataStore encryption for sensitive prefs | 3 | 15 | |

### Detailed AC: E25.S01 — Cert pinning
- [ ] `core/network/security/security-config.kt` `PRIMARY_PIN`, `BACKUP_PIN` set to actual SHA-256 base64 of `*.databayt.org` cert chain.
- [ ] Documented rotation procedure in `docs/security/cert-rotation.md`.
- [ ] Test: rotate cert in staging → primary pin fails → backup pin recovers.

### Detailed AC: E25.S03 — Play Integrity
- [ ] On login, call Play Integrity API → send token to `POST /api/mobile/auth/integrity-check`.
- [ ] Server validates → blocks if not `MEETS_DEVICE_INTEGRITY` or `MEETS_BASIC_INTEGRITY`.
- [ ] Configurable per tenant: hard-block vs warn-only.

### Detailed AC: E25.S07 — Audit log
- [ ] Sensitive actions: login, password change, payment, role change, account deletion.
- [ ] `POST /api/mobile/audit` `{ action, resource, timestamp, deviceMeta }`.
- [ ] Mirrors web's `src/lib/audit-log.ts`.

### Detailed AC: E25.S08 — Log redaction
- [ ] Custom `Timber.Tree` regex-redacts: `Authorization: Bearer ...`, `password=...`, `\d{16}` (card numbers), `[A-Z0-9]{20,}` (potential tokens).
- [ ] Crashlytics breadcrumbs sanitized on submit.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| Cert chain access for pin computation | Required (ops) |
| Play Console access for Play Integrity API | Required |
| Server `POST /api/mobile/audit` endpoint | Required |
| RootBeer library | New dep |

---

## 4. DoD

- [ ] All 10 stories merged.
- [ ] Cert pin verified by intentionally rotating cert + observing pin failure → backup recovery.
- [ ] Play Integrity blocks emulator (test config).
- [ ] OWASP audit doc at `docs/security/owasp-mobile-top10-2026.md`.
- [ ] Captain + security review signoff.
