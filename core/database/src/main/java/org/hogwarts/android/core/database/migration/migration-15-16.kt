package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 15 to 16.
 * Adds attendance_badges, hall_passes, and attendance_interventions tables for Advanced Attendance module.
 */
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `attendance_badges` (
                `id` TEXT NOT NULL, `schoolId` TEXT NOT NULL, `userId` TEXT NOT NULL,
                `name` TEXT NOT NULL, `description` TEXT NOT NULL, `iconUrl` TEXT,
                `type` TEXT NOT NULL, `earnedAt` INTEGER, `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_badges_schoolId` ON `attendance_badges` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_badges_userId_schoolId` ON `attendance_badges` (`userId`, `schoolId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `hall_passes` (
                `id` TEXT NOT NULL, `schoolId` TEXT NOT NULL, `studentId` TEXT NOT NULL,
                `studentName` TEXT NOT NULL, `teacherId` TEXT NOT NULL,
                `destination` TEXT NOT NULL, `reason` TEXT,
                `requestedAt` INTEGER NOT NULL, `approvedAt` INTEGER, `expiresAt` INTEGER,
                `status` TEXT NOT NULL, `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hall_passes_schoolId` ON `hall_passes` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hall_passes_studentId_schoolId` ON `hall_passes` (`studentId`, `schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hall_passes_teacherId_schoolId` ON `hall_passes` (`teacherId`, `schoolId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `attendance_interventions` (
                `id` TEXT NOT NULL, `schoolId` TEXT NOT NULL, `studentId` TEXT NOT NULL,
                `studentName` TEXT NOT NULL, `type` TEXT NOT NULL,
                `threshold` REAL NOT NULL, `currentRate` REAL NOT NULL,
                `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `resolvedAt` INTEGER,
                `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_interventions_schoolId` ON `attendance_interventions` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_interventions_studentId_schoolId` ON `attendance_interventions` (`studentId`, `schoolId`)")
    }
}
