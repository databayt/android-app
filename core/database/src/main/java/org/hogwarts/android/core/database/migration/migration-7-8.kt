package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 7 to 8.
 * Adds the children table for guardian module.
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `children` (
                `id` TEXT NOT NULL,
                `schoolId` TEXT NOT NULL,
                `guardianId` TEXT NOT NULL,
                `studentId` TEXT NOT NULL,
                `givenName` TEXT NOT NULL,
                `familyName` TEXT NOT NULL,
                `grade` TEXT NOT NULL,
                `section` TEXT NOT NULL DEFAULT '',
                `avatarUrl` TEXT,
                `attendanceRate` REAL NOT NULL DEFAULT 0,
                `latestGrade` TEXT,
                `feeBalance` REAL NOT NULL DEFAULT 0,
                `className` TEXT NOT NULL DEFAULT '',
                `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_children_schoolId` ON `children` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_children_guardianId_schoolId` ON `children` (`guardianId`, `schoolId`)")
    }
}
