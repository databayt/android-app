package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 18 to 19.
 * Adds school_settings and academic_years tables for School Administration module.
 */
val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `school_settings` (
                `id` TEXT NOT NULL, `schoolId` TEXT NOT NULL, `name` TEXT NOT NULL,
                `domain` TEXT NOT NULL, `logoUrl` TEXT, `contactEmail` TEXT,
                `contactPhone` TEXT, `address` TEXT, `subscription` TEXT,
                `academicYear` TEXT, `activeTerms` TEXT,
                `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_school_settings_schoolId` ON `school_settings` (`schoolId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `academic_years` (
                `id` TEXT NOT NULL, `schoolId` TEXT NOT NULL, `name` TEXT NOT NULL,
                `startDate` TEXT NOT NULL, `endDate` TEXT NOT NULL,
                `isCurrent` INTEGER NOT NULL DEFAULT 0, `terms` TEXT,
                `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_academic_years_schoolId` ON `academic_years` (`schoolId`)")
    }
}
