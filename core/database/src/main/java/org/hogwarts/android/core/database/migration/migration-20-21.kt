package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 20 to 21.
 *
 * Stream LMS — adds single-grade scoping to courses with a (grade, schoolId) index.
 * (Superseded in v22 by a multi-grade `grades` TEXT array; see migration-21-22.)
 */
val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `courses` ADD COLUMN `grade` INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_courses_grade_schoolId` ON `courses` (`grade`, `schoolId`)")
    }
}
