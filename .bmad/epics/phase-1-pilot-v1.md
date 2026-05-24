# Phase 1 — Pilot v1 (King Fahad Schools)

**Release phase:** 1 of 3
**Sprints:** P1, P2, P3 (~7 weeks)
**Target points:** ~296
**Target date:** 2026-07-12
**Pilot tenant:** King Fahad Schools (Sudan, K-12) — free pilot active since Apr 2026; conversion conversation Jun 2026.
**Status:** Not Started
**Owner:** Captain (kun) + Samia (R&D)
**Sibling phases:** [phase-2-mena10.md](./phase-2-mena10.md) · [phase-3-launch.md](./phase-3-launch.md)
**Source plan:** [`~/.claude/plans/take-web-app-as-precious-turtle.md`](../../../.claude/plans/take-web-app-as-precious-turtle.md)

---

## Intent

The slimmest mobile companion that makes parents and students at King Fahad happy enough to recommend conversion to paid, and gives the sales conversation an admission demo for the next intake season.

**What ships:** auth (incl. biometric), read-only views of grades / attendance / fees / timetable, announcements + notifications (FCM data-channel), Arabic-complete UI, full admission flow, signed APK on Play Internal Testing track, Crashlytics + Performance + Analytics.

**What does NOT ship in Phase 1** (moved to Phase 2 or 3): teacher write flows, Socket.IO real-time messaging, fee payment / Stripe, OAuth (Google/Facebook), LMS / Stream / Lessons, Library, ID Card / Wallet, Quiz, exam interactions, public Play Store track.

---

## Story selection by epic

Each entry links to the source story in the epic file. Stories are listed in dependency order within each epic.

### E01 — Foundation Hardening (~26 pts)

Source: [`epic-prod-01-foundation-hardening.md`](./epic-prod-01-foundation-hardening.md)

| Story | Pts | Sprint | Why pilot-critical |
|---|---:|---|---|
| E01.S01 | 2 | P1 | `google-services.json` committed — FCM cannot init without it; pilot demo fails on fresh checkout |
| E01.S02 | 8 | P1 | Replace `fallbackToDestructiveMigration()` — pilot users upgrade across schema bumps; wiping their cache kills trust |
| E01.S03 | 8 | P1 | Mutation queue actually transmits — required so FCM token upload + read receipts don't drop |
| E01.S04 | 3 | P1 | Upload FCM token on first auth — without it, push notifications never arrive |
| E01.S05 | 2 | P1 | One source of truth for API base URL — avoids prod-vs-staging debug regression |
| E01.S06 | 1 | P1 | Fix EN/AR parity drift — Arabic is primary; missing one string fails `:app:checkStringParity` |
| E01.S07 | 2 | P1 | Wire `AppStartupInitializer` — needed for Crashlytics + StrictMode initialisation |

**Deferred to Phase 2:** S08 Detekt `NoHardcodedComposeText`, S09 Detekt `RequireTenantScope`, S10 Spotless, S11 Kover, S12 lint promotion. These are CI hygiene; not pilot-gating.

### E02 — Multi-Tenancy & RBAC (~15 pts)

Source: [`epic-prod-02-multi-tenancy-rbac.md`](./epic-prod-02-multi-tenancy-rbac.md)

| Story | Pts | Sprint | Why pilot-critical |
|---|---:|---|---|
| E02.S01 | 2 | P1 | 8-role enum — server returns 8 roles (DEVELOPER, ADMIN, TEACHER, STUDENT, GUARDIAN, ACCOUNTANT, STAFF, USER); local mismatch deserialises wrong |
| E02.S02 | 3 | P1 | Move `SessionManagerImpl` to `core/security` — unblocks S06 |
| E02.S06 | 5 | P1 | `TenantContext` with `userScope` — needed so dashboard knows which child a guardian is viewing |
| E02.S08 | 2 | P2 | `TenantInterceptor` reads flow — logout doesn't fire stale tenant requests |
| E02.S09 | 3 | P2 | Logout orchestrator — King Fahad is a shared-device family scenario |

