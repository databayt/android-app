package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 23 to 24.
 *
 * Notifications — adds a `priority` column to mirror the web NotificationPriority
 * field (low | normal | high | urgent), enabling urgent highlighting and
 * priority-aware sorting in the mobile notification center.
 */
val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `notifications` ADD COLUMN `priority` TEXT NOT NULL DEFAULT 'normal'")
    }
}
