# Web parity

Each native screen mirrors a hogwarts phone screen. This table records the
hogwarts commit each screen was last mirrored from, so drift can be diffed with
`git -C ../hogwarts diff <sha>..main -- <web path>`.

| Screen | Web source | Android | hogwarts sha | Verified on device |
| --- | --- | --- | --- | --- |
| Shell: header + Menu | `template/mobile-nav`, `template/platform-sidebar/config.ts` | `app/.../shell/` | `fa86f317a` | 2026-09-14, teacher@balqalam.com |
| Dashboard | `school-dashboard/dashboard/*` | `feature/dashboard` | `fa86f317a` | 2026-09-14, teacher@balqalam.com |
| Attendance (teacher quick, admin overview, student, guardian) | `school-dashboard/attendance/*` | `feature/attendance` | `fa86f317a` | 2026-09-14, teacher (roster only, no save) |
| Login, reset, school picker | `(auth)/login`, `components/auth/*` | `feature/auth` | `fa86f317a` | 2026-09-14, emulator API 34 |
| Messages shell + thread | `school-dashboard/messaging/mobile/*` | `feature/messaging` | `fa86f317a` | 2026-09-14, teacher@balqalam.com |
| Announcements (list + reading page) | `(listings)/announcements`, `listings/announcements/*` | `feature/announcements` | `0c177706c` | 2026-09-15, teacher@balqalam.com |
| Exams landing per role, upcoming, detail, question bank, online exam | `exams/*` | `feature/exams` | `0c177706c` | 2026-09-15, teacher (landing) |
| Finance: families + staff landing | `finance/family/*`, `finance/*` | `feature/fees` | `0c177706c` | 2026-09-15, parent@balqalam.com (no pay) |
| Settings tabs | `settings/*` | `feature/settings` | `0c177706c` | screenshots only |
| Notifications list + preferences | `notifications/*` | `feature/notifications` | `0c177706c` | screenshots only |
| Timetable per role (student, teacher, guardian; staff on web) | `timetable/*` | `feature/timetable` | `0c177706c` | 2026-09-15, teacher@balqalam.com |

Screens intentionally differing from the web are noted in their commit
messages (for example Latin clock digits in Messages, which the web forces).
