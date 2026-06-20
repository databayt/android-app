# Hogwarts Android — Production Roadmap

This directory holds the **active** production roadmap for `databayt/android-app`. Two coordinate axes describe each story: a **build phase** (dependency order) and a **release phase** (ship order).

## Two-axis model

| Axis | Phases | What it tells you |
|---|---|---|
| **Build phase** (existing) | A Foundation · B i18n · C Backend · D Features · E Quality · F Production | Which work must complete before which other work |
| **Release phase** (new overlay) | 1 Pilot v1 · 2 MENA-10 · 3 Public Launch | Which customer milestone the work serves |

A story like `E13.S04` (mark attendance bulk) lives in **build phase D** (it depends on backend endpoints from C) and **release phase 2** (it ships to support the 10 pilot schools, not the first one). The two phases are orthogonal — read each story's header for both.

## Start here

| File | Purpose |
|---|---|
| [`epic-prod-overview.md`](./epic-prod-overview.md) | Master roadmap. 28 epics, 1115 points, 16 sprints, both axes summarised. |
| [`phase-1-pilot-v1.md`](./phase-1-pilot-v1.md) | **The next ~7 weeks.** King Fahad Schools (Sudan) pilot conversion target. |
| [`phase-2-mena10.md`](./phase-2-mena10.md) | MENA-10 program — scale from 1 to 10 schools. |
| [`phase-3-launch.md`](./phase-3-launch.md) | Public Play Store launch (v1.0.0). |

## Individual epic files

`epic-prod-01-*.md` through `epic-prod-28-*.md` — one file per epic, ~10 stories each, with acceptance criteria + file refs + estimates. Each story's header marks both its build phase and its release phase.

## Status

`../bmm-workflow-status.yaml` is the canonical source of "what's done / in progress / not started" per story and per epic. Update it as work lands.

## Archive

`./archive/` holds:

- `epic-01..17-*.md` — V1 epics (the completed UI shell; 431 points; superseded).
- `epic-v2-01-backend-api.md` — V2 backend API draft (folded into `epic-prod-08`).
- `docs-epics/epic-1..7-*.md` — Earlier `docs/epics/` mirrors (stale).

These remain readable for historical reference; do not pick up stories from them.

## How to use this directory

- **Picking next work:** read the active phase manifest (`phase-1-pilot-v1.md` today). Find the next story whose dependencies are merged. Follow [`.claude/rules/github-workflow.md`](../../.claude/rules/github-workflow.md).
- **Adding a story:** add it to the relevant `epic-prod-NN-*.md` file. Tag it with `**Phase: <Pilot v1 | MENA-10 | Launch>**` and a sprint. Update `bmm-workflow-status.yaml`.
- **Shifting a story between release phases:** edit its header marker, move it in the relevant phase manifest, update `bmm-workflow-status.yaml`. Don't renumber.
- **Closing an epic:** mark all stories `completed` in the YAML; tick the epic's DoD; leave the epic file in place.
