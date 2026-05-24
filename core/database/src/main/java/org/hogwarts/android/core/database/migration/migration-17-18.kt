package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 17 to 18.
 * Adds lesson_plans and lesson_resources tables for Lessons & Curriculum module.
 */
val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `lesson_plans` (
                `id` TEXT NOT NULL, `schoolId` TEXT NOT NULL, `classId` TEXT NOT NULL,
                `subjectName` TEXT NOT NULL, `topic` TEXT NOT NULL, `date` TEXT NOT NULL,
                `objectives` TEXT NOT NULL, `activities` TEXT NOT NULL,
                `homework` TEXT, `teacherNotes` TEXT,
                `status` TEXT NOT NULL, `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_lesson_plans_schoolId` ON `lesson_plans` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_lesson_plans_classId_schoolId` ON `lesson_plans` (`classId`, `schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_lesson_plans_date_schoolId` ON `lesson_plans` (`date`, `schoolId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `lesson_resources` (
                `id` TEXT NOT NULL, `lessonPlanId` TEXT NOT NULL, `name` TEXT NOT NULL,
                `type` TEXT NOT NULL, `url` TEXT NOT NULL, `fileSize` INTEGER,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_lesson_resources_lessonPlanId` ON `lesson_resources` (`lessonPlanId`)")
    }
}
