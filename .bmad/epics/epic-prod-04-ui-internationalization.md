# Epic E04: UI Internationalization

**Epic ID:** EPIC-PROD-04
**Title:** UI Internationalization
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0
**Sprint:** 3-4
**Total Points:** 34

---

## 1. Overview

Every UI string in every Composable, dialog, notification, and snackbar is keyed in `strings.xml` with parity-checked AR/EN translations. Server-driven labels (route-specific `dictionary.json` per hogwarts pattern) are loaded on demand and cached. Locale switch takes effect without app restart.

### Business Value
- Native-quality Arabic experience (the platform default).
- New features can't ship with hardcoded English strings (CI-enforced).
- Locale switch is one tap, no restart.

### Success Criteria
- [ ] `:app:checkStringParity` green (already after E01.S06).
- [ ] Detekt `NoHardcodedComposeText` green across all 25 feature modules.
- [ ] New feature module without `values-ar/strings.xml` fails CI.
- [ ] Per-app locale switch (en ↔ ar) takes effect without app restart.
- [ ] Native Arabic speaker rates ≥95% strings as "natural" (vs literal/awkward) in audit.

---

## 2. Stories

### Story E04.S01: Audit existing 1,820 EN / 1,819 AR strings for translation quality [8 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 3

**As a** native Arabic-speaking user, **I want** strings that read naturally, **So that** I trust the app for my school's daily work.

**Acceptance Criteria:**
- [ ] Native-Arabic-speaker review pass (manual or contracted via crowdin / Lokalise).
- [ ] Each string rated `natural` / `acceptable` / `awkward` / `incorrect`.
- [ ] ≥95% rated `natural` or `acceptable`.
- [ ] All `incorrect` rewritten before merge.
- [ ] All `awkward` flagged for follow-up issues, not blocking merge.
- [ ] Output: `docs/i18n/translation-audit-2026Q2.md` with sampled strings + ratings.

**Files:** all `*/src/main/res/values-ar/strings.xml`, new `docs/i18n/translation-audit-2026Q2.md`.

---

### Story E04.S02: Fix all `Text("literal")` violations after Detekt rule lands [8 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 3

**As a** non-English user, **I want** every UI string available in my language, **So that** I'm not surprised by random English text.

**Acceptance Criteria:**
- [ ] After E01.S08 lands the Detekt rule, fix every existing violation across all modules.
- [ ] Each violation: move literal to `strings.xml`, replace with `stringResource(R.string.X)`.
- [ ] Estimated ~80 violations (concentrated in `core/designsystem/atom/*` and `feature/messaging/*`).
- [ ] CI green on `detekt` for `NoHardcodedComposeText` rule.
- [ ] Sweep checks `Toast`, `Snackbar`, dialog titles, button texts, accessibility content descriptions.

**Files:** every `feature/*/src/main/...`, every `core/*/src/main/...` with Composable code.

**Refs:** Detekt rule from E01.S08.

---

### Story E04.S03: Server-driven dictionary loader [5 pts]
**Status:** Not Started · **Sprint:** 4

**As a** mobile user, **I want** dynamic labels (subject names, term names) localized server-side, **So that** the labels match the web's wording exactly.

**Acceptance Criteria:**
- [ ] New module `core/i18n` with `DictionaryRepository`.
- [ ] `dictionaryRepository.get(module: String, lang: String): Flow<Map<String, String>>`.
- [ ] Fetches `GET /api/mobile/dictionary?lang=ar&module=fees` on demand.
- [ ] Caches to DataStore Proto file `tenant-dictionary-{tenantId}-{locale}-{module}.pb`.
- [ ] Refresh policy: on app start if cache age >24h, OR on tenant switch, OR on explicit `force=true`.
- [ ] Compose helper `serverString(key)` reads from `LocalDictionary` composition local.

**Files:** new `core/i18n/build.gradle.kts`, `core/i18n/.../dictionary-repository.kt`, `core/i18n/.../dictionary-impl.kt`, `core/i18n/.../local-dictionary.kt`, new `core/network/api/dictionary-api.kt`.

**Backend dependency:** E08.S08.

**Refs:** hogwarts split-by-domain dictionary loaders at `src/components/internationalization/dictionaries.ts`.

---

### Story E04.S04: Per-locale resource module isolation [3 pts]
**Status:** Not Started · **Sprint:** 4

