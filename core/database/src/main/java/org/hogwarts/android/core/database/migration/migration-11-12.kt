package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 11 to 12.
 * Adds subjects table for subjects module.
 */
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `subjects` (
                `id` TEXT NOT NULL, `schoolId` TEXT NOT NULL, `name` TEXT NOT NULL,
                `code` TEXT NOT NULL DEFAULT '', `department` TEXT NOT NULL DEFAULT '',
                `description` TEXT NOT NULL DEFAULT '', `teacherCount` INTEGER NOT NULL DEFAULT 0,
                `studentCount` INTEGER NOT NULL DEFAULT 0, `iconUrl` TEXT, `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_subjects_schoolId` ON `subjects` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_subjects_department_schoolId` ON `subjects` (`department`, `schoolId`)")
    }
}
