package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 8 to 9.
 * Adds the classes and class_assignments tables for teacher module.
 */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `classes` (
                `id` TEXT NOT NULL,
                `schoolId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `grade` TEXT NOT NULL,
                `section` TEXT NOT NULL DEFAULT '',
                `subjectId` TEXT NOT NULL,
                `subjectName` TEXT NOT NULL,
                `teacherCount` INTEGER NOT NULL DEFAULT 0,
                `studentCount` INTEGER NOT NULL DEFAULT 0,
                `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_classes_schoolId` ON `classes` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_classes_subjectId_schoolId` ON `classes` (`subjectId`, `schoolId`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `class_assignments` (
                `id` TEXT NOT NULL,
                `schoolId` TEXT NOT NULL,
                `classId` TEXT NOT NULL,
                `teacherId` TEXT NOT NULL,
                `role` TEXT NOT NULL DEFAULT 'PRIMARY',
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_class_assignments_classId_schoolId` ON `class_assignments` (`classId`, `schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_class_assignments_teacherId_schoolId` ON `class_assignments` (`teacherId`, `schoolId`)")
    }
}
