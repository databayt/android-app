# Epic E24: Accessibility

**Epic ID:** EPIC-PROD-24
**Title:** Accessibility
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P1
**Sprint:** 14-15
**Total Points:** 26

---

## 1. Overview

WCAG 2.1 AA-equivalent for the platforms we ship. Manual TalkBack audit on top 10 screens. All interactive elements ≥48dp. Color contrast ≥4.5:1.

### Success Criteria
- [ ] TalkBack flow for top 10 screens — no blockers.
- [ ] All touch targets ≥48dp.
- [ ] All text/background pairs ≥4.5:1 contrast.
- [ ] Screens render correctly at 1.3x and 2.0x font scale.
- [ ] Reduced-motion preference disables non-essential animations.

---

## 2. Stories

| ID | Title | Pts | Sprint |
|---|---|---:|---|
| E24.S01 | Content descriptions on every interactive Composable | 5 | 14 |
| E24.S02 | TalkBack flow audit on top 10 screens | 8 | 14-15 |
| E24.S03 | Touch target ≥48dp audit + fix | 3 | 15 |
| E24.S04 | Color contrast audit (WCAG AA) | 3 | 15 |
| E24.S05 | Text scaling (1.3x, 2.0x) audit | 3 | 15 |
| E24.S06 | Reduced-motion preference | 2 | 15 |
| E24.S07 | Keyboard navigation (external keyboard / Chromebook) | 2 | 15 |

### Detailed AC: E24.S02 — TalkBack
- [ ] Manual audit on: dashboard, login, attendance mark, grade entry, fees pay, chat, profile, settings, exam taking, library catalog.
- [ ] Focus order correct.
- [ ] Group titles announced.
- [ ] Error states announced via `Modifier.semantics { liveRegion = LiveRegionMode.Polite }`.
- [ ] Recorded video evidence of each screen's flow.

### Detailed AC: E24.S06 — Reduced motion
- [ ] `accessibilityManager.isEnabledFor(AccessibilityServiceInfo.FLAG_REDUCE_MOTION)` (or equivalent setting check).
- [ ] When enabled: disable shrink-on-scroll, fade transitions, parallax. Keep functional animations (ripple).

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| Existing 4 `core/designsystem/.../androidTest/accessibility-test.kt` tests | Available skeleton |
| `androidx.compose.ui:ui-test-junit4` | Available |

---

## 4. DoD

- [ ] All 7 stories merged.
- [ ] TalkBack audit videos archived.
- [ ] Contrast report attached to closing PR.
- [ ] Captain signoff.
