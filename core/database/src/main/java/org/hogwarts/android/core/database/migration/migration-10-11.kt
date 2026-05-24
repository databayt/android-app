package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 10 to 11.
 * Adds books and borrowings tables for library module.
 */
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `books` (
                `id` TEXT NOT NULL, `schoolId` TEXT NOT NULL, `title` TEXT NOT NULL, `author` TEXT NOT NULL,
                `isbn` TEXT NOT NULL DEFAULT '', `category` TEXT NOT NULL, `description` TEXT NOT NULL DEFAULT '',
                `coverImageUrl` TEXT, `availableCopies` INTEGER NOT NULL DEFAULT 0, `totalCopies` INTEGER NOT NULL DEFAULT 0,
                `shelfLocation` TEXT, `sectionName` TEXT, `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_books_schoolId` ON `books` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_books_category_schoolId` ON `books` (`category`, `schoolId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `borrowings` (
                `id` TEXT NOT NULL, `schoolId` TEXT NOT NULL, `bookId` TEXT NOT NULL, `bookTitle` TEXT NOT NULL,
                `userId` TEXT NOT NULL, `borrowedDate` TEXT NOT NULL, `dueDate` TEXT NOT NULL,
                `returnedDate` TEXT, `status` TEXT NOT NULL, `fine` REAL, `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_borrowings_schoolId` ON `borrowings` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_borrowings_userId_schoolId` ON `borrowings` (`userId`, `schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_borrowings_bookId_schoolId` ON `borrowings` (`bookId`, `schoolId`)")
    }
}
