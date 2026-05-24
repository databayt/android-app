package org.hogwarts.android.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.hogwarts.android.core.database.converter.DateConverter
import org.hogwarts.android.core.database.converter.EnumConverter
import org.hogwarts.android.core.database.dao.AnnouncementDao
import org.hogwarts.android.core.database.dao.AttendanceDao
import org.hogwarts.android.core.database.dao.AttendanceBadgeDao
import org.hogwarts.android.core.database.dao.BookDao
import org.hogwarts.android.core.database.dao.ChildDao
import org.hogwarts.android.core.database.dao.ClassDao
import org.hogwarts.android.core.database.dao.CourseDao
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
import org.hogwarts.android.core.database.entity.AcademicYearEntity
import org.hogwarts.android.core.database.entity.AnnouncementEntity
import org.hogwarts.android.core.database.entity.AttendanceBadgeEntity
import org.hogwarts.android.core.database.entity.AttendanceEntity
import org.hogwarts.android.core.database.entity.AttendanceInterventionEntity
import org.hogwarts.android.core.database.entity.BookEntity
import org.hogwarts.android.core.database.entity.BorrowingEntity
import org.hogwarts.android.core.database.entity.ChapterEntity
import org.hogwarts.android.core.database.entity.ChildEntity
import org.hogwarts.android.core.database.entity.ClassAssignmentEntity
import org.hogwarts.android.core.database.entity.ClassEntity
import org.hogwarts.android.core.database.entity.CourseEntity
import org.hogwarts.android.core.database.entity.EnrollmentEntity
import org.hogwarts.android.core.database.entity.EventEntity
import org.hogwarts.android.core.database.entity.EventRegistrationEntity
import org.hogwarts.android.core.database.entity.ConversationEntity
import org.hogwarts.android.core.database.entity.ExamAnswerEntity
import org.hogwarts.android.core.database.entity.ExamEntity
import org.hogwarts.android.core.database.entity.ExamViolationEntity
import org.hogwarts.android.core.database.entity.FeeEntity
import org.hogwarts.android.core.database.entity.GradeEntity
import org.hogwarts.android.core.database.entity.HallPassEntity
import org.hogwarts.android.core.database.entity.InvoiceEntity
import org.hogwarts.android.core.database.entity.InvoiceLineItemEntity
import org.hogwarts.android.core.database.entity.LessonEntity
import org.hogwarts.android.core.database.entity.LessonPlanEntity
import org.hogwarts.android.core.database.entity.LessonProgressEntity
import org.hogwarts.android.core.database.entity.LessonResourceEntity
import org.hogwarts.android.core.database.entity.MessageAttachmentEntity
import org.hogwarts.android.core.database.entity.MessageEntity
import org.hogwarts.android.core.database.entity.NotificationEntity
import org.hogwarts.android.core.database.entity.PendingMessageEntity
import org.hogwarts.android.core.database.entity.PaymentTransactionEntity
import org.hogwarts.android.core.database.entity.PendingOperationEntity
import org.hogwarts.android.core.database.entity.QuestionBankEntity
import org.hogwarts.android.core.database.entity.ReportCardEntity
import org.hogwarts.android.core.database.entity.SchoolSettingsEntity
import org.hogwarts.android.core.database.entity.StudentEntity
import org.hogwarts.android.core.database.entity.SubjectEntity
import org.hogwarts.android.core.database.entity.SubjectReportEntity
import org.hogwarts.android.core.database.entity.TimetableEntity
import org.hogwarts.android.core.database.entity.UserEntity

/**
 * Main Room database for Hogwarts Android app.
 *
 * All entities MUST include schoolId for multi-tenant isolation.
 * All queries MUST filter by schoolId.
 *
 * Schema changes require migration - see migrations/ package.
 */
@Database(
    entities = [
        UserEntity::class,
        PendingOperationEntity::class,
        AttendanceEntity::class,
        GradeEntity::class,
        TimetableEntity::class,
        StudentEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        NotificationEntity::class,
        FeeEntity::class,
        ExamEntity::class,
        AnnouncementEntity::class,
        ChildEntity::class,
        ClassEntity::class,
        ClassAssignmentEntity::class,
        EventEntity::class,
        EventRegistrationEntity::class,
        BookEntity::class,
        BorrowingEntity::class,
        SubjectEntity::class,
        ReportCardEntity::class,
        SubjectReportEntity::class,
        InvoiceEntity::class,
        InvoiceLineItemEntity::class,
        PaymentTransactionEntity::class,
        CourseEntity::class,
        ChapterEntity::class,
        LessonEntity::class,
        EnrollmentEntity::class,
        LessonProgressEntity::class,
        AttendanceBadgeEntity::class,
        HallPassEntity::class,
        AttendanceInterventionEntity::class,
        ExamAnswerEntity::class,
        ExamViolationEntity::class,
        QuestionBankEntity::class,
        LessonPlanEntity::class,
        LessonResourceEntity::class,
        SchoolSettingsEntity::class,
        AcademicYearEntity::class,
        PendingMessageEntity::class,
        MessageAttachmentEntity::class,
    ],
    version = 24,
    exportSchema = true
)
@TypeConverters(
    DateConverter::class,
    EnumConverter::class
)
abstract class HogwartsDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun pendingOperationDao(): PendingOperationDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun gradeDao(): GradeDao
    abstract fun timetableDao(): TimetableDao
    abstract fun studentDao(): StudentDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun feeDao(): FeeDao
    abstract fun examDao(): ExamDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun childDao(): ChildDao
    abstract fun classDao(): ClassDao
    abstract fun eventDao(): EventDao
    abstract fun bookDao(): BookDao
    abstract fun subjectDao(): SubjectDao
    abstract fun reportCardDao(): ReportCardDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun courseDao(): CourseDao
    abstract fun attendanceBadgeDao(): AttendanceBadgeDao
    abstract fun examAnswerDao(): ExamAnswerDao
    abstract fun lessonPlanDao(): LessonPlanDao
    abstract fun schoolSettingsDao(): SchoolSettingsDao
    abstract fun pendingMessageDao(): PendingMessageDao
    abstract fun messageAttachmentDao(): MessageAttachmentDao

    companion object {
        const val DATABASE_NAME = "hogwarts_database"
    }
}
