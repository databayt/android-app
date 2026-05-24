# Epic E07: Locale-Aware Formatting

**Epic ID:** EPIC-PROD-07
**Title:** Locale-Aware Formatting
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P1
**Sprint:** 4
**Total Points:** 18

---

## 1. Overview

Numbers, dates, currency, file sizes, and durations render correctly in both locales. Arabic uses Eastern Arabic numerals (٠١٢٣٤٥٦٧٨٩); English uses Western digits. Currency follows tenant settings (`school.currency` from server).

### Business Value

- Native-feeling numbers and dates in Arabic.
- Currency follows the tenant's actual currency, not hardcoded USD/SAR.
- File sizes / durations have proper Arabic units.

### Success Criteria

- [ ] `formatCurrency(1234.56, locale=ar, tenantCurrency=SAR)` → `١٬٢٣٤٫٥٦ ر.س`.
- [ ] `formatDate(now, locale=en)` → `04/26/2026`; `formatDate(now, locale=ar)` → `٢٦/٠٤/٢٠٢٦`.
- [ ] File-size labels use Arabic units (بايت / كيلوبايت / ميجابايت / جيجابايت) when locale=ar.
- [ ] Hijri toggle scaffolded (off by default) for future Saudi pilot use.

---

## 2. Stories

### Story E07.S01: `LocaleAwareFormatter` API [5 pts]
**Status:** Not Started · **Sprint:** 4

**As a** UI layer, **I want** one set of formatters for all locale-aware rendering, **So that** every screen consistently follows locale rules.

**Acceptance Criteria:**
- [ ] New module `core/i18n/format` (extension of `core/i18n` from E04.S03).
- [ ] Formatters:
  - `LocaleAwareNumberFormatter(locale).format(value: Number): String`
  - `LocaleAwareDateFormatter(locale, calendarSystem: CalendarSystem = Gregorian).format(date: LocalDate, style: FormatStyle): String`
  - `LocaleAwareCurrencyFormatter(locale, tenantCurrency: String).format(value: BigDecimal): String`
  - `LocaleAwareDurationFormatter(locale).format(duration: Duration): String`
  - `LocaleAwareFileSizeFormatter(locale).format(bytes: Long): String`
  - `LocaleAwareRelativeTimeFormatter(locale).format(instant: Instant, now: Instant = Instant.now()): String`
- [ ] Built on `java.text.NumberFormat`, `java.time.format.DateTimeFormatter`, `androidx.compose.ui.text.intl.Locale`.
- [ ] Compose helper `rememberLocaleFormatters(): LocaleFormatters` returns all 6 formatters scoped to current `Locale`.

**Files:** `core/i18n/format/{locale-aware-number-formatter,locale-aware-date-formatter,locale-aware-currency-formatter,locale-aware-duration-formatter,locale-aware-file-size-formatter,locale-aware-relative-time-formatter}.kt`, `core/i18n/format/locale-formatters.kt`.

**Refs:** hogwarts `src/lib/i18n-format.ts` (475 lines).

---

### Story E07.S02: Tenant-currency injection [3 pts]
**Status:** Not Started · **Sprint:** 4

**As a** parent paying fees in SAR, **I want** prices displayed in SAR, **So that** I'm not confused by USD or EUR.

**Acceptance Criteria:**
- [ ] `LocaleAwareCurrencyFormatter` reads `tenantContext.activeSchool.currency` (defaults to `"USD"` per hogwarts default).
- [ ] Currency code (e.g. `"SAR"`, `"USD"`, `"EGP"`, `"AED"`) follows ISO 4217.
- [ ] All ~30 existing currency-display callsites in `feature/fees/...` updated.
- [ ] Settings screen shows current currency next to language picker.

**Files:** `core/i18n/format/locale-aware-currency-formatter.kt`, `feature/fees/.../*-screen.kt`, `feature/settings/.../settings-screen.kt`.

**Refs:** hogwarts `school.prisma:40` `currency` default `"USD"`.

---

### Story E07.S03: Refactor existing formatter usages [5 pts]
**Status:** Not Started · **Sprint:** 4

