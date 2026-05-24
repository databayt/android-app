# Epic E06: Database Content Translation (CRITICAL)

**Epic ID:** EPIC-PROD-06
**Title:** Database Content Translation
**Status:** Not Started
**Owner:** BMAD Dev Agent
**Priority:** P0 (the user's central ask)
**Sprint:** 4-5
**Total Points:** 47

---

## 1. Overview

**This is the load-bearing translation epic.** It mirrors hogwarts' `lang` column + `TranslationCache` pattern verbatim. Every translatable Room entity carries a `lang: String @default("ar")` column declaring what language its content is written in. A tenant-scoped `TranslationCache` table holds Google-Translate-derived (server-side) translations on demand, looked up by `(schoolId, sourceText, sourceLanguage, targetLanguage)`. The Server is the translation authority.

### Why this matters

Without this, an Arabic announcement created by a teacher is invisible (or appears as raw Arabic) to an English-display student. Hogwarts solved this with non-trivial machinery (`src/components/translation/{display,util,actions,google,legacy}.ts`) and the Android client must mirror it.

### Architectural decisions (already locked in overview)

- **AD-03**: Mirror `lang` + `TranslationCache` pattern verbatim.
- **AD-04**: `Tenant.nameEn` exception (pre-translated school name to avoid translation cache lookups on every screen render).
- The Server is the translation authority — Android calls `POST /api/mobile/translate`, which reuses the web's `translateWithCache(text, sourceLang, targetLang, schoolId)`. Both clients share the cache.
- Pre-seed canonical reference labels (subjects, grade levels, terms) on tenant onboarding.

### Business Value

- Multi-language schools can operate normally — teachers write in their preferred language; readers see content in theirs.
- Translation cost minimized via server-shared cache + LRU eviction + canonical pre-seeds.
- New Detekt rule (`RequireLangColumn`) prevents schema regression.

### Success Criteria

- [ ] Every translatable Room entity has a `lang: String` column.
- [ ] `TranslationRepository.getDisplayText(text, contentLang, displayLang)` is called at every display site for translatable fields.
- [ ] An announcement created in Arabic by a teacher renders in English for an English-display student (with cache hit on second render — verified by Analytics event).
- [ ] Detekt `RequireLangColumn` enforces the schema invariant.
- [ ] Local `TranslationCacheDao` mirrors the server's `TranslationCache` row; round-trips work.
- [ ] New tenant onboarding pre-seeds translations for canonical reference data.
- [ ] LRU eviction worker keeps cache <5000 entries per tenant.

---

## 2. Stories

### Story E06.S01: Add `lang: String` column to all translatable entities [8 pts]
**Status:** Not Started · **Sprint:** 4

**As a** server, **I want** the client to track each row's source language, **So that** display-side translation can be triggered correctly.

**Acceptance Criteria:**
- [ ] 24 entities updated with `lang: String` (default `"ar"`):
  - `AnnouncementEntity`, `AttendanceEntity` (where translatable fields exist), `BookEntity`, `ChapterEntity`, `ClassEntity`, `CourseEntity`, `EventEntity`, `ExamEntity`, `FeeEntity`, `GradeEntity`, `LessonEntity`, `LessonPlanEntity`, `NotificationEntity`, `ReportCardEntity`, `SubjectEntity`, `StudentEntity` (display name field), `StaffEntity`, `ClassroomEntity`, `AcademicGradeEntity`, `AcademicLevelEntity`, `AcademicStreamEntity`, `AssignmentEntity`, `MaterialEntity`, `CurriculumEntity`.
- [ ] `@Translatable` annotation added on each (used by Detekt rule `RequireLangColumn` from E01.S09 / E06.S09).
- [ ] Schema migration `v23 → v24` adds the column. Existing rows backfilled with `"ar"` (Arabic is platform default per `i18n.defaultLocale`).
- [ ] Schema export at `core/database/schemas/24.json`.

**Files:** `core/database/.../entity/{...}-entity.kt` × 24, new `core/database/migration/migration-23-24.kt`, `core/database/.../hogwarts-database.kt` (bump to v24), `core/database/schemas/24.json`.

**Refs:** hogwarts entities with `lang` listed in §6 of `~/.claude/plans/typed-wobbling-giraffe.md`.

---

### Story E06.S02: `TranslationCacheEntity` + DAO [3 pts]
**Status:** Not Started · **Sprint:** 4

**As a** client, **I want** a local mirror of the server's translation cache, **So that** previously-seen translations don't require a network round-trip.

**Acceptance Criteria:**
- [ ] New `TranslationCacheEntity`:
  ```kotlin
  @Entity(
    tableName = "translation_cache",
    indices = [Index(value = ["schoolId", "sourceText", "sourceLanguage", "targetLanguage"], unique = true)]
  )
  data class TranslationCacheEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val sourceText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val translatedText: String,
    val provider: String = "google",
    val hitCount: Int = 0,
    val lastAccessedAt: Long,
  )
  ```
- [ ] DAO with `findCached(schoolId, sourceText, sourceLanguage, targetLanguage): TranslationCacheEntity?`, `upsert(entity)`, `incrementHit(id)`, `evictLru(schoolId, maxSize)`, `countForTenant(schoolId): Int`.
- [ ] Wired into `HogwartsDatabase` (bump to v25 if separate from E06.S01 schema bump).
- [ ] Provided in `DatabaseModule`.

**Files:** new `core/database/entity/translation-cache-entity.kt`, new `core/database/dao/translation-cache-dao.kt`.

**Refs:** hogwarts `prisma/models/translation-cache.prisma`.

---

### Story E06.S03: `TranslationRepository` with `getDisplayText` + `getDisplayFields` [8 pts]
**Status:** Not Started · **Sprint:** 4

**As a** UI layer, **I want** one helper to call at every display site, **So that** I never render untranslated content.

**Acceptance Criteria:**
- [ ] New module `core/translation` registered in `settings.gradle.kts`.
- [ ] `interface TranslationRepository { suspend fun getDisplayText(text: String, contentLang: String, displayLang: String): String; suspend fun getDisplayFields(entity: Any, fields: List<String>, contentLang: String, displayLang: String): Map<String, String> }`.
- [ ] `TranslationRepositoryImpl` algorithm:
  1. If `contentLang == displayLang`, return text immediately.
  2. **Script-mismatch guard**: if `displayLang == "ar"` and Arabic Unicode block (U+0600..U+06FF) is present in text, return text as-is (mirrors hogwarts `display.ts:53-58`).
  3. Look up `translationCacheDao.findCached(schoolId, text, contentLang, displayLang)`. If hit, increment hit count + return.
  4. Call `POST /api/mobile/translate` (E06.S04). Server hits its own cache (web's `TranslationCache`) → returns translated text.
  5. Upsert local cache.
- [ ] Concurrent in-flight de-duplication via `Mutex` keyed on `(text, sourceLang, targetLang)` to avoid duplicate API calls.
- [ ] Logs `translation_cache_hit` / `translation_cache_miss` / `translation_script_mismatch_guard` Analytics events (E06.S12).

**Files:** new `core/translation/build.gradle.kts`, `core/translation/.../translation-repository.kt`, `core/translation/.../translation-repository-impl.kt`.

**Refs:** hogwarts `src/components/translation/display.ts:53-58` (script-mismatch guard).

---

### Story E06.S04: `Translation` API endpoint contract [3 pts]
**Status:** Not Started · **Sprint:** 4

**As a** client, **I want** to ask the server to translate, **So that** the server's cache is shared between web and mobile.

**Acceptance Criteria:**
- [ ] `POST /api/mobile/translate` accepts `{ text: String, sourceLang: String, targetLang: String, schoolId: String? }` (schoolId implicit from JWT).
- [ ] Response: `{ translatedText: String, fromCache: Boolean, provider: String }`.
- [ ] Backend reuses web's `translateWithCache(text, sourceLang, targetLang, schoolId)` from `src/components/translation/actions.ts`.
- [ ] Rate-limited per user (50 calls/min default).
- [ ] Returns 400 on `sourceLang == targetLang` (client should never send this; included as a safety net).

**Files:** Retrofit `core/network/api/translation-api.kt` (new).

**Backend dependency:** E08.S15 (server endpoint).

---

### Story E06.S05: `Tenant.nameEn` exception (pre-translated school name) [2 pts]
**Status:** Not Started · **Sprint:** 4

**As a** every-screen renderer, **I want** the school name pre-translated, **So that** I don't pay translation cache cost on every render.

**Acceptance Criteria:**
- [ ] `TenantInfo` data class adds `nameEn: String?`.
- [ ] New helper `getDisplayName(tenant: TenantInfo, locale: String): String` returns:
  - `nameEn` if `locale == "en"` AND `nameEn != null`
  - else translates via cache (E06.S03 path)
- [ ] All school-name display sites use `getDisplayName` (header, sidebar, splash, profile, etc.).

**Files:** `core/data/.../tenant/tenant-info.kt`, `core/translation/.../tenant-display-name.kt`, callsites.

**Refs:** hogwarts `school.prisma:5-6` and the layout consumer at `(school-dashboard)/layout.tsx:67-76`.

---

### Story E06.S06: `LocalizableText` value class [3 pts]
**Status:** Not Started · **Sprint:** 4

**As a** developer, **I want** the type system to remind me to translate before display, **So that** I can't accidentally render raw `entity.name`.

**Acceptance Criteria:**
- [ ] `@JvmInline value class LocalizableText private constructor(internal val raw: Pair<String, String>)` carries `(text, lang)`.
- [ ] Companion `fromEntity(text: String, lang: String): LocalizableText`.
- [ ] Property `text: String`, `lang: String`.
- [ ] Used in all view-state DTOs that surface translatable content.
- [ ] Detekt rule (informational) flags `Text(entity.name)` where `entity` has a `lang` field — recommends `LocalizedText(entity.toLocalizable())` from E06.S10.

**Files:** new `core/translation/.../localizable-text.kt`, new Detekt rule `RequireLocalizedTextForTranslatableEntity` (advisory).

---

### Story E06.S07: Pre-seed canonical reference translations on tenant onboarding [5 pts]
**Status:** Not Started · **Sprint:** 5

**As a** new tenant, **I want** common labels (grade levels, terms, subjects) pre-translated, **So that** display works without round-tripping the API for high-frequency labels.

**Acceptance Criteria:**
- [ ] New asset `core/database/src/main/assets/canonical-translations-seed.json` containing ~200 AR↔EN pairs:
  - Grade levels: `Grade 1` ↔ `الصف الأول` ... `Grade 12` ↔ `الصف الثاني عشر`.
  - Terms: `First Term` ↔ `الفصل الأول`, `Second Term` ↔ `الفصل الثاني`, `Third Term` ↔ `الفصل الثالث`.
  - Subject keys: `Mathematics` ↔ `الرياضيات`, `Science` ↔ `العلوم`, `Arabic` ↔ `اللغة العربية`, `English` ↔ `اللغة الإنجليزية`, `Islamic Studies` ↔ `التربية الإسلامية`, `Physical Education` ↔ `التربية البدنية`, etc.
  - Role names: `Admin` ↔ `إداري`, `Teacher` ↔ `معلم`, `Student` ↔ `طالب`, `Guardian` ↔ `ولي أمر`, `Accountant` ↔ `محاسب`, `Staff` ↔ `موظف`.
  - Common attendance / grade / fee status labels.
- [ ] On first tenant fetch (after login), `TenantOnboardingWorker` reads asset → upserts into `TranslationCache` (both ar→en and en→ar pairs).
- [ ] Idempotent — re-running doesn't duplicate.

**Files:** new `core/database/src/main/assets/canonical-translations-seed.json`, new `core/sync/.../tenant-onboarding-worker.kt`.

---

### Story E06.S08: `TranslationCache` LRU eviction worker [2 pts]
**Status:** Not Started · **Sprint:** 5

**As a** memory-conscious app, **I want** old translations purged, **So that** the cache doesn't grow unbounded.

**Acceptance Criteria:**
- [ ] WorkManager periodic worker (weekly) calls `translationCacheDao.evictLru(schoolId, maxSize = 5000)` per active tenant.
- [ ] Deletes least-recently-accessed rows when count exceeds `maxSize`.
- [ ] Runs on `Constraints.Builder().setRequiresCharging(true).setRequiresBatteryNotLow(true).build()`.
- [ ] Worker name: `translation-cache-cleanup`.

**Files:** new `core/sync/.../translation-cache-cleanup-worker.kt`, `core/sync/.../sync-manager-impl.kt` (schedule).

---

### Story E06.S09: `Translatable` annotation + Detekt `RequireLangColumn` rule applied [3 pts]
**Status:** Not Started · **Sprint:** 5

**As a** code reviewer, **I want** schema invariants enforced by static analysis, **So that** we can't accidentally add a translatable entity without a `lang` column.

**Acceptance Criteria:**
- [ ] `@Translatable` annotation defined in `core/translation/.../annotation/translatable.kt`.
- [ ] Applied to all 24 entities from E06.S01.
- [ ] Detekt rule `RequireLangColumn` (introduced in E01.S09) graduates to error.
- [ ] CI fails if a new `@Translatable @Entity` lacks `lang: String`.

**Files:** new annotation file, all 24 entities, Detekt rule (already exists).

---

### Story E06.S10: Compose `LocalizedText` Composable [3 pts]
**Status:** Not Started · **Sprint:** 5

**As a** UI developer, **I want** one Composable to drop in for translatable content, **So that** I don't write 5 lines per call site.

**Acceptance Criteria:**
- [ ] `@Composable LocalizedText(text: LocalizableText, modifier: Modifier = Modifier, style: TextStyle = LocalTextStyle.current, ...)`:
  - Reads current locale from `LocalConfiguration`.
  - If `text.lang == currentLocale`, renders directly.
  - Else launches a `LaunchedEffect(text)` calling `translationRepo.getDisplayText(...)`.
  - Shows shimmer placeholder while loading.
- [ ] Handles cancellation when Composable leaves composition.
- [ ] Respects all standard `Text` parameters.

**Files:** new `core/designsystem/atom/localized-text.kt`.

---

### Story E06.S11: Migrate existing translatable display sites [5 pts]
**Status:** Not Started · **Sprint:** 5

**As a** real user, **I want** existing screens to honor my locale, **So that** the new infrastructure actually shows up in the app I use.

**Acceptance Criteria:**
- [ ] Identify all sites that render translatable content. Estimated ~40-60 sites in:
  - `feature/announcements/...` (announcement title, content)
  - `feature/notifications/...` (notification title, body)
  - `feature/attendance/...` (attendance reasons)
  - `feature/fees/...` (fee descriptions)
  - `feature/exams/...` (exam titles, descriptions)
  - `feature/stream/...` (course titles, lesson titles, chapter titles)
  - `feature/events/...` (event titles, descriptions)
  - `feature/library/...` (book titles, authors)
  - `feature/lessons/...` (lesson plan titles)
  - `feature/subjects/...` (subject names — high-frequency, often hits canonical seeds)
  - `feature/students/...`, `feature/teacher/...` (where applicable)
- [ ] Replace `Text(entity.name)` with `LocalizedText(entity.toLocalizable())`.
- [ ] Add `EntityType.toLocalizable(): LocalizableText` extension functions per entity (~24).

**Files:** ~40-60 site fixes across feature modules.

---

### Story E06.S12: Translation observability [2 pts]
**Status:** Not Started · **Sprint:** 5

**As a** ops engineer, **I want** to see translation cache hit rate and API cost, **So that** I can budget for Google Cloud Translation v2 spend.

**Acceptance Criteria:**
- [ ] Analytics events emitted from `TranslationRepositoryImpl`:
  - `translation_cache_hit` (params: `schoolId`, `sourceLang`, `targetLang`, `textLength`)
  - `translation_cache_miss` (same params)
  - `translation_api_error` (param: `errorCode`)
  - `translation_script_mismatch_guard` (params: `schoolId`, `displayLang`)
- [ ] Surfaced in Firebase Analytics + monthly cost report.
- [ ] Custom dashboard in Firebase Console: cache hit rate %, API call count, error rate.

**Files:** `core/translation/.../translation-repository-impl.kt`, `core/push/analytics-tracker.kt` (add helpers).

---

## 3. Dependencies

| Dependency | Status |
|------------|--------|
| E01.S02 explicit migrations (foundation for v23 → v24) | Required |
| E01.S09 Detekt `RequireLangColumn` rule | Required for E06.S09 |
| E02.S06 `TenantContext` with `nameEn` field | Required for E06.S05 |
| Backend `POST /api/mobile/translate` | Required — see E08.S15 |
| Server-side `translateWithCache` (already exists in hogwarts) | Available |

---

## 4. Risks

| Risk | Mitigation |
|------|------------|
| Google Translate v2 quota exceeded (500K chars/month free) | E06.S07 pre-seed reduces calls; E06.S08 LRU eviction; E06.S12 observability flags issues; budget alerts in Cloud Console |
| Server cache and client cache diverge | Server cache is authoritative; client is opportunistic. On miss, client always re-asks server. |
| Script-mismatch guard incorrectly tagged data leaks through | Comprehensive parameterized test of the guard with 20 fixture strings (Arabic/English/Mixed/Numeric) |
| Migration adding `lang` column on large tables takes time | Default value `"ar"` makes it instant; benchmark on 100K-row test fixture |
| `LocalizedText` causes flicker on slow translation | Shimmer placeholder; pre-fetch on screen entry where feasible |

---

## 5. Out of Scope

- Real-time co-editing of translations (translator UI) → defer to admin tool.
- Auto-detect content language client-side → server detects on write; client trusts server.
- Translation memory editing UI → defer.

---

## 6. Definition of Done

- [ ] All 12 stories merged.
- [ ] All 24 translatable entities have `lang: String` column + `@Translatable`.
- [ ] Detekt `RequireLangColumn` green (would fail on regression).
- [ ] Manual E2E: Teacher (Arabic UI) creates announcement → Student (English UI) sees it translated → second view comes from cache (verified via Analytics dashboard).
- [ ] Cache hit rate ≥80% for canonical labels (subjects, grades) per Analytics dashboard within 48h of deployment.
- [ ] LRU eviction worker confirmed running weekly (verified via `WorkManager.getInstance().getWorkInfosByTagFlow(...)`).
- [ ] Captain signoff documented.
