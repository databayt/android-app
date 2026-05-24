# Epic V2-03: Internationalization & RTL

**Epic ID:** EPIC-V2-03
**Title:** Internationalization & RTL
**Status:** In Progress (~75%)
**Owner:** BMAD Dev Agent
**Priority:** P1
**Sprint:** 12-14
**Total Points:** 42
**Last Refresh:** 2026-04-25

---

## 1. Overview

The app already ships ~1,700 string resources with full Arabic translations and uses
`AppCompatDelegate.setApplicationLocales` for per-app language switching. This epic
now tracks the remaining hardening work: ICU-skeleton-based formatting, plurals,
bidi safety, an `Accept-Language` interceptor, and CI parity guards. See the
companion plan at `~/.claude/plans/read-latest-offical-docs-lively-naur.md`.

### Business Value
- Arabic-speaking users (primary market) get a fully translated app
- RTL layout feels natural and correct
- Locale-aware dates / numbers / currency match user expectations
- Server-rendered content (emails, push) follows the in-app language

### Success Criteria
- ~1,700 string resources, every EN key has an AR counterpart (parity-tested via Gradle task)
- SF Pro for Latin, SF Arabic for Arabic, auto-selected from `LocalLayoutDirection`
- In-app language switching without manual Activity recreation
- All screens render correctly in RTL with `Icons.AutoMirrored.*`
- Accept-Language header on outgoing API calls
- ICU skeleton patterns (`yMMMd`, `Hm`) instead of hardcoded `MMM d, yyyy`

---

## 2. Stories

### Story V2-03-S01: String Resource Extraction [13 pts]
**Status:** Done

**As a** user,
**I want** all visible text from string resources,
**So that** the app can be fully translated.

**Acceptance Criteria:**
- [x] All 25 feature modules use `stringResource(R.string.xxx)` — no hardcoded English on user-facing screens (Atom Studio debug catalog excepted)
- [x] ~1,700 string resources (vastly exceeds the original 400+ goal)
- [ ] Plurals via `<plurals>` resource type — see V2-03-S08
- [x] Organized by prefix: `auth_`, `dashboard_`, `grades_`, `stream_`, etc.
- [x] Android Lint `HardcodedText` check enabled as error in app/build.gradle.kts

---

### Story V2-03-S02: Arabic Translation [8 pts]
**Status:** Done

**As an** Arabic-speaking user,
**I want** the entire app in Arabic,
**So that** I can use it in my native language.

**Acceptance Criteria:**
- [x] `values-ar/strings.xml` has Arabic for all ~1,700 resources
- [x] Natural Arabic (manually translated)
- [ ] Arabic pluralization rules (one/two/few/many/other) — see V2-03-S08
- [x] Date/time references translated
- [x] Parity enforced via `./gradlew checkStringParity` (added to `app/build.gradle.kts`)

---

### Story V2-03-S03: Custom Fonts [5 pts]
**Status:** Done (different fonts than originally planned)

**As a** user,
**I want** SF Pro (Latin) and SF Arabic fonts,
**So that** text is readable and aesthetically consistent.

**Acceptance Criteria:**
- [x] Real SF Pro family in `core/designsystem/.../res/font/`
- [x] Real SF Arabic family in same location
- [x] `SFProFontFamily` and `SFArabicFontFamily` defined in `typography.kt`
- [x] Theme auto-selects font from `LocalLayoutDirection.current` (no separate `isRtl` flag)

**Technical Notes:**
- Decision: shipped SF Pro / SF Arabic instead of Rubik / Noto Sans Arabic to match the iOS counterpart

---

### Story V2-03-S04: Locale-Aware Formatting [5 pts]
**Status:** Done (core), Follow-up for messaging time fields

**As a** user,
**I want** dates, numbers, and currency formatted by locale,
**So that** data is familiar.

**Acceptance Criteria:**
- [x] `LocaleFormatter` in `core/common/.../utils/` with `formatDate`, `formatDateShort`, `formatDateLong`, `formatTime`, `formatDateTime`, `formatRelativeDate`, `formatNumber`, `formatPercentage`, `formatCurrency`, `unicodeWrap`
- [x] Patterns derived from ICU skeletons via `DateFormat.getBestDateTimePattern(locale, skeleton)` (Hijri-aware where applicable, 12/24h auto)
- [x] Source-of-truth `Locale` from `AppCompatDelegate.getApplicationLocales()` (not `Locale.getDefault`)
- [x] Migrated callsites: attendance, fees (incl. invoice list/detail/balance dashboard), announcements (list + detail), library borrowings, students detail, exams (list/detail/hall ticket), kiosk mode
- [ ] Migrate messaging time displays (message-bubble, message-search, contact-row, date-separator) — they currently pass `Locale.getDefault()` directly which works but is inconsistent
- [x] Hardcoded `$%.2f` currency in guardian fees replaced with `formatCurrency()`