**As a** developer, **I want** one formatter API across the app, **So that** I don't have 3 different number-formatting patterns.

**Acceptance Criteria:**
- [ ] Existing `core/common/utils/locale-formatter.kt` refactored to delegate to E07.S01 implementations.
- [ ] All ~30 callsites updated to use new API.
- [ ] Existing 1 unit test (`locale-formatter-test.kt`) extended with 20 new test fixtures (covering numbers, dates, currency, file size, duration, EN + AR).
- [ ] No raw `String.format("%.2f", value)` left in feature modules (Detekt rule `NoRawFormatForNumbers` flags any).

**Files:** `core/common/utils/locale-formatter.kt`, every callsite, `core/common/src/test/.../locale-formatter-test.kt`, new Detekt rule.

---

### Story E07.S04: Hijri toggle (deferred, scaffolded) [3 pts]
**Status:** Not Started · **Sprint:** 4

**As a** Saudi pilot tenant, **I might want** dates rendered in Hijri (lunar Islamic) calendar, **So that** the app matches our administrative calendar.

**Acceptance Criteria:**
- [ ] `formatDate(date, locale, calendarSystem = CalendarSystem.Gregorian)` accepts `CalendarSystem.Hijri`.
- [ ] Toggle behind tenant setting `useHijriCalendar` (defaults `false`).
- [ ] Implementation uses `java.time.chrono.HijrahChronology` (umm-al-qura variant for Saudi alignment).
- [ ] Admin can toggle via tenant settings (E21).
- [ ] Surfaces only on date displays — not internal storage.

**Why scaffolded, not shipped:** hogwarts doesn't ship Hijri yet; we want the toggle in place for pilot Saudi schools, but it's not on the critical path for v1.0.0.

**Files:** `core/i18n/format/locale-aware-date-formatter.kt`, `core/data/.../tenant/tenant-settings.kt` (add `useHijriCalendar`).

---

### Story E07.S05: Documentation of formatting conventions [2 pts]
**Status:** Not Started · **Sprint:** 4

**As a** new contributor, **I want** one place to learn the formatting rules, **So that** I don't reinvent locale handling.

**Acceptance Criteria:**
- [ ] New doc `docs/conventions/i18n-formatting.md`.
- [ ] Documents all 6 formatters with examples for both locales.
- [ ] Includes the "do not use raw `String.format`" rule.
- [ ] Includes Hijri toggle status (scaffolded, off by default).
- [ ] Linked from main `README.md` and `docs/architecture.md`.

**Files:** `docs/conventions/i18n-formatting.md` (new), `README.md`, `docs/architecture.md`.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E02.S06 `TenantContext.activeSchool` exposed | Required for E07.S02 |
| Existing `core/common/utils/locale-formatter.kt` | Refactor target |
| `org.threeten.extra` for HijrahChronology (if not in JDK) | Add to `libs.versions.toml` |

---

## 4. Risks

| Risk | Mitigation |
|------|------------|
| `java.text.NumberFormat` for Arabic locale doesn't always emit Eastern digits | Verified via fixture; force via `setNumberingSystem("arab")` if needed |
| Hijri date conversion edge cases (1-day drift) | Use Umm-al-Qura calendar (Saudi-official); test against known reference dates |
| Currency formatting for unfamiliar codes (EGP, AED) | Use `Currency.getInstance(code)` with explicit symbol fallback |

---

## 5. Out of Scope

- Custom calendar systems beyond Gregorian + Hijri (Buddhist, Hebrew, etc.) → defer.
- Spelled-out numbers (e.g. `one thousand two hundred`) → defer.

---

## 6. Definition of Done

- [ ] All 5 stories merged.
- [ ] 20+ formatter test fixtures pass for both locales.
- [ ] Manual smoke: change app locale to Arabic → all numbers, dates, currencies render in Arabic style.
- [ ] Manual smoke: change tenant currency in admin settings → fees screen reflects new currency immediately.
- [ ] Hijri toggle test: enable → date displays in Hijri; disable → reverts to Gregorian.
- [ ] Captain signoff documented.
