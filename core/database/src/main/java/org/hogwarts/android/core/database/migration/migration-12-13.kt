package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 12 to 13.
 * Adds report_cards and subject_reports tables for report cards module.
 */
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `report_cards` (
                `id` TEXT NOT NULL, `schoolId` TEXT NOT NULL, `studentId` TEXT NOT NULL,
                `studentName` TEXT NOT NULL, `termId` TEXT NOT NULL, `termName` TEXT NOT NULL,
                `academicYear` TEXT NOT NULL, `gpa` REAL, `rank` INTEGER, `totalStudents` INTEGER,
                `status` TEXT NOT NULL DEFAULT 'Published', `overallRemarks` TEXT, `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_report_cards_schoolId` ON `report_cards` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_report_cards_studentId_schoolId` ON `report_cards` (`studentId`, `schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_report_cards_termId_schoolId` ON `report_cards` (`termId`, `schoolId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `subject_reports` (
                `id` TEXT NOT NULL, `reportCardId` TEXT NOT NULL, `subjectId` TEXT NOT NULL,
                `subjectName` TEXT NOT NULL, `teacherName` TEXT NOT NULL DEFAULT '',
                `marks` REAL NOT NULL, `maxMarks` REAL NOT NULL, `grade` TEXT NOT NULL,
                `percentage` REAL NOT NULL, `remarks` TEXT,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_subject_reports_reportCardId` ON `subject_reports` (`reportCardId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_subject_reports_subjectId` ON `subject_reports` (`subjectId`)")
    }
}
