package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 21 to 22.
 *
 * Stream LMS — replaces single-grade `grade INTEGER` on courses with multi-grade
 * `grades TEXT` (JSON-encoded array of grade numbers) so a course can target
 * several grade levels.
 *
 * SQLite (Android API 26+) doesn't support DROP COLUMN before 3.35, so we
 * recreate the table: copy rows with grade cast into a single-element JSON
 * array string ("[1]") when grade was set, NULL otherwise.
 */
val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Drop the v21-specific (grade, schoolId) index — it references the column we're dropping.
        db.execSQL("DROP INDEX IF EXISTS `index_courses_grade_schoolId`")

        // 2. Create new table with `grades TEXT` instead of `grade INTEGER`.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `courses_new` (
                `id` TEXT NOT NULL,
                `schoolId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `instructorName` TEXT NOT NULL,
                `thumbnailUrl` TEXT,
                `category` TEXT NOT NULL,
                `enrollmentCount` INTEGER NOT NULL,
                `lessonCount` INTEGER NOT NULL,
                `totalDuration` INTEGER NOT NULL,
                `status` TEXT NOT NULL,
                `grades` TEXT,
                `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        // 3. Copy data, transforming grade INTEGER → grades TEXT (JSON single-element array).
        db.execSQL(
            """
            INSERT INTO `courses_new` (
                `id`, `schoolId`, `title`, `description`, `instructorName`,
                `thumbnailUrl`, `category`, `enrollmentCount`, `lessonCount`,
                `totalDuration`, `status`, `grades`, `lastSyncedAt`
            )
            SELECT
                `id`, `schoolId`, `title`, `description`, `instructorName`,
                `thumbnailUrl`, `category`, `enrollmentCount`, `lessonCount`,
                `totalDuration`, `status`,
                CASE WHEN `grade` IS NULL THEN NULL ELSE '[' || `grade` || ']' END,
                `lastSyncedAt`
            FROM `courses`
            """.trimIndent()
        )

        // 4. Drop old table; rename new to canonical name.
        db.execSQL("DROP TABLE `courses`")
        db.execSQL("ALTER TABLE `courses_new` RENAME TO `courses`")

        // 5. Recreate the indices that survive v22.
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_courses_schoolId` ON `courses` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_courses_category_schoolId` ON `courses` (`category`, `schoolId`)")
    }
}