**Deferred to Phase 2:** S03–S05 (Action/Subject taxonomy, full predicate ability layer, `@SessionScoped`), S07 (Detekt sweep), S10 (cross-tenant integration test). Phase 1 is read-only — inline `role in allowedRoles` checks suffice.

### E03 — Auth & Session Hardening (~23 pts)

Source: [`epic-prod-03-auth-session-hardening.md`](./epic-prod-03-auth-session-hardening.md)

| Story | Pts | Sprint | Why pilot-critical |
|---|---:|---|---|
| E03.S01 | 5 | P2 | Credential Manager autofill — single-tap return is daily-use feature |
| E03.S04 | 5 | P2 | `whoami` post-login — dashboard renders correct tenant + role on first launch |
| E03.S05 | 5 | P2 | Biometric re-login — King Fahad parents asked for this in pilot feedback |
| E03.S06 | 5 | P2 | Forgot-password OTP — locked-out parents must self-recover |
| E03.S08 | 3 | P2 | School selector — King Fahad parents with kids on multiple campuses hit this |

**Deferred to Phase 2:** S02 Google OAuth, S03 Facebook OAuth, S07 sign-up. Pilot users are pre-provisioned by the school; self-signup not needed.

### E04 — UI Internationalization (~24 pts)

Source: [`epic-prod-04-ui-internationalization.md`](./epic-prod-04-ui-internationalization.md)

| Story | Pts | Sprint | Why pilot-critical |
|---|---:|---|---|
| E04.S01 | 8 | P1 | Native-speaker translation audit — Arabic primary; awkward translations fail trust |
| E04.S02 | 8 | P2 | Sweep `Text("literal")` violations — depends on audit list, not the Detekt rule (deferred) |
| E04.S05 | 5 | P2 | Locale switch without restart — power users will toggle to verify |
| E04.S06 | 3 | P2 | Arabic six-form plurals — counts read wrong otherwise |

**Deferred to Phase 2:** S03 server dictionary loader, S04 per-module string-parity build task, S07 docs.

### E05 — RTL Layout & Typography (~16 pts)

Source: [`epic-prod-05-rtl-layout-typography.md`](./epic-prod-05-rtl-layout-typography.md)

| Story | Pts | Sprint | Why pilot-critical |
|---|---:|---|---|
| E05.S01 | 8 | P1 | RTL padding correctness sweep — visible mirroring bugs would tank pilot |
| E05.S02 | 3 | P2 | Auto-mirroring icons — wrong-direction back arrows signal "broken" |
| E05.S04 | 3 | P2 | Font-swap audit — Arabic must use SF Arabic / Noto Arabic, not Latin |
| E05.S05 | 2 | P2 | Bidi-safe number rendering — phone numbers, OTPs, fee amounts |

**Deferred to Phase 3:** S03 RTL snapshot tests. Manual visual audit acceptable for 1 pilot school.

### E06 — Database Content Translation (~29 pts)

Source: [`epic-prod-06-database-content-translation.md`](./epic-prod-06-database-content-translation.md)

| Story | Pts | Sprint | Why pilot-critical |
|---|---:|---|---|
| E06.S01 | 8 | P2 | Add `lang: String` column to translatable entities — schema must absorb this before Phase 2 writes |
| E06.S02 | 3 | P2 | `TranslationCacheEntity` + DAO |
| E06.S03 | 8 | P3 | `TranslationRepository.getDisplayText` — minimal path: same-lang short-circuit + script-mismatch guard + cache lookup |
| E06.S05 | 2 | P3 | `Tenant.nameEn` pre-translated — King Fahad's English name fixed |
| E06.S07 | 5 | P3 | Pre-seed canonical translations (grade levels, subjects, terms, role names). Phase 1 ships without `POST /api/mobile/translate` because King Fahad's announcement labels hit only canonical seeds |
| E06.S10 | 3 | P3 | `LocalizedText` Composable — drop-in for all display sites |

**Deferred to Phase 2:** S04 API endpoint, S06 value class, S08 LRU eviction, S09 Detekt enforcement, S11 full migration of 40–60 display sites, S12 observability dashboard.

### E07 — Locale-Aware Formatting (~13 pts)

Source: [`epic-prod-07-locale-aware-formatting.md`](./epic-prod-07-locale-aware-formatting.md)

