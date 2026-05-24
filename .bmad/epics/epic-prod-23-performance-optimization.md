# Epic E23: Performance & Resource Optimization

**Epic ID:** EPIC-PROD-23
**Title:** Performance & Resource Optimization
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P1
**Sprint:** 14-15
**Total Points:** 34

---

## 1. Overview

Cold-start <2s on Pixel 8, scroll jank <1% on Pixel 6a, app size ≤25MB (currently 8.2MB so headroom), memory <150MB on dashboard.

### Success Criteria
- [ ] Macrobenchmark cold-start ≤2s on Pixel 8.
- [ ] Frozen-frame rate <0.5% on critical screens.
- [ ] Initial APK size ≤20MB (with dynamic features deferring quizgame, idcard, library).
- [ ] No memory leaks reported by LeakCanary on 8-hour usage.

---

## 2. Stories

| ID | Title | Pts | Sprint | Notes |
|---|---|---:|---|---|
| E23.S01 | Macrobenchmark setup (`:benchmark` module) | 5 | 14 | |
| E23.S02 | Baseline Profiles for cold-start | 5 | 14 | Generate via `:benchmark`; ship in release |
| E23.S03 | R8 mapping uploads to Crashlytics | 3 | 14 | |
| E23.S04 | Image loading audit (Coil) — placeholder, error, size constraints, cache size | 3 | 14 | |
| E23.S05 | Eliminate request waterfalls — `Promise.all`-style with `coroutineScope { async/async }` | 5 | 15 | Top 10 violations |
| E23.S06 | Dynamic Feature modules — split `quizgame`, `idcard`, `library` | 5 | 15 | Initial APK ≤20MB |
| E23.S07 | `@Stable` + `@Immutable` audit on Composable params | 3 | 15 | |
| E23.S08 | Memory leak audit (LeakCanary debug-only) | 3 | 15 | |
| E23.S09 | StrictMode policy violations fixed | 2 | 15 | |

### Detailed AC: E23.S02 — Baseline Profiles
- [ ] `:benchmark` module generates baseline.
- [ ] Profiles output to `app/src/main/baseline-prof.txt`.
- [ ] Macrobenchmark verifies ≥20% cold-start improvement vs no profile.
- [ ] CI regenerates on `main` merges (cron-scheduled, not per-PR).

### Detailed AC: E23.S03 — R8 mapping
- [ ] `firebaseCrashlytics { mappingFileUploadEnabled = true }` in `app/build.gradle.kts`.
- [ ] Verified by triggering a release-mode crash + confirming demangled stack in console within 5 min.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E26 Crashlytics plugin | Required for E23.S03 |
| `androidx.benchmark` library | New dep |
| Existing R8 / ProGuard config | Available (185-line `proguard-rules.pro`) |

---

## 4. DoD

- [ ] All 9 stories merged.
- [ ] Cold-start benchmark passes.
- [ ] Mapping verified with test crash.
- [ ] APK size ≤20MB.
- [ ] Captain signoff.
