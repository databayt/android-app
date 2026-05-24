package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 13 to 14.
 * Adds invoices, invoice_line_items, and payment_transactions tables for Enhanced Finance (EPIC-25).
 */
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `invoices` (
                `id` TEXT NOT NULL, `schoolId` TEXT NOT NULL, `invoiceNumber` TEXT NOT NULL,
                `studentId` TEXT NOT NULL, `studentName` TEXT NOT NULL,
                `issueDate` TEXT NOT NULL, `dueDate` TEXT NOT NULL, `status` TEXT NOT NULL,
                `subtotal` REAL NOT NULL, `taxAmount` REAL NOT NULL DEFAULT 0.0,
                `discountAmount` REAL NOT NULL DEFAULT 0.0, `totalAmount` REAL NOT NULL,
                `paidAmount` REAL NOT NULL DEFAULT 0.0, `notes` TEXT,
                `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoices_schoolId` ON `invoices` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoices_studentId_schoolId` ON `invoices` (`studentId`, `schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoices_status_schoolId` ON `invoices` (`status`, `schoolId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `invoice_line_items` (
                `id` TEXT NOT NULL, `invoiceId` TEXT NOT NULL,
                `description` TEXT NOT NULL, `quantity` INTEGER NOT NULL DEFAULT 1,
                `unitPrice` REAL NOT NULL, `amount` REAL NOT NULL,
                `category` TEXT,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoice_line_items_invoiceId` ON `invoice_line_items` (`invoiceId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `payment_transactions` (
                `id` TEXT NOT NULL, `schoolId` TEXT NOT NULL,
                `invoiceId` TEXT, `feeId` TEXT,
                `amount` REAL NOT NULL, `currency` TEXT NOT NULL DEFAULT 'SAR',
                `paymentMethod` TEXT NOT NULL, `status` TEXT NOT NULL,
                `transactionRef` TEXT, `processedAt` INTEGER,
                `description` TEXT, `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_transactions_schoolId` ON `payment_transactions` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_transactions_invoiceId` ON `payment_transactions` (`invoiceId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_transactions_status_schoolId` ON `payment_transactions` (`status`, `schoolId`)")
    }
}
