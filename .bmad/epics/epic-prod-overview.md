# Production-Ready Roadmap — Overview

**Source plan:** `~/.claude/plans/typed-wobbling-giraffe.md` (full detail, ~3500 lines)
**Generated:** 2026-04-26
**Owner:** Captain (handover via `/sprint-plan`)

This series supersedes the V2 plan (`epic-v2-01..09`). It reorganizes ~352 V2 points into ~1115 points across 28 epics in 6 dependency-ordered phases. The V2 epic files remain as historical drafts; their stories have been folded into the new epics with explicit cross-reference (see "Mapping" section below).

---

## Why a New Roadmap

The MVP shell is in place (35 modules, 533 feature Kotlin files, 1820+ EN strings, encrypted token storage, periodic sync, 24 nav graphs, Material 3 theme with RTL font swap). But the visible polish hides ten silent stubs that block any production claim:

1. Backend is largely fictional — only 2 of ~92 endpoints actually exist
2. `fallbackToDestructiveMigration()` will wipe user data on every schema bump
3. Mutation queue never transmits (all branches are TODO)
4. FCM tokens never reach the server
5. `google-services.json` missing from repo (clean build fails)
6. `SubmitGradeUseCase` returns synthesized fake response
7. Database content has no translation pattern (Arabic announcement invisible to English student)
8. 22 of 25 feature modules have zero unit tests
9. Local `UserRole` enum has 5 roles vs server's 8
10. `app/strings.xml` parity drift would fail `:app:checkStringParity`

The new roadmap fixes all of these and adds the user-requested DB-content translation pattern that mirrors hogwarts' `lang` column + `TranslationCache` design.

---

## 28 Epics in 6 Phases

| Phase | # | Epic | Pts | Sprint Target | Priority |
|---|---|---|---:|---|---|
| **A: Foundation** | E01 | Foundation Hardening | 42 | 1-2 | P0 |
| | E02 | Multi-Tenancy & RBAC Core | 39 | 1-3 | P0 |
| | E03 | Auth & Session Hardening | 34 | 2-3 | P0 |
| **B: Internationalization** | E04 | UI Internationalization | 34 | 3-4 | P0 |
| | E05 | RTL Layout & Typography | 29 | 3-4 | P0 |
| | E06 | Database Content Translation | 47 | 4-5 | P0 |
| | E07 | Locale-Aware Formatting | 18 | 4 | P1 |
| **C: Backend Integration** | E08 | API Contract & Endpoints | 89 | 4-7 | P0 |
| | E09 | Real-Time Layer (Socket.IO + FCM) | 42 | 6-7 | P1 |
| **D: Feature Completion** | E10 | Auth, Onboarding & Profile Surfaces | 34 | 7 | P0 |
| | E11 | Dashboard & Navigation | 26 | 7-8 | P0 |
| | E12 | SIS — Students, Staff, Subjects, Classes | 47 | 8-9 | P0 |
| | E13 | Attendance & Behaviour | 55 | 8-10 | P0 |
| | E14 | Grading, Exams & Report Cards | 55 | 9-11 | P0 |
| | E15 | Timetable & Curriculum | 34 | 10-11 | P1 |
| | E16 | Fees, Payments & Finance | 47 | 10-12 | P1 |
| | E17 | LMS — Stream, Lessons, Courses | 55 | 11-13 | P1 |
| | E18 | Communications | 47 | 11-13 | P1 |
| | E19 | Admissions, Events, Library, ID Card, Quiz | 47 | 12-13 | P2 |
| | E20 | Role-Specific Portals (Guardian, Teacher, Admin) | 39 | 12-13 | P1 |
| | E21 | Settings & Personalization | 18 | 13 | P2 |
| **E: Quality** | E22 | Test Infrastructure & Coverage | 55 | 14-15 | P0 |
| | E23 | Performance & Resource Optimization | 34 | 14-15 | P1 |
| | E24 | Accessibility | 26 | 14-15 | P1 |
| | E25 | Security Hardening | 29 | 14-15 | P0 |
| **F: Production** | E26 | Observability & Crash Reporting | 26 | 15-16 | P0 |
| | E27 | Release Pipeline & Distribution | 29 | 15-16 | P0 |
| | E28 | Play Store Launch | 18 | 16 | P0 |
| | | **Total** | **~1115** | **16 sprints (~32 weeks)** | |

---

## Architecture Decisions (locked)

See full plan §"Architecture Decisions". Key load-bearing choices:

- **AD-01: Single-tenant per session** — JWT carries one `schoolId`. Switching = logout. Mirrors hogwarts.
- **AD-03: Mirror `lang` + `TranslationCache` pattern verbatim** for DB content. Server is translation authority. Local cache mirrors web's `TranslationCache` table.
- **AD-04: `Tenant.nameEn` exception** — pre-translated school name (per `school.prisma:5-6`).
- **AD-05: Predicate-based ability layer** (CASL doesn't translate cleanly to Kotlin).
- **AD-06: 8 roles** — `DEVELOPER, ADMIN, TEACHER, STUDENT, GUARDIAN, ACCOUNTANT, STAFF, USER`.
- **AD-11: `ApiResult<T>` + `ActionErrorCode`** — mirror hogwarts' `ActionResponse<T>` and the ~250-entry error code list.
- **AD-18: OpenAPI 3.1 + Retrofit codegen** — single source of truth between Android and web.
- **AD-20: Per-locale string resources + server-driven dictionary JSON** for module-level labels.

---

## Cross-Cutting Conventions Enforced via CI

1. Every Room query MUST include `tenantId` (Detekt `RequireTenantScope`).
2. Every Composable text uses `stringResource(...)` (Detekt `NoHardcodedComposeText`).
3. All `Modifier.padding(start|end)` — never `left|right` (Detekt `NoLeftRightPadding`).
4. All translatable entities carry `lang: String` (Detekt `RequireLangColumn`).
5. All actions return `ApiResult<T>` (never throw to UI).
6. All money rendering goes through `LocaleAwareCurrencyFormatter`.
7. Conventional commits + co-author trailer + linked issue.

---

## Mapping to Existing V2 Epics

| New | Old V2 | Status |
|---|---|---|
| E01 (Foundation Hardening) | V2-09 partial | Net new + some V2-09 items moved here |
| E02 (Tenancy + RBAC) | none | **Net new core** |
| E03 (Auth Hardening) | V2-02 | Direct map |
| E04, E05, E07 | V2-03 | Split into UI strings / RTL layout / formatting |
| E06 (DB Translation) | none | **Net new — user's central ask** |
| E08 (API Contract) | V2-01 | Direct map; expanded with OpenAPI codegen |
| E09 (Real-Time) | V2-07 | Direct map |
| E10..E20 | partial V2-04, V2-06 | Reorganized by domain |
| E22 (Test Infra) | V2-08 | Expanded with Maestro + snapshot |
| E23 (Performance) | V2-09 partial | |
| E24 (Accessibility) | V2-09 partial | |
| E25 (Security) | none | Net new |
| E26 (Observability) | V2-09 partial | |
| E27 (Release Pipeline) | V2-09 partial | |
| E28 (Play Store Launch) | none | Net new explicit gate |

After this series is approved, the V2 epic files (`epic-v2-01..09.md`) can be retired — keep the `.md` files as historical, drop them from `bmm-workflow-status.yaml`.

---

## Files in This Series

Currently (Phase A only — others to be split as needed):

| File | Status |
|---|---|
| `epic-prod-overview.md` | This file |
| `epic-prod-01-foundation-hardening.md` | **Drafted** — Phase A starts here |
| `epic-prod-02-multi-tenancy-rbac.md` | **Drafted** |
| `epic-prod-03-auth-session-hardening.md` | **Drafted** |
| `epic-prod-04..28-*.md` | Pending — see full plan for content |

**For Phase B onward**, refer to `~/.claude/plans/typed-wobbling-giraffe.md` until split into individual files. Each epic in the plan has full goal + success criteria + ~10 stories with AC, files, refs, and estimates.

---

## Sprint Cadence (16 sprints × 2 weeks)

| Sprint | Phase | Headline epics | Velocity |
|---|---|---|---:|
| 1 | A | E01 starts, E02 starts | 50 |
| 2 | A | E01 finishes, E02 mid, E03 starts | 60 |
| 3 | A | E02/E03 finish, E04 starts | 65 |
| 4 | B | E04, E05, E07, E06 starts | 70 |
| 5 | B | E06, E08 starts | 70 |
| 6 | C | E08 mid, E09 starts | 75 |
| 7 | C/D | E08 finishes, E09 finishes, E10, E11 | 80 |
| 8 | D | E12, E13 starts | 80 |
| 9 | D | E13 mid, E14 starts | 80 |
| 10 | D | E13 finishes, E14 mid, E15, E16 starts | 80 |
| 11 | D | E14 finishes, E15 finishes, E17 / E18 start | 75 |
| 12 | D | E16 finishes, E18 mid, E19 / E20 start | 75 |
| 13 | D | E17 / E18 finish, E19 / E20 / E21 finish | 70 |
| 14 | E | E22 starts, E23, E24, E25 | 70 |
| 15 | E/F | E22 finishes, E26, E27 | 70 |
| 16 | F | E27 finishes, E28 (launch) | 50 |

---

## Definition of Done

**Story:** PR merged · conventional commit · linked issue · per-story tests pass · no new lint/Detekt violations · no coverage drop · manual smoke test or screenshot.

**Epic:** All stories merged · integration test green · docs updated · demo recording in closing PR · captain signoff.

**Release:** Tagged `vX.Y.Z` · release notes · mapping uploaded to Crashlytics · production-key signed · pre-launch report green · ≥24h on prior track.

---

## How to Resume

When picking up work:
1. Read this overview for context.
2. Open the active phase's epic file (e.g. `epic-prod-01-foundation-hardening.md`).
3. Read the cited file:line refs (every story has them).
4. Branch: `feat/E0X-S0Y-{kebab-case}` from `main`.
5. Implement — run `./gradlew :feature/<m>:lint :feature/<m>:test detekt spotlessCheck` locally.
6. PR with `Closes #N`.

For Phase B onward, until the corresponding `epic-prod-NN.md` files are split out, use the section in `~/.claude/plans/typed-wobbling-giraffe.md` as the source of truth.