| Story | Pts | Sprint | Why pilot-critical |
|---|---:|---|---|
| E07.S01 | 5 | P1 | `LocaleAwareFormatter` API — needed for fee amounts + dates |
| E07.S02 | 3 | P2 | Tenant currency — King Fahad is SDG; defaulting USD is wrong |
| E07.S03 | 5 | P2 | Refactor existing formatter usages — sweep the ~30 `String.format` callsites |

**Deferred to Phase 3:** S04 Hijri toggle (Sudan uses Gregorian for school year), S05 doc.

### E08 — API Contract & Endpoints (~62 pts)

Source: [`epic-prod-08-api-contract-endpoints.md`](./epic-prod-08-api-contract-endpoints.md)

⚠ **Roughly half of E08 is server-side work in the hogwarts repo, not android-app.** Track each server story as a hogwarts issue with explicit Jun 1, 2026 deadline.

| Story | Pts | Sprint | Side | Why pilot-critical |
|---|---:|---|---|---|
| E08.S01 | 8 | P1 | both | OpenAPI 3.1 spec — single source of truth for the pilot subset |
| E08.S02 | 5 | P1 | mobile | Codegen Retrofit interfaces from OpenAPI |
| E08.S03 | 8 | P1 | server | Auth endpoints (register/reset/OTP/biometric/schools) |
| E08.S04 | 3 | P1 | server | `whoami` |
| E08.S05 | 3 | P1 | server | `/devices/*` FCM tokens |
| E08.S06 | 3 | P2 | server | `/dashboard` |
| E08.S07 | 3 | P2 | server | `/profile` GET + PUT + delete |
| E08.S11 | 8 | P2 | server | Attendance endpoints — **read subset only**: `GET /student/{id}`, `GET /analytics`. Mark endpoints deferred |
| E08.S12 | 5 | P2 | server | Grades endpoints — **read subset**: `GET /student/{id}`, `GET /summary/{id}`, `GET /report-cards/{id}`. Submit deferred |
| E08.S14 | 3 | P2 | server | Timetable endpoints — full read |
| E08.S16 | 5 | P3 | server | Fees endpoints — **read subset**: `GET /fees`, `GET /fees/summary/{id}`. Payment deferred |
| E08.S17 | 5 | P2 | server | Announcements + notifications endpoints — full read + mark-read |
| E08.S20 | 3 | P2 | server | Admission endpoints (admission portion only — events/library/idcard/quiz deferred) |

**Deferred to Phase 2:** S08 dictionary, S15 translate, S18 messaging, S21 MockWebServer contract tests.
**Deferred to Phase 3:** S09–S10 SIS CRUD, S13 exams, S19 LMS, S22 Pact, S20 remaining portions (events/library/idcard/quiz).

### E09 — Real-Time Layer (~6 pts)

Source: [`epic-prod-09-real-time-layer.md`](./epic-prod-09-real-time-layer.md)

| Story | Pts | Sprint | Why pilot-critical |
|---|---:|---|---|
| E09.S06 | 3 | P3 | FCM data + notification dual-channel — pilot uses FCM-only |
| E09.S07 | 3 | P3 | Background message sync via FCM data payload — guardian gets attendance alerts |

**Deferred entirely to Phase 2:** S01–S05, S08, S09 (full Socket.IO server + auth + presence + typing + reconnection + real-time attendance + test harness). One school, small user count — FCM latency is fine.

### E10 — Auth, Onboarding & Profile UI (~17 pts)

Source: [`epic-prod-10-auth-onboarding-profile.md`](./epic-prod-10-auth-onboarding-profile.md)

| Story | Pts | Sprint | Why pilot-critical |
|---|---:|---|---|
| E10.S01 | 5 | P3 | Welcome / login / signup flow polish — edge cases will be hit |
| E10.S05 | 5 | P3 | Profile view + edit — basic identity surface |
| E10.S08 | 5 | P3 | Account deletion — **Google Play 2026 policy gate**. Submit listing draft in P1 week 1 |
| E10.S09 | 2 | P3 | Privacy + ToS in-app viewer — Play Store policy gate |

