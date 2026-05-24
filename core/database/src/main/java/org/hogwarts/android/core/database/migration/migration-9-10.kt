package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 9 to 10.
 * Adds the events and event_registrations tables for events module.
 */
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `events` (
                `id` TEXT NOT NULL,
                `schoolId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT NOT NULL DEFAULT '',
                `type` TEXT NOT NULL,
                `startDate` TEXT NOT NULL,
                `endDate` TEXT,
                `startTime` TEXT,
                `endTime` TEXT,
                `location` TEXT,
                `isAllDay` INTEGER NOT NULL DEFAULT 0,
                `maxAttendees` INTEGER,
                `currentAttendees` INTEGER NOT NULL DEFAULT 0,
                `isRegistered` INTEGER NOT NULL DEFAULT 0,
                `registrationRequired` INTEGER NOT NULL DEFAULT 0,
                `imageUrl` TEXT,
                `organizerName` TEXT,
                `status` TEXT NOT NULL DEFAULT 'UPCOMING',
                `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_events_schoolId` ON `events` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_events_type_schoolId` ON `events` (`type`, `schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_events_startDate_schoolId` ON `events` (`startDate`, `schoolId`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `event_registrations` (
                `id` TEXT NOT NULL,
                `schoolId` TEXT NOT NULL,
                `eventId` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `registeredAt` TEXT NOT NULL,
                `status` TEXT NOT NULL DEFAULT 'REGISTERED',
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_event_registrations_eventId_schoolId` ON `event_registrations` (`eventId`, `schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_event_registrations_userId_schoolId` ON `event_registrations` (`userId`, `schoolId`)")
    }
}
