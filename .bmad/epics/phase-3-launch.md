# Phase 3 — Public Launch (v1.0.0)

**Release phase:** 3 of 3
**Sprints:** P9, P10, P11, P12, P13, P14, P15, P16 (~16 weeks)
**Target points:** ~408
**Target date:** 2026-12-20
**Status:** Not Started (begins after Phase 2 DoD)
**Owner:** Captain (kun) + Samia (R&D)
**Sibling phases:** [phase-1-pilot-v1.md](./phase-1-pilot-v1.md) · [phase-2-mena10.md](./phase-2-mena10.md)
**Source plan:** [`~/.claude/plans/take-web-app-as-precious-turtle.md`](../../../.claude/plans/take-web-app-as-precious-turtle.md)

---

## Intent

Public Play Store launch. v1.0.0 staged rollout to 100%. Feature breadth complete (LMS, library, ID card with Wallet passes, full settings + accessibility + performance audits), 80% unit test coverage, ≥ 99.5% crash-free user rate, WCAG AA accessibility audit clean.

Phase 3 is **everything not in Phase 1 or Phase 2**, plus the explicit Play Store launch gate.

---

## Story selection by epic

### E11 — Dashboard polish

[Source](./epic-prod-11-dashboard-navigation.md). S02 search, S04 long-press quick actions, S05 bottom nav polish, S06 sidebar drawer, S08 app shortcuts. — needed for public, not pilot.

### E15 — Timetable & Curriculum (full)

[Source](./epic-prod-15-timetable-curriculum.md). S01–S08 — lesson plans + curriculum map + home widget. Public expects this completeness.

### E17 — LMS Stream / Lessons / Courses (full)

[Source](./epic-prod-17-lms-stream-lessons-courses.md). S01–S11 — Media3 video player, offline downloads, Stripe-gated courses, certificates. Major surface area; deferred entirely because King Fahad isn't using LMS.

### E18 — Communications polish

[Source](./epic-prod-18-communications.md). S09 search, S12 voice messages, S13 link previews, S14 wallpaper SVG. Power-user polish.

### E19 — Library + ID Card + Wallet + Quiz

[Source](./epic-prod-19-admissions-events-library-idcard-quiz.md). S07–S14 — library catalog + book detail + borrowings + digital ID card + Apple Wallet + Google Wallet passes + quiz hub + timed challenge + tests. Entire wallet pass story is launch-marketing-worthy but not pilot-critical.

### E20 — Role-Specific Portals (remaining)

[Source](./epic-prod-20-role-specific-portals.md). S03–S14 — meetings, consent, trips, prefs, teacher portals, admin portals. Secondary role surfaces.

### E21 — Settings & Personalization (full)

[Source](./epic-prod-21-settings-personalization.md). S01–S08 — wallpaper, notification channels per-type, cache management, diagnostics.

### E22 — Test Infrastructure (finish)

[Source](./epic-prod-22-test-infrastructure-coverage.md). S04 Compose UI tests for top 30 screens, S06 Maestro E2E for top 8 flows, S07–S09 (contract + snapshot + perf regression). **Target: 80% coverage gate.**

### E23 — Performance & Resource Optimization (full)

[Source](./epic-prod-23-performance-optimization.md). S01–S09 — Macrobenchmark, baseline profiles, dynamic features, leak audit.

### E24 — Accessibility (full)

[Source](./epic-prod-24-accessibility.md). S01–S07 — TalkBack audit, WCAG AA, reduced motion, keyboard nav.

### E25 — Security (remaining)

[Source](./epic-prod-25-security-hardening.md). S02 `network_security_config.xml`, S06 full OWASP audit, S09 debugger disable, S10 DataStore encryption.

### E26 — Observability (remaining)

[Source](./epic-prod-26-observability-crash-reporting.md). S06 dashboard, S07 Sentry, S08 logcat, S10 custom dashboards.

### E27 — Release Pipeline (remaining)

[Source](./epic-prod-27-release-pipeline-distribution.md). S06 Dependabot, S07 Renovate, S08 release notes automation, S09 build cache, S10 reproducible builds.

### E28 — Play Store Launch (full)

[Source](./epic-prod-28-play-store-launch.md). S01–S08 — listings, screenshots, video, Privacy Policy, Data Safety form, soft-launch, staged rollout. **This is the v1.0.0 ship gate.**

---

## Definition of Done

- [ ] v1.0.0 tagged and in Play Store **production** track.
- [ ] Staged rollout reaches 100% with no rollback.
- [ ] Crash-free user rate ≥ 99.5%.
- [ ] Unit test coverage ≥ 80% (Kover report).
- [ ] TalkBack walkthrough of every Phase-1 + Phase-2 surface passes.
- [ ] WCAG AA audit clean (manual + axe-android).
- [ ] All 28 production epics either shipped, archived as obsolete, or explicitly carried to v1.1+ roadmap.

---

## Post-launch

After Phase 3 ships, the production roadmap (`epic-prod-01..28`) is complete. The next planning artifact is the v1.1+ roadmap, sourced from:

- King Fahad + MENA-10 cumulative pilot feedback.
- Play Store user reviews + ANR/crash signals.
- Sentry + Crashlytics + Firebase Performance dashboards.
- A formal Phase 4 captain's review with Samia (R&D).