**Deferred to Phase 2:** S02 OTP SMS auto-fill (covered functionally by E03.S06), S03 forgot-password nav, S04 school selector UI (covered by E03.S08), S06 avatar upload, S07 change password.

### E11 — Dashboard & Navigation (~13 pts)

Source: [`epic-prod-11-dashboard-navigation.md`](./epic-prod-11-dashboard-navigation.md)

| Story | Pts | Sprint | Why pilot-critical |
|---|---:|---|---|
| E11.S01 | 5 | P3 | Wire 6 dashboard TODO tiles — `onClick` lambdas that currently do nothing |
| E11.S03 | 5 | P3 | Role-specific tile filtering — students don't see admin tiles |
| E11.S07 | 3 | P3 | Deep-link audit for cold-start — notifications deep-link to feature screens |

**Deferred to Phase 3:** S02 search, S04 long-press quick actions, S05 bottom nav polish, S06 sidebar drawer, S08 app shortcuts. All polish.

### E19 — Admissions (~23 pts; events/library/idcard/quiz deferred)

Source: [`epic-prod-19-admissions-events-library-idcard-quiz.md`](./epic-prod-19-admissions-events-library-idcard-quiz.md)

| Story | Pts | Sprint | Why pilot-critical |
|---|---:|---|---|
| E19.S01 | 3 | P2 | Admission application list (admin) — sales demo |
| E19.S02 | 8 | P2 | Admission application form (multi-step) — sales demo |
| E19.S03 | 3 | P3 | Admission status check (public, OTP-gated) — applicants can check without account |
| E19.S04 | 3 | P3 | Tour booking (public) — sales-funnel hook |
| E19.S05 | 3 | P3 | Events list + calendar |
| E19.S06 | 3 | P3 | Event detail + RSVP |

**Deferred to Phase 2:** none (admission fully in P1 per user lock).
**Deferred to Phase 3:** S07–S14 library catalog, book detail, borrowings, digital ID card, Apple/Google Wallet passes, quiz hub, timed challenge, tests.

### E25 — Security Hardening (~5 pts)

Source: [`epic-prod-25-security-hardening.md`](./epic-prod-25-security-hardening.md)

| Story | Pts | Sprint | Why pilot-critical |
|---|---:|---|---|
| E25.S01 | 3 | P3 | Real cert pins — staging already configured; pilot uses production cert |
| E25.S08 | 2 | P3 | Sensitive-data redaction in Crashlytics breadcrumbs — tokens must not leak |

**Deferred to Phase 2:** S02 `network_security_config.xml` (done with S01), S03 Play Integrity, S04 RootBeer, S05 server rate-limiting, S07 audit log.
**Deferred to Phase 3:** S06 OWASP audit, S09 debugger disable, S10 DataStore encryption.

### E26 — Observability & Crash Reporting (~11 pts)

Source: [`epic-prod-26-observability-crash-reporting.md`](./epic-prod-26-observability-crash-reporting.md)

| Story | Pts | Sprint | Why pilot-critical |
|---|---:|---|---|
| E26.S01 | 3 | P3 | Verify Crashlytics e2e (test crash) — required to debug pilot crashes |
| E26.S02 | 3 | P3 | Firebase Performance traces — cold-start baseline |
| E26.S03 | 3 | P3 | Business analytics events — captain needs DAU/funnel data |
| E26.S04 | 2 | P3 | User attribution on Crashlytics — identify which pilot user crashed |

**Deferred to Phase 2:** S05 ANR tracking, S09 shake-to-report.
**Deferred to Phase 3:** S06 dashboard, S07 Sentry, S08 logcat capture, S10 custom dashboards.

### E27 — Release Pipeline & Distribution (~12 pts)

Source: [`epic-prod-27-release-pipeline-distribution.md`](./epic-prod-27-release-pipeline-distribution.md)

| Story | Pts | Sprint | Why pilot-critical |
|---|---:|---|---|
| E27.S01 | 5 | P3 | PR build + tests + lint workflow — basic CI |
| E27.S03 | 5 | P3 | Tagged release → Play Internal Testing track |
| E27.S04 | 2 | P3 | Signing key in GitHub Secrets — required to ship signed APK |

