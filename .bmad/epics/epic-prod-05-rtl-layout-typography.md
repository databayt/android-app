# Epic E05: RTL Layout & Typography

**Epic ID:** EPIC-PROD-05
**Title:** RTL Layout & Typography
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0
**Sprint:** 3-4
**Total Points:** 29

---

## 1. Overview

Pixel-perfect mirror layouts in Arabic. Every padding, alignment, icon, and animation respects layout direction. Fonts swap correctly. Visual regressions caught by snapshot tests.

### Business Value
- Native-quality Arabic UX (per the platform's primary locale).
- Visual regressions can't ship — snapshot tests fail PRs.
- Directional icons (back arrows, chevrons) feel correct in both directions.

### Success Criteria
- [ ] Detekt `NoLeftRightPadding` green.
- [ ] Snapshot tests for top 30 screens pass at both `LayoutDirection.Ltr` and `LayoutDirection.Rtl`.
- [ ] All directional icons flip correctly in RTL.
- [ ] Numbers preserve direction within bidi boundaries (no mirrored phone numbers).
- [ ] Manual visual audit on Pixel 6 + Galaxy S22 in Arabic locale — no obvious bugs.

---

## 2. Stories

### Story E05.S01: Audit and fix 100% of `Modifier.padding(start|end)` correctness [8 pts]
**Status:** Not Started · **Sprint:** 3

**As a** developer, **I want** zero `left`/`right` padding violations, **So that** RTL just works.

**Acceptance Criteria:**
- [ ] All 533 feature Kotlin files surveyed.
- [ ] Existing `start`/`end` already widely used (Compose default); verify no `left`/`right` regressions.
- [ ] Manual fix for any violations (~5-10 expected, mostly in older modules).
- [ ] Detekt rule `NoLeftRightPadding` (from E01.S09) graduates to error.
- [ ] Net: zero violations.

**Files:** all `feature/*/src/main/...`, all `core/*/src/main/...`.

---

### Story E05.S02: Auto-mirroring icons via `autoMirrored = true` [3 pts]
**Status:** Not Started · **Sprint:** 3

**As a** Arabic user, **I want** the back arrow to point right (toward where I came from), **So that** navigation icons feel native.

**Acceptance Criteria:**
- [ ] Every directional icon in `core/designsystem/atom/...` and feature drawables uses `Icons.AutoMirrored.Filled.X` (e.g. `ArrowBack`, `KeyboardArrowRight`, `Send`, `ChevronRight`).
- [ ] Vector drawables that imply direction (sliders, lists) set `android:autoMirrored="true"`.
- [ ] Audit + fix every icon import.

**Files:** every `feature/*/src/main/.../*-screen.kt`, every drawable XML.

**Refs:** Material 3 auto-mirrored icon set; Compose Material Icons `Icons.AutoMirrored.*`.

---

### Story E05.S03: RTL snapshot tests for top 30 screens [13 pts]
**Status:** Not Started · **Sprint:** 4

**As a** code reviewer, **I want** automated visual regression for both directions, **So that** RTL bugs don't reach prod.

**Acceptance Criteria:**
- [ ] New `:test-snapshot` Gradle module using **Roborazzi** (fast on JVM, no emulator needed).
- [ ] Each of the top 30 screens has 2 snapshots: `<screen>_ltr.png`, `<screen>_rtl.png`.
- [ ] Tests fail on visual diff >0.1%.
- [ ] CI runs `:test-snapshot:verifyRoborazzi` on PRs.
- [ ] Snapshots committed under `test-snapshot/src/test/snapshots/...`.
- [ ] Top 30: dashboard (per role), login, signup, attendance list, attendance mark, grades list, grade detail, exam list, exam taking, fees list, invoice detail, payment, conversations list, chat, profile, settings, students list, student detail, timetable, course catalog, course detail, lesson player, announcement list, announcement detail, notifications list, library catalog, book detail, events list, event detail, ID card.

**Files:** new `test-snapshot/build.gradle.kts`, `test-snapshot/src/test/kotlin/.../{module}-snapshot-test.kt` × 30, snapshots committed.

**Refs:** existing `core/designsystem/.../androidTest/rtl-test-helper.kt` extension functions for `setContentRtl` / `setContentLtr`.

---

### Story E05.S04: Font-swap correctness audit [3 pts]
**Status:** Not Started · **Sprint:** 4

**As a** Arabic user, **I want** Arabic text rendered with the proper Arabic font, **So that** ligatures and diacritics look right.

**Acceptance Criteria:**
- [ ] `HogwartsTheme` correctly swaps to `SFArabicFontFamily` when `LayoutDirection.Rtl`.
- [ ] Verified by new `androidTest` `FontSwapTest` at `core/designsystem/src/androidTest/.../FontSwapTest.kt`.
- [ ] Test sets RTL → renders sample text → measures rendered glyph metrics → asserts SF Arabic family used.
- [ ] Same for Noto Sans Arabic fallback in feature module headers.

**Files:** `core/designsystem/.../theme/theme.kt` (verify), `core/designsystem/.../theme/typography.kt` (verify), new `core/designsystem/src/androidTest/.../FontSwapTest.kt`.

---

### Story E05.S05: Bidi-safe number rendering [2 pts]
**Status:** Not Started · **Sprint:** 4

**As a** Arabic user reading a phone number or invoice number, **I want** the digits in their original order (not mirrored), **So that** I can read and dial correctly.

**Acceptance Criteria:**
- [ ] New Composable `BidiText(text)` wraps `Text` and applies `TextDirection.ContentOrLtr` for numeric content.
- [ ] Top 5 violations fixed (phone display, invoice numbers, OTP code display, currency amounts in mixed text).
- [ ] Compose preview demonstrates correct rendering at both directions.

**Files:** new `core/designsystem/atom/bidi-text.kt`, ~5 callsite fixes.

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E01.S09 Detekt `NoLeftRightPadding` rule | Required for E05.S01 |
| Existing `core/designsystem/.../androidTest/rtl-test-helper.kt` | Available |
| Existing dual-font setup (SF Pro + SF Arabic + Rubik + Noto Sans Arabic) | Available |
| Roborazzi 1.x (JVM-based, no emulator) | New dependency in `libs.versions.toml` |

---

## 4. Risks

| Risk | Mitigation |
|------|------------|
| Snapshot tests flake on font rendering differences across machines | Pin JVM version + font hinting in CI; check in baseline from a single canonical machine |
| `autoMirrored` not respected in older Compose versions | Verified to work since Compose 1.6; we're at Compose BOM 2025.01.01 |
| Bidi-text fix breaks pure-Arabic text | `ContentOrLtr` only flips for content-detected LTR; pure Arabic stays RTL |

---

## 5. Out of Scope

- Vertical text / right-to-bottom scripts (Mongolian, etc.) → out of scope.
- Emoji / sticker direction handling → defer.
- Rich text editor RTL handling → defer to E18 messaging.

---

## 6. Definition of Done

- [ ] All 5 stories merged.
- [ ] Detekt `NoLeftRightPadding` green.
- [ ] 60+ snapshots committed (30 screens × 2 directions).
- [ ] Manual RTL audit on Pixel 6 + Galaxy S22 — no obvious mirroring bugs.
- [ ] Captain signoff documented.
