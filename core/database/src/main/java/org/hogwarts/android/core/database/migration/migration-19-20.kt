package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 19 to 20.
 *
 * Messaging module v2 — adds WhatsApp-flavored conversation/message metadata
 * and introduces the offline-attachment + outbound-pending-message queues.
 *
 * Conversations: avatar URL, last-message status, WhatsApp-enabled flag, pin/mute toggles.
 * Messages: sender avatar URL, content type, status, edit/delete flags, dedup nonce, WhatsApp status.
 * New tables: message_attachments (per-message file metadata), pending_messages (outbound queue).
 */
val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // conversations: 5 new columns
        db.execSQL("ALTER TABLE `conversations` ADD COLUMN `avatarUrl` TEXT")
        db.execSQL("ALTER TABLE `conversations` ADD COLUMN `lastMessageStatus` TEXT")
        db.execSQL("ALTER TABLE `conversations` ADD COLUMN `isWhatsAppEnabled` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `conversations` ADD COLUMN `isPinned` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `conversations` ADD COLUMN `isMuted` INTEGER NOT NULL DEFAULT 0")

        // messages: 7 new columns
        db.execSQL("ALTER TABLE `messages` ADD COLUMN `senderAvatarUrl` TEXT")
        db.execSQL("ALTER TABLE `messages` ADD COLUMN `contentType` TEXT NOT NULL DEFAULT 'TEXT'")
        db.execSQL("ALTER TABLE `messages` ADD COLUMN `status` TEXT NOT NULL DEFAULT 'SENT'")
        db.execSQL("ALTER TABLE `messages` ADD COLUMN `isEdited` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `messages` ADD COLUMN `isDeleted` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `messages` ADD COLUMN `nonce` TEXT")
        db.execSQL("ALTER TABLE `messages` ADD COLUMN `whatsappStatus` TEXT")

        // message_attachments: new table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `message_attachments` (
                `id` TEXT NOT NULL,
                `schoolId` TEXT NOT NULL,
                `messageId` TEXT NOT NULL,
                `fileName` TEXT NOT NULL,
                `fileUrl` TEXT NOT NULL,
                `fileSize` INTEGER NOT NULL,
                `fileType` TEXT NOT NULL,
                `thumbnail` TEXT,
                `width` INTEGER,
                `height` INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_attachments_schoolId` ON `message_attachments` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_attachments_messageId` ON `message_attachments` (`messageId`)")

        // pending_messages: new table (outbound queue)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `pending_messages` (
                `id` TEXT NOT NULL,
                `schoolId` TEXT NOT NULL,
                `conversationId` TEXT NOT NULL,
                `content` TEXT NOT NULL,
                `contentType` TEXT NOT NULL,
                `replyToId` TEXT,
                `attachmentUri` TEXT,
                `status` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `retryCount` INTEGER NOT NULL,
                `lastAttemptAt` INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pending_messages_schoolId` ON `pending_messages` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pending_messages_schoolId_conversationId` ON `pending_messages` (`schoolId`, `conversationId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pending_messages_status` ON `pending_messages` (`status`)")
    }
}
