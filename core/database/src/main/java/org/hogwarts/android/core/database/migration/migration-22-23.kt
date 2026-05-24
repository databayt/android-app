package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 22 to 23.
 *
 * Stream LMS — adds a thumbnail URL to lessons so chapter cards can render
 * a preview image (currently chapter cards fall back to course thumbnail).
 */
val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `lessons` ADD COLUMN `thumbnailUrl` TEXT")
    }
}
