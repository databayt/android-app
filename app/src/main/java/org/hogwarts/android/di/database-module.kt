package org.hogwarts.android.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.database.HogwartsDatabase
import org.hogwarts.android.core.database.migration.ALL_MIGRATIONS
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
        .addMigrations(*ALL_MIGRATIONS)
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