**As a** team, **I want** each Gradle module to own its own translations, **So that** module owners can update strings without touching app-wide files.

**Acceptance Criteria:**
- [ ] Each Gradle feature module continues shipping its own `values{,-ar}/strings.xml` (already does).
- [ ] New Gradle task `:checkPerModuleStringPresence` fails if a module has `values/strings.xml` but no `values-ar/strings.xml` (or vice versa).
- [ ] Wired into `:check`.
- [ ] Verifies count parity per module too (already done by `:app:checkStringParity` for app module — extend to all modules).

**Files:** root `build.gradle.kts` (task definition + per-module wiring).

---

### Story E04.S05: Locale switch without app restart [5 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 4

**As a** user, **I want** to switch language and see immediate effect, **So that** I don't have to kill the app.

**Acceptance Criteria:**
- [ ] `feature/settings/.../settings-screen.kt` language picker calls `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))`.
- [ ] Active screen recomposes with new locale.
- [ ] Nav state preserved (no nav-stack reset).
- [ ] Existing `LocaleListCompat` integration via `AppLocalesMetadataHolderService` (already configured in manifest) honored.
- [ ] Manual smoke test on Android 11, 13, 14.

**Files:** `feature/settings/.../settings-screen.kt`, `feature/settings/.../settings-view-model.kt`.

---

### Story E04.S06: Plurals audit and fix for Arabic six-form rule [3 pts] · **Phase: Pilot v1**
**Status:** Not Started · **Sprint:** 4

**As a** native Arabic speaker, **I want** counts rendered with grammatical plurals, **So that** "1 student" / "2 students" / "11 students" all read correctly in Arabic.

**Acceptance Criteria:**
- [ ] Every quantity-string uses `<plurals>` tags with all 6 Arabic forms: `zero`, `one`, `two`, `few`, `many`, `other`.
- [ ] ~5-10 known violations expected; all fixed.
- [ ] New `<plurals>` strings added where flat strings were used (e.g. `R.plurals.students_count`).
- [ ] `Resources.getQuantityString(R.plurals.X, count, count)` callsites verified.

**Refs:** ICU plural rules at https://www.unicode.org/cldr/charts/latest/supplemental/language_plural_rules.html.

---

### Story E04.S07: Format-args safety for placeholders [2 pts]
**Status:** Not Started · **Sprint:** 4

**As a** developer, **I want** missing `String.format` args to fail at compile-time, **So that** I don't ship runtime crashes.

**Acceptance Criteria:**
- [ ] Detekt rule `RequireFormatArgsForPlaceholders` flags `String.format(stringResource(...))` and `stringResource(R.string.X, arg1)` when placeholder count doesn't match arg count.
- [ ] Pure compile-time check via lint metadata.
- [ ] Passes if `R.string.X` has no placeholders (no `%s` / `%d` / `%1$s`).

**Files:** `config/detekt/custom-rules/.../RequireFormatArgsForPlaceholders.kt`.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E01.S08 Detekt `NoHardcodedComposeText` rule | Required for E04.S02 |
| E01.S06 string parity drift fix | Required (already done) |
| Backend `GET /api/mobile/dictionary` | Required for E04.S03 — see E08.S08 |
| AppCompat per-app locale (`AppLocalesMetadataHolderService`) | Already configured |

---

## 4. Risks

| Risk | Mitigation |
|------|------------|
| Native-speaker review delays merge | E04.S01 runs in parallel with feature work; only blocks final v1.0.0 cut |
| Server dictionary fetch fails offline | Cached fallback; English defaults if cache empty |
| Detekt rule false positives on `Text("$variable")` | Rule explicitly allows string templates with no literal portion |
| Locale switch leaves stale Composables | Mitigated by recompose triggered by `LocalConfiguration.current` change |

---

## 5. Out of Scope

- Multi-locale at runtime (e.g. EN UI + AR content) → not a feature; one locale at a time per session.
- More than en/ar — defer until business need arises.

---

## 6. Definition of Done

- [ ] All 7 stories merged.
- [ ] Translation audit complete with ≥95% natural/acceptable rating.
- [ ] Detekt `NoHardcodedComposeText` green across all 25 feature modules.
- [ ] Manual smoke: switch language en→ar from Settings → dashboard, chat, fees screen all reflect correct strings + layout direction + fonts.
- [ ] Server dictionary endpoint returns expected shape; client caches and renders.
- [ ] Captain signoff documented.