---

### Story V2-03-S05: In-App Language Switcher [5 pts]
**Status:** Done

**As a** user,
**I want** to switch Arabic/English without restarting,
**So that** language changes take effect immediately.

**Acceptance Criteria:**
- [x] `AppCompatDelegate.setApplicationLocales()` in settings VM (works on API 26+)
- [x] AndroidX backport handles activity recreation automatically
- [x] `HogwartsTheme` reads RTL from framework `LocalLayoutDirection.current` — no parallel `isRtl` plumbing
- [x] Persistence via `AppLocalesMetadataHolderService` with `autoStoreLocales=true` (manifest) + redundant DataStore write for in-app picker UI

---

### Story V2-03-S06: RTL Verification & Fixes [3 pts]
**Status:** Mostly Done

**As an** Arabic user,
**I want** the entire UI to mirror correctly in RTL,
**So that** layout feels natural.

**Acceptance Criteria:**
- [x] All directional icons use `Icons.AutoMirrored.*` (verified via grep)
- [x] Modifiers use `start`/`end` not `left`/`right` (only `Canvas.drawText` numeric offsets remain, which don't auto-mirror)
- [ ] RTL screenshot tests for login, dashboard, grades, stream catalog (Roborazzi setup pending — see V2-03-S09)
- [x] Pseudo-locales (`en-XA` text expansion, `ar-XB` forced RTL) enabled on debug builds via `isPseudoLocalesEnabled = true`

---

### Story V2-03-S07: I18n Lint & CI [3 pts]
**Status:** Done

**Acceptance Criteria:**
- [x] `HardcodedText` and `MissingTranslation` as error in `app/build.gradle.kts` `lint { … }` block
- [x] `./gradlew checkStringParity` task added to `app/build.gradle.kts`, wired into `check`
- [x] Task fails build on EN/AR key mismatch and on AR-only orphan keys

---

### Story V2-03-S08: Plurals Adoption [5 pts]
**Status:** Not Started
**Sprint:** TBD

**As an** Arabic user,
**I want** count-dependent text ("1 message" / "2 رسائل") in correct grammatical form,
**So that** counts read naturally instead of using English-style "%d items".

**Acceptance Criteria:**
- [ ] `<plurals>` resources for: notification counts, unread chat counts, student counts in classes, announcement counts, sync conflict counts, "X People" avatar caption
- [ ] EN entries use `one` + `other`; AR entries cover all six (`zero`/`one`/`two`/`few`/`many`/`other`)
- [ ] Replace `getString(…, n)` / `"$n items"` interpolations with `resources.getQuantityString(R.plurals.…, n, formatter.formatNumber(n))`
- [ ] `MissingQuantity` lint warning resolved everywhere
- Scaffold present: `core/common/values{,-ar}/plurals.xml` defines `locale_days_ago` with all six AR categories.

---

### Story V2-03-S09: Backend Locale Forwarding [3 pts]
**Status:** Done

**Acceptance Criteria:**
- [x] `AcceptLanguageInterceptor` in `core/network/.../interceptor/` reads `AppCompatDelegate.getApplicationLocales().toLanguageTags()` and adds the `Accept-Language` header
- [x] Wired into `provideOkHttpClient` between `TenantInterceptor` and `loggingInterceptor`
- [x] Defaults to `en` if no per-app locale set yet

---

### Story V2-03-S10: RTL Screenshot Tests [3 pts]
**Status:** Not Started
**Sprint:** TBD

**Acceptance Criteria:**
- [ ] Roborazzi added to `gradle/libs.versions.toml` and applied to top features (dashboard, attendance, fees, messaging, students)
- [ ] `core/designsystem/.../androidTest/.../rtl-test-helper.kt` extended with `@Parameterized` over `Locale("en")` + `Locale("ar")` and snapshot capture
- [ ] CI: `./gradlew :app:verifyRoborazziDebug`

---

### Story V2-03-S11: LocaleFormatter Unit Tests [2 pts]
**Status:** Not Started
**Sprint:** TBD

**Acceptance Criteria:**
- [ ] Robolectric tests in `core/common/src/test/.../utils/locale-formatter-test.kt`
- [ ] Cover: `formatDate(ar)` produces ICU-best-pattern; `formatNumber(ar)` produces Eastern-Arabic digits; `formatCurrency` SAR; `formatRelativeDate` resource lookups; `unicodeWrap` adds RLM/LRM
