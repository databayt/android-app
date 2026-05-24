package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 16 to 17.
 * Adds exam_answers, exam_violations, and question_bank tables for Advanced Exams module.
 */
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `exam_answers` (
                `id` TEXT NOT NULL, `examId` TEXT NOT NULL, `questionId` TEXT NOT NULL,
                `studentAnswer` TEXT, `isCorrect` INTEGER, `marksObtained` REAL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exam_answers_examId` ON `exam_answers` (`examId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exam_answers_examId_questionId` ON `exam_answers` (`examId`, `questionId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `exam_violations` (
                `id` TEXT NOT NULL, `examId` TEXT NOT NULL,
                `type` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `description` TEXT,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exam_violations_examId` ON `exam_violations` (`examId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `question_bank` (
                `id` TEXT NOT NULL, `schoolId` TEXT NOT NULL, `subject` TEXT NOT NULL,
                `topic` TEXT NOT NULL, `difficulty` TEXT NOT NULL, `type` TEXT NOT NULL,
                `question` TEXT NOT NULL, `options` TEXT, `correctAnswer` TEXT NOT NULL,
                `explanation` TEXT, `isBookmarked` INTEGER NOT NULL DEFAULT 0,
                `lastSyncedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_question_bank_schoolId` ON `question_bank` (`schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_question_bank_subject_schoolId` ON `question_bank` (`subject`, `schoolId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_question_bank_difficulty_schoolId` ON `question_bank` (`difficulty`, `schoolId`)")
    }
}
