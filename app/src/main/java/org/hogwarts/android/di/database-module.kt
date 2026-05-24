package org.hogwarts.android.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.database.HogwartsDatabase
import org.hogwarts.android.core.database.migration.MIGRATION_10_11
import org.hogwarts.android.core.database.migration.MIGRATION_11_12
import org.hogwarts.android.core.database.migration.MIGRATION_12_13
import org.hogwarts.android.core.database.migration.MIGRATION_13_14
import org.hogwarts.android.core.database.migration.MIGRATION_14_15
import org.hogwarts.android.core.database.migration.MIGRATION_15_16
import org.hogwarts.android.core.database.migration.MIGRATION_16_17
import org.hogwarts.android.core.database.migration.MIGRATION_17_18
import org.hogwarts.android.core.database.migration.MIGRATION_18_19
import org.hogwarts.android.core.database.migration.MIGRATION_19_20
import org.hogwarts.android.core.database.migration.MIGRATION_20_21
import org.hogwarts.android.core.database.migration.MIGRATION_21_22
import org.hogwarts.android.core.database.migration.MIGRATION_22_23
import org.hogwarts.android.core.database.migration.MIGRATION_23_24
import org.hogwarts.android.core.database.migration.MIGRATION_7_8
import org.hogwarts.android.core.database.migration.MIGRATION_8_9
import org.hogwarts.android.core.database.migration.MIGRATION_9_10
import org.hogwarts.android.core.database.dao.AnnouncementDao
import org.hogwarts.android.core.database.dao.AttendanceBadgeDao
import org.hogwarts.android.core.database.dao.AttendanceDao
import org.hogwarts.android.core.database.dao.BookDao
import org.hogwarts.android.core.database.dao.CourseDao
import org.hogwarts.android.core.database.dao.ChildDao
import org.hogwarts.android.core.database.dao.ClassDao
import org.hogwarts.android.core.database.dao.EventDao
import org.hogwarts.android.core.database.dao.ConversationDao
import org.hogwarts.android.core.database.dao.ExamAnswerDao
import org.hogwarts.android.core.database.dao.ExamDao
import org.hogwarts.android.core.database.dao.FeeDao
import org.hogwarts.android.core.database.dao.GradeDao
import org.hogwarts.android.core.database.dao.InvoiceDao
import org.hogwarts.android.core.database.dao.LessonPlanDao
import org.hogwarts.android.core.database.dao.MessageAttachmentDao
import org.hogwarts.android.core.database.dao.MessageDao
import org.hogwarts.android.core.database.dao.NotificationDao
import org.hogwarts.android.core.database.dao.PendingMessageDao
import org.hogwarts.android.core.database.dao.PendingOperationDao
import org.hogwarts.android.core.database.dao.ReportCardDao
import org.hogwarts.android.core.database.dao.SchoolSettingsDao
import org.hogwarts.android.core.database.dao.StudentDao
import org.hogwarts.android.core.database.dao.SubjectDao
import org.hogwarts.android.core.database.dao.TimetableDao
import org.hogwarts.android.core.database.dao.UserDao
import javax.inject.Singleton

/**
 * Hilt module for Room database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): HogwartsDatabase = Room.databaseBuilder(
        context,
        HogwartsDatabase::class.java,
        HogwartsDatabase.DATABASE_NAME
    )
        .addMigrations(
            // Span: v7 → v23 (current). v1→v7 has no authored migrations; users
            // never shipped on those versions outside development. New v23+
            // schema bumps must add a corresponding MIGRATION_X_Y file here.
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
            MIGRATION_12_13,
            MIGRATION_13_14,
            MIGRATION_14_15,
            MIGRATION_15_16,
            MIGRATION_16_17,
            MIGRATION_17_18,
            MIGRATION_18_19,
            MIGRATION_19_20,
            MIGRATION_20_21,
            MIGRATION_21_22,
            MIGRATION_22_23,
            MIGRATION_23_24,
        )
        .build()

    @Provides
    fun provideUserDao(database: HogwartsDatabase): UserDao =
        database.userDao()

    @Provides
    fun providePendingOperationDao(database: HogwartsDatabase): PendingOperationDao =
        database.pendingOperationDao()

    @Provides
    fun provideAttendanceDao(database: HogwartsDatabase): AttendanceDao =
        database.attendanceDao()

    @Provides
    fun provideGradeDao(database: HogwartsDatabase): GradeDao =
        database.gradeDao()

    @Provides
    fun provideTimetableDao(database: HogwartsDatabase): TimetableDao =
        database.timetableDao()

    @Provides
    fun provideStudentDao(database: HogwartsDatabase): StudentDao =
        database.studentDao()

    @Provides
    fun provideConversationDao(database: HogwartsDatabase): ConversationDao =
        database.conversationDao()

    @Provides
    fun provideMessageDao(database: HogwartsDatabase): MessageDao =
        database.messageDao()

    @Provides
    fun provideNotificationDao(database: HogwartsDatabase): NotificationDao =
        database.notificationDao()

    @Provides
    fun provideFeeDao(database: HogwartsDatabase): FeeDao =
        database.feeDao()

    @Provides
    fun provideExamDao(database: HogwartsDatabase): ExamDao =
        database.examDao()

    @Provides
    fun provideAnnouncementDao(database: HogwartsDatabase): AnnouncementDao =
        database.announcementDao()

    @Provides
    fun provideChildDao(database: HogwartsDatabase): ChildDao =
        database.childDao()

    @Provides
    fun provideClassDao(database: HogwartsDatabase): ClassDao =
        database.classDao()

    @Provides
    fun provideEventDao(database: HogwartsDatabase): EventDao =
        database.eventDao()

    @Provides
    fun provideBookDao(database: HogwartsDatabase): BookDao =
        database.bookDao()

    @Provides
    fun provideSubjectDao(database: HogwartsDatabase): SubjectDao =
        database.subjectDao()

    @Provides
    fun provideReportCardDao(database: HogwartsDatabase): ReportCardDao =
        database.reportCardDao()

    @Provides
    fun provideInvoiceDao(database: HogwartsDatabase): InvoiceDao =
        database.invoiceDao()

    @Provides
    fun provideCourseDao(database: HogwartsDatabase): CourseDao =
        database.courseDao()

    @Provides
    fun provideAttendanceBadgeDao(database: HogwartsDatabase): AttendanceBadgeDao =
        database.attendanceBadgeDao()

    @Provides
    fun provideExamAnswerDao(database: HogwartsDatabase): ExamAnswerDao =
        database.examAnswerDao()

    @Provides
    fun provideLessonPlanDao(database: HogwartsDatabase): LessonPlanDao =
        database.lessonPlanDao()

    @Provides
    fun provideSchoolSettingsDao(database: HogwartsDatabase): SchoolSettingsDao =
        database.schoolSettingsDao()

    @Provides
    fun providePendingMessageDao(database: HogwartsDatabase): PendingMessageDao =
        database.pendingMessageDao()

    @Provides
    fun provideMessageAttachmentDao(database: HogwartsDatabase): MessageAttachmentDao =
        database.messageAttachmentDao()
}