**Deferred to Phase 2:** S02 nightly staging to App Distribution, S05 Play Console API.
**Deferred to Phase 3:** S06 Dependabot, S07 Renovate, S08 release notes, S09 build cache, S10 reproducible builds.

---

## Sprint cadence

| Sprint | Weeks | Theme | Key stories | Pts |
|---|---|---|---|---:|
| **P1** | 1–2 | Foundation + tenancy + i18n start + OpenAPI kickoff | E01.S01–S07, E02.S01–S02, E02.S06, E04.S01, E05.S01, E07.S01, E08.S01–S05 (incl. server-side auth + whoami + devices) | ~88 |
| **P2** | 3–5 | Auth UI + read endpoints + RTL/i18n sweep + DB translation foundation + admission UI start | E02.S08–S09, E03.S01/S04/S05/S06/S08, E04.S02/S05/S06, E05.S02/S04/S05, E07.S02/S03, E06.S01–S02, E08.S06/S07/S11/S12/S14/S17 (server), E08.S20 admission portion (server), E19.S01–S02 | ~110 |
| **P3** | 6–7 | Translation finish + dashboard + fees read + FCM + admission finish + Play Internal release | E06.S03/S05/S07/S10, E08.S16, E10.S01/S05/S08/S09, E11.S01/S03/S07, E09.S06–S07, E19.S03–S06, E26.S01–S04, E27.S01/S03/S04, E25.S01/S08 | ~98 |
| | | | **Phase 1 total** | **~296** |

**Velocity:** 88/110/98 vs the existing-plan target of 50–80. Hot. Mitigation: roughly half of P1+P2 stories are server-side hogwarts-repo work that runs in parallel; mobile-side weekly load stays manageable.

---

## Definition of Done

- [ ] All ~70 Phase 1 stories merged via the `IDEA → ISSUE → BRANCH → PR` workflow (`.claude/rules/github-workflow.md`).
- [ ] Conventional commits with `Closes #N`; co-author trailer present.
- [ ] Signed APK uploaded to **Play Internal Testing** track. Crashlytics, Performance, Analytics events visible in Firebase console.
- [ ] King Fahad principals + a sample parent cohort (5–10 accounts) have install + login + Arabic UI screenshot review.
- [ ] `MigrationTest` green; `:app:checkStringParity` green; `detekt` + `spotlessCheck` green on every PR.
- [ ] One Maestro flow recorded: login → dashboard → announcement detail → fee balance → log out.
- [ ] One admission demo recording: multi-step form → submit → status check via OTP link.
- [ ] Captain (kun) + Ali (QA) sign off on the build going to King Fahad.

---

## Risks (see source plan for mitigations)

1. **Server endpoint slippage** (highest impact) — hogwarts-repo work owned by web team. Mitigation: per-endpoint owners; MockWebServer fallback.
2. **Arabic translation audit (E04.S01)** depends on native-speaker review (Crowdin/Lokalise vendor).
3. **Conversion conversation timing** — pitch from a P2-end build (week 5) before P3 fully ships.
4. **Velocity is hot** — bail-out is dropping E04.S01 audit and accepting canonical-seed Arabic.
5. **Account deletion review** can take 2–4 weeks; submit listing draft in P1 week 1.

---

## How to pick up a story

1. Read this manifest; pick the next available story whose dependencies are merged.
2. Open the source epic file (linked above) and read the full AC + file refs.
3. Create the GitHub issue per `.claude/rules/github-workflow.md`:
   ```bash
   gh issue create --repo databayt/android-app \
     --title "<type>: <description> (E0X.S0Y)" \
     --body "<acceptance criteria copied from epic file>" \
     --label "type:<feature|fix|chore>,P0,phase:pilot-v1"
   ```
4. Branch: `feat/E0X-S0Y-{kebab-case}` from `main`.
5. Implement; run `./gradlew :app:lintRelease detekt spotlessCheck`.
6. PR with `Closes #N`.

---

## Phase boundary

When Phase 1 ships, the next manifest is [`phase-2-mena10.md`](./phase-2-mena10.md). Phase 2 picks up the deferred items listed above plus the broader teacher / messaging / payment surfaces.
