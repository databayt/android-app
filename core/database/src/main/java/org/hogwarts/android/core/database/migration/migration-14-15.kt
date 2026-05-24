package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 14 to 15.
 * Adds courses, chapters, lessons, enrollments, and lesson_progress tables for LMS/Stream module.
 */
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `courses` (
                `id` TEXT NOT NULL, `schoolId` TEXT NOT NULL, `title` TEXT NOT NULL,
                `description` TEXT NOT NULL, `instructorName` TEXT NOT NULL,
                `thumbnailUrl` TEXT, `category` TEXT NOT NULL,
                `enrollmentCount` INTEGER NOT NULL DEFAULT 0, `lessonCount` INTEGER NOT NULL DEFAULT 0,
                `totalDuration` INTEGER NOT NULL DEFAULT 0, `status` TEXT NOT NULL,
                `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_courses_schoolId` ON `courses` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_courses_category_schoolId` ON `courses` (`category`, `schoolId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `chapters` (
                `id` TEXT NOT NULL, `courseId` TEXT NOT NULL, `title` TEXT NOT NULL,
                `orderIndex` INTEGER NOT NULL, `lessonCount` INTEGER NOT NULL DEFAULT 0,
                `completedLessons` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chapters_courseId` ON `chapters` (`courseId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `lessons` (
                `id` TEXT NOT NULL, `chapterId` TEXT NOT NULL, `title` TEXT NOT NULL,
                `type` TEXT NOT NULL, `duration` INTEGER NOT NULL DEFAULT 0,
                `contentUrl` TEXT, `orderIndex` INTEGER NOT NULL,
                `isCompleted` INTEGER NOT NULL DEFAULT 0, `isLocked` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_lessons_chapterId` ON `lessons` (`chapterId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `enrollments` (
                `id` TEXT NOT NULL, `courseId` TEXT NOT NULL, `userId` TEXT NOT NULL,
                `progress` REAL NOT NULL DEFAULT 0, `startedAt` INTEGER NOT NULL,
                `lastAccessedAt` INTEGER NOT NULL, `completedAt` INTEGER,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_enrollments_courseId_userId` ON `enrollments` (`courseId`, `userId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_enrollments_userId` ON `enrollments` (`userId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `lesson_progress` (
                `id` TEXT NOT NULL, `lessonId` TEXT NOT NULL, `enrollmentId` TEXT NOT NULL,
                `status` TEXT NOT NULL, `score` REAL, `startedAt` INTEGER, `completedAt` INTEGER,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_lesson_progress_lessonId` ON `lesson_progress` (`lessonId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_lesson_progress_enrollmentId` ON `lesson_progress` (`enrollmentId`)")
    }
}
