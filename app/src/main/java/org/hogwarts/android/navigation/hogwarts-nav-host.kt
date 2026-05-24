package org.hogwarts.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.security.BiometricHelper
import org.hogwarts.android.feature.auth.navigation.AuthGraph
import org.hogwarts.android.feature.auth.navigation.authNavGraph
import org.hogwarts.android.feature.attendance.navigation.attendanceScreen
import org.hogwarts.android.feature.attendance.navigation.Attendance
import org.hogwarts.android.feature.attendance.navigation.gamificationScreen
import org.hogwarts.android.feature.attendance.navigation.hallPassScreen
import org.hogwarts.android.feature.attendance.navigation.interventionsScreen
import org.hogwarts.android.feature.attendance.navigation.attendanceAnalyticsScreen
import org.hogwarts.android.feature.attendance.navigation.attendanceMethodSettingsScreen
import org.hogwarts.android.feature.dashboard.navigation.Dashboard
import org.hogwarts.android.feature.dashboard.navigation.dashboardScreen
import org.hogwarts.android.feature.fees.navigation.Fees
import org.hogwarts.android.feature.grades.navigation.Grades
import org.hogwarts.android.feature.announcements.navigation.AnnouncementDetail
import org.hogwarts.android.feature.announcements.navigation.Announcements
import org.hogwarts.android.feature.announcements.navigation.announcementDetailScreen
import org.hogwarts.android.feature.announcements.navigation.announcementsScreen
import org.hogwarts.android.feature.exams.navigation.ExamCertificate
import org.hogwarts.android.feature.exams.navigation.ExamDetail
import org.hogwarts.android.feature.exams.navigation.ExamResults
import org.hogwarts.android.feature.exams.navigation.examDetailScreen
import org.hogwarts.android.feature.exams.navigation.examsScreen
import org.hogwarts.android.feature.exams.navigation.quizScreen
import org.hogwarts.android.feature.exams.navigation.onlineExamScreen
import org.hogwarts.android.feature.exams.navigation.questionBankScreen
import org.hogwarts.android.feature.exams.navigation.examResultsScreen
import org.hogwarts.android.feature.exams.navigation.examCertificateScreen
import org.hogwarts.android.feature.fees.navigation.FeeInvoiceDetail
import org.hogwarts.android.feature.fees.navigation.FeeInvoices
import org.hogwarts.android.feature.fees.navigation.FeePayment
import org.hogwarts.android.feature.fees.navigation.FeeReceipt
import org.hogwarts.android.feature.fees.navigation.FeeTransactions
import org.hogwarts.android.feature.fees.navigation.feesScreen
import org.hogwarts.android.feature.fees.navigation.invoiceListScreen
import org.hogwarts.android.feature.fees.navigation.invoiceDetailScreen
import org.hogwarts.android.feature.fees.navigation.paymentProcessingScreen
import org.hogwarts.android.feature.fees.navigation.paymentReceiptScreen
import org.hogwarts.android.feature.fees.navigation.transactionHistoryScreen
import org.hogwarts.android.feature.fees.navigation.feeBalanceDashboardScreen
import org.hogwarts.android.feature.grades.navigation.gradesScreen
import org.hogwarts.android.feature.messaging.navigation.Chat
import org.hogwarts.android.feature.messaging.navigation.ConversationInfo
import org.hogwarts.android.feature.messaging.navigation.ConversationMessageSearch
import org.hogwarts.android.feature.messaging.navigation.MessageSearch
import org.hogwarts.android.feature.messaging.navigation.Messaging
import org.hogwarts.android.feature.messaging.navigation.WhatsAppQR
import org.hogwarts.android.feature.messaging.navigation.WhatsAppSettings
import org.hogwarts.android.feature.messaging.navigation.chatScreen
import org.hogwarts.android.feature.messaging.navigation.conversationInfoScreen
import org.hogwarts.android.feature.messaging.navigation.messageSearchScreen
import org.hogwarts.android.feature.messaging.navigation.messagingScreen
import org.hogwarts.android.feature.messaging.navigation.whatsAppQRScreen
import org.hogwarts.android.feature.messaging.navigation.whatsAppSettingsScreen
import org.hogwarts.android.feature.notifications.navigation.NotificationPreferences
import org.hogwarts.android.feature.notifications.navigation.Notifications
import org.hogwarts.android.feature.notifications.navigation.notificationPreferencesScreen
import org.hogwarts.android.feature.notifications.navigation.notificationsScreen
import org.hogwarts.android.feature.profile.navigation.Profile
import org.hogwarts.android.feature.profile.navigation.profileScreen
import org.hogwarts.android.feature.settings.navigation.Settings
import org.hogwarts.android.feature.settings.navigation.settingsScreen
import org.hogwarts.android.feature.students.navigation.StudentDetail
import org.hogwarts.android.feature.students.navigation.StudentEdit
import org.hogwarts.android.feature.students.navigation.StudentsList
import org.hogwarts.android.feature.students.navigation.studentDetailScreen
import org.hogwarts.android.feature.students.navigation.studentFormScreen
import org.hogwarts.android.feature.students.navigation.studentsScreen
import org.hogwarts.android.feature.admission.navigation.AdmissionForm
import org.hogwarts.android.feature.admission.navigation.AdmissionList
import org.hogwarts.android.feature.admission.navigation.AdmissionStatus
import org.hogwarts.android.feature.admission.navigation.admissionListScreen
import org.hogwarts.android.feature.admission.navigation.admissionFormScreen
import org.hogwarts.android.feature.admission.navigation.admissionFormEditScreen
import org.hogwarts.android.feature.admission.navigation.admissionStatusScreen
import org.hogwarts.android.feature.events.navigation.EventCalendar
import org.hogwarts.android.feature.events.navigation.EventDetail
import org.hogwarts.android.feature.events.navigation.eventsListScreen
import org.hogwarts.android.feature.events.navigation.eventDetailScreen as eventsDetailScreen
import org.hogwarts.android.feature.events.navigation.eventCalendarScreen
import org.hogwarts.android.feature.library.navigation.LibraryBookDetail
import org.hogwarts.android.feature.library.navigation.LibraryCatalog
import org.hogwarts.android.feature.library.navigation.LibraryMyBorrowings
import org.hogwarts.android.feature.library.navigation.libraryCatalogScreen
import org.hogwarts.android.feature.library.navigation.bookDetailScreen as libraryBookDetailScreen
import org.hogwarts.android.feature.library.navigation.myBorrowingsScreen
import org.hogwarts.android.feature.subjects.navigation.Subjects
import org.hogwarts.android.feature.subjects.navigation.SubjectDetail
import org.hogwarts.android.feature.subjects.navigation.subjectsScreen as subjectsCatalogScreen
import org.hogwarts.android.feature.subjects.navigation.subjectDetailScreen
import org.hogwarts.android.feature.subjects.navigation.mySubjectsScreen
import org.hogwarts.android.feature.reportcards.navigation.ReportCardDetail
import org.hogwarts.android.feature.reportcards.navigation.ReportCardsProgress
import org.hogwarts.android.feature.reportcards.navigation.reportCardsListScreen
import org.hogwarts.android.feature.reportcards.navigation.reportCardDetailScreen
import org.hogwarts.android.feature.reportcards.navigation.progressChartsScreen
import org.hogwarts.android.feature.guardian.navigation.GuardianChildAttendance
import org.hogwarts.android.feature.teacher.navigation.TeacherClassDetail
import org.hogwarts.android.feature.teacher.navigation.TeacherClassAttendance
import org.hogwarts.android.feature.teacher.navigation.TeacherClassGrades
import org.hogwarts.android.feature.teacher.navigation.TeacherClassStudents
import org.hogwarts.android.feature.teacher.navigation.teacherClassesScreen
import org.hogwarts.android.feature.teacher.navigation.teacherClassDetailScreen
import org.hogwarts.android.feature.teacher.navigation.teacherBatchAttendanceScreen
import org.hogwarts.android.feature.teacher.navigation.teacherGradeEntryScreen
import org.hogwarts.android.feature.teacher.navigation.teacherStudentRosterScreen
import org.hogwarts.android.feature.teacher.navigation.teacherScheduleScreen
import org.hogwarts.android.feature.guardian.navigation.guardianChildAttendanceScreen
import org.hogwarts.android.feature.guardian.navigation.guardianChildFeesScreen
import org.hogwarts.android.feature.guardian.navigation.guardianChildGradesScreen
import org.hogwarts.android.feature.guardian.navigation.guardianChildTimetableScreen
import org.hogwarts.android.feature.guardian.navigation.guardianChildrenScreen
import org.hogwarts.android.feature.guardian.navigation.guardianMessagesScreen
import org.hogwarts.android.feature.guardian.navigation.guardianNotificationsScreen
import org.hogwarts.android.feature.timetable.navigation.Timetable
import org.hogwarts.android.feature.timetable.navigation.timetableScreen
import org.hogwarts.android.feature.stream.navigation.StreamCatalog
import org.hogwarts.android.feature.stream.navigation.StreamCourseDetail
import org.hogwarts.android.feature.stream.navigation.StreamChapters
import org.hogwarts.android.feature.stream.navigation.StreamHome
import org.hogwarts.android.feature.stream.navigation.StreamVideoLesson
import org.hogwarts.android.feature.stream.navigation.StreamTextLesson
import org.hogwarts.android.feature.stream.navigation.StreamQuiz
import org.hogwarts.android.feature.stream.navigation.StreamProgress
import org.hogwarts.android.feature.stream.navigation.StreamCertificate
import org.hogwarts.android.feature.stream.navigation.courseCatalogScreen
import org.hogwarts.android.feature.stream.navigation.courseDetailScreen
import org.hogwarts.android.feature.stream.navigation.chapterListScreen
import org.hogwarts.android.feature.stream.navigation.streamHomeScreen
import org.hogwarts.android.feature.stream.navigation.videoLessonScreen
import org.hogwarts.android.feature.stream.navigation.textLessonScreen
import org.hogwarts.android.feature.stream.navigation.lessonQuizScreen as streamLessonQuizScreen
import org.hogwarts.android.feature.stream.navigation.courseProgressScreen
import org.hogwarts.android.feature.stream.navigation.courseCertificateScreen
import org.hogwarts.android.feature.lessons.navigation.LessonDetail
import org.hogwarts.android.feature.lessons.navigation.LessonCurriculum
import org.hogwarts.android.feature.lessons.navigation.LessonResources
import org.hogwarts.android.feature.lessons.navigation.LessonPlanForm
import org.hogwarts.android.feature.lessons.navigation.LessonPlanFormEdit
import org.hogwarts.android.feature.lessons.navigation.lessonPlansScreen
import org.hogwarts.android.feature.lessons.navigation.lessonDetailScreen as lessonsDetailScreen
import org.hogwarts.android.feature.lessons.navigation.curriculumMapScreen
import org.hogwarts.android.feature.lessons.navigation.resourceBrowserScreen
import org.hogwarts.android.feature.lessons.navigation.lessonPlanFormScreen
import org.hogwarts.android.feature.lessons.navigation.lessonPlanFormEditScreen
import org.hogwarts.android.feature.admin.navigation.AdminDashboard
import org.hogwarts.android.feature.admin.navigation.AdminSchoolInfo
import org.hogwarts.android.feature.admin.navigation.AdminStaff
import org.hogwarts.android.feature.admin.navigation.AdminClassRoster
import org.hogwarts.android.feature.admin.navigation.AdminStats
import org.hogwarts.android.feature.admin.navigation.adminDashboardScreen
import org.hogwarts.android.feature.admin.navigation.schoolInfoScreen
import org.hogwarts.android.feature.admin.navigation.staffDirectoryScreen
import org.hogwarts.android.feature.admin.navigation.classRosterScreen
import org.hogwarts.android.feature.admin.navigation.classRosterDetailScreen
import org.hogwarts.android.feature.admin.navigation.schoolStatsScreen
import org.hogwarts.android.feature.idcard.navigation.IdCardWallet
import org.hogwarts.android.feature.idcard.navigation.IdCardPdf
import org.hogwarts.android.feature.idcard.navigation.digitalIdScreen
import org.hogwarts.android.feature.idcard.navigation.walletModeScreen
import org.hogwarts.android.feature.idcard.navigation.idCardPdfScreen
import org.hogwarts.android.feature.attendance.navigation.kioskModeScreen
import org.hogwarts.android.feature.guardian.navigation.meetingBookingScreen
import org.hogwarts.android.feature.guardian.navigation.consentFormsScreen
import org.hogwarts.android.feature.guardian.navigation.tripPermissionsScreen
import org.hogwarts.android.feature.guardian.navigation.communicationPreferencesScreen
import org.hogwarts.android.feature.quizgame.navigation.QuizGamePractice
import org.hogwarts.android.feature.quizgame.navigation.QuizGameTimed
import org.hogwarts.android.feature.quizgame.navigation.QuizGameLeaderboard
import org.hogwarts.android.feature.quizgame.navigation.QuizGameAchievements
import org.hogwarts.android.feature.quizgame.navigation.QuizGameTournament
import org.hogwarts.android.feature.quizgame.navigation.QuizGameSession
import org.hogwarts.android.feature.quizgame.navigation.gameHubScreen
import org.hogwarts.android.feature.quizgame.navigation.practiceModeScreen
import org.hogwarts.android.feature.quizgame.navigation.timedChallengeScreen
import org.hogwarts.android.feature.quizgame.navigation.quizLeaderboardScreen
import org.hogwarts.android.feature.quizgame.navigation.quizAchievementsScreen
import org.hogwarts.android.feature.quizgame.navigation.tournamentScreen
import org.hogwarts.android.feature.quizgame.navigation.quizSessionScreen
import org.hogwarts.android.core.sync.navigation.syncSettingsScreen
import org.hogwarts.android.core.sync.navigation.conflictResolutionScreen
import org.hogwarts.android.core.sync.navigation.syncProgressScreen
import org.hogwarts.android.core.designsystem.atom.AtomStudioRoute
import org.hogwarts.android.core.designsystem.atom.atomStudioScreen

/**
 * Main navigation host for Hogwarts Android.
 *
 * Handles navigation between auth and main app flows.
 */
@Composable
fun HogwartsNavHost(
    navController: NavHostController = rememberNavController(),
    isAuthenticated: Boolean,
    onLogout: () -> Unit,
    biometricHelper: BiometricHelper,
    tenantContext: TenantContext,
    modifier: Modifier = Modifier
) {
    val startDestination: Any = if (isAuthenticated) {
        Dashboard
    } else {
        AuthGraph
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth flow
        authNavGraph(
            navController = navController,
            onLoginSuccess = {
                navController.navigate(Dashboard) {
                    popUpTo<AuthGraph> { inclusive = true }
                }
            },
            biometricHelper = biometricHelper
        )

        // Main app flow - Dashboard with tab bar
        dashboardScreen(
            onNavigateToStudents = { navController.navigate(StudentsList) },
            onNavigateToAttendance = { navController.navigate(Attendance) },
            onNavigateToGrades = { navController.navigate(Grades) },
            onNavigateToFees = { navController.navigate(Fees) },
            onNavigateToTimetable = { navController.navigate(Timetable) },
            onNavigateToMessages = { navController.navigate(Messaging) },
            onNavigateToSettings = { navController.navigate(Settings) },
            onNavigateToStream = {
                // Students skip the marketing landing and drop straight into a
                // catalog locked to their grade. Other roles still see StreamHome.
                if (tenantContext.userRole == UserRole.STUDENT) {
                    navController.navigate(
                        StreamCatalog(
                            initialGrade = tenantContext.studentGrade,
                            lockGrade = true
                        )
                    )
                } else {
                    navController.navigate(StreamHome)
                }
            },
            onNavigateToSubjects = { navController.navigate(Subjects) },
            onNavigateToAtomStudio = { navController.navigate(AtomStudioRoute) },
            onNavigateToAnnouncements = { navController.navigate(Announcements) },
            onNavigateToLibrary = { navController.navigate(LibraryCatalog) },
            onNavigateToProfile = { navController.navigate(Profile) }
        )

        atomStudioScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Students
        studentsScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToStudent = { studentId ->
                navController.navigate(StudentDetail(studentId))
            }
        )

        // Student Detail
        studentDetailScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEdit = { studentId ->
                navController.navigate(StudentEdit(studentId))
            }
        )

        // Student Form (Create/Edit)
        studentFormScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Attendance
        attendanceScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Grades
        gradesScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Fees
        feesScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Fee Balance Dashboard
        feeBalanceDashboardScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToInvoices = { navController.navigate(FeeInvoices) },
            onNavigateToTransactions = { navController.navigate(FeeTransactions) },
            onNavigateToPayment = { navController.navigate(FeeInvoices) }
        )

        // Invoices
        invoiceListScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToInvoice = { invoiceId ->
                navController.navigate(FeeInvoiceDetail(invoiceId))
            },
            onNavigateToTransactions = {
                navController.navigate(FeeTransactions)
            }
        )

        // Invoice Detail
        invoiceDetailScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToPayment = { invoiceId ->
                navController.navigate(FeePayment(invoiceId))
            }
        )

        // Payment Processing
        paymentProcessingScreen(
            onNavigateBack = { navController.popBackStack() },
            onPaymentComplete = { transactionId ->
                navController.navigate(FeeReceipt(transactionId)) {
                    popUpTo<FeeInvoices> { inclusive = false }
                }
            }
        )

        // Payment Receipt
        paymentReceiptScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Transaction History
        transactionHistoryScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToReceipt = { transactionId ->
                navController.navigate(FeeReceipt(transactionId))
            }
        )

        // Timetable
        timetableScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Messaging
        messagingScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToChat = { conversationId ->
                navController.navigate(Chat(conversationId))
            },
            onNavigateToWhatsAppSettings = {
                navController.navigate(WhatsAppSettings)
            },
        )

        // Chat
        chatScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToInfo = { conversationId ->
                navController.navigate(ConversationInfo(conversationId))
            },
        )

        // Conversation info
        conversationInfoScreen(
            onNavigateBack = { navController.popBackStack() },
            onAfterExit = {
                navController.popBackStack(Messaging, inclusive = false)
            },
        )

        // Message search (global + per-conversation)
        messageSearchScreen(
            onNavigateBack = { navController.popBackStack() },
            onResultClick = { conversationId, _ ->
                navController.navigate(Chat(conversationId))
            },
        )

        // WhatsApp Settings
        whatsAppSettingsScreen(
            onDismiss = { navController.popBackStack() },
            onNavigateToQR = { navController.navigate(WhatsAppQR) },
        )

        // WhatsApp QR
        whatsAppQRScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Notifications
        notificationsScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToPreferences = { navController.navigate(NotificationPreferences) }
        )
        notificationPreferencesScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Settings
        settingsScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToProfile = { navController.navigate(Profile) },
            onLogout = {
                onLogout()
                navController.navigate(AuthGraph) {
                    popUpTo<Dashboard> { inclusive = true }
                }
            }
        )

        // Profile
        profileScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Exams
        examsScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToExam = { examId ->
                navController.navigate(ExamDetail(examId))
            }
        )

        // Exam Detail
        examDetailScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Quiz
        quizScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Announcements
        announcementsScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAnnouncement = { announcementId ->
                navController.navigate(AnnouncementDetail(announcementId))
            }
        )

        // Announcement Detail
        announcementDetailScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Guardian - Children List
        guardianChildrenScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToChild = { childId ->
                navController.navigate(GuardianChildAttendance(childId))
            }
        )

        // Guardian - Child Attendance
        guardianChildAttendanceScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Guardian - Child Grades
        guardianChildGradesScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Guardian - Child Fees
        guardianChildFeesScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Guardian - Child Timetable
        guardianChildTimetableScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Guardian - Messages
        guardianMessagesScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToChat = { teacherId ->
                navController.navigate(Chat(teacherId))
            }
        )

        // Guardian - Notifications
        guardianNotificationsScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Teacher - My Classes
        teacherClassesScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToClass = { classId ->
                navController.navigate(TeacherClassDetail(classId))
            }
        )

        // Teacher - Class Detail
        teacherClassDetailScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAttendance = { classId ->
                navController.navigate(TeacherClassAttendance(classId))
            },
            onNavigateToGrades = { classId ->
                navController.navigate(TeacherClassGrades(classId))
            },
            onNavigateToStudentRoster = { classId ->
                navController.navigate(TeacherClassStudents(classId))
            }
        )

        // Teacher - Batch Attendance
        teacherBatchAttendanceScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Teacher - Grade Entry
        teacherGradeEntryScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Teacher - Student Roster
        teacherStudentRosterScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToStudent = { studentId ->
                navController.navigate(StudentDetail(studentId))
            }
        )

        // Teacher - Schedule
        teacherScheduleScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToClass = { classId ->
                navController.navigate(TeacherClassDetail(classId))
            }
        )

        // Events - List
        eventsListScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEvent = { eventId ->
                navController.navigate(EventDetail(eventId))
            },
            onNavigateToCalendar = {
                navController.navigate(EventCalendar)
            }
        )

        // Events - Detail
        eventsDetailScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Events - Calendar
        eventCalendarScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEvent = { eventId ->
                navController.navigate(EventDetail(eventId))
            }
        )

        // Admission - Applications List
        admissionListScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToApplication = { applicationId ->
                navController.navigate(AdmissionStatus(applicationId))
            },
            onNavigateToNewApplication = {
                navController.navigate(AdmissionForm)
            }
        )

        // Admission - New Application Form
        admissionFormScreen(
            onNavigateBack = { navController.popBackStack() },
            onSubmitSuccess = { navController.popBackStack<AdmissionList>(inclusive = false) }
        )

        // Admission - Edit Application Form
        admissionFormEditScreen(
            onNavigateBack = { navController.popBackStack() },
            onSubmitSuccess = { navController.popBackStack<AdmissionList>(inclusive = false) }
        )

        // Admission - Status
        admissionStatusScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Library - Book Catalog
        libraryCatalogScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToBook = { bookId ->
                navController.navigate(LibraryBookDetail(bookId))
            },
            onNavigateToMyBorrowings = {
                navController.navigate(LibraryMyBorrowings)
            }
        )

        // Library - Book Detail
        libraryBookDetailScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Library - My Borrowings
        myBorrowingsScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Subjects - Catalog
        subjectsCatalogScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSubject = { subjectId ->
                navController.navigate(SubjectDetail(subjectId))
            }
        )

        // Subjects - Detail
        subjectDetailScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Subjects - My Subjects
        mySubjectsScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSubject = { subjectId ->
                navController.navigate(SubjectDetail(subjectId))
            }
        )

        // Report Cards - List
        reportCardsListScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToDetail = { reportCardId ->
                navController.navigate(ReportCardDetail(reportCardId))
            },
            onNavigateToProgress = {
                navController.navigate(ReportCardsProgress)
            }
        )

        // Report Cards - Detail
        reportCardDetailScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Report Cards - Progress Charts
        progressChartsScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // =====================================================
        // Phase 2C: Advanced Features
        // =====================================================

        // Advanced Attendance - Gamification (badges, streaks, points)
        gamificationScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Advanced Attendance - Hall Pass
        hallPassScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Advanced Attendance - Interventions
        interventionsScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Advanced Attendance - Analytics (heatmap, charts)
        attendanceAnalyticsScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Advanced Attendance - Method Settings
        attendanceMethodSettingsScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Advanced Exams - Online Exam Taking
        onlineExamScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToResults = { examId ->
                navController.navigate(ExamResults(examId))
            }
        )

        // Advanced Exams - Question Bank
        questionBankScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Advanced Exams - Detailed Results
        examResultsScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToCertificate = { examId ->
                navController.navigate(ExamCertificate(examId))
            }
        )

        // Advanced Exams - Certificate
        examCertificateScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Stream / LMS - Home (landing page, mirrors web /stream)
        streamHomeScreen(
            onNavigateToCourses = { navController.navigate(StreamCatalog()) },
            onNavigateToMyLearning = { navController.navigate(StreamCatalog()) },
            onNavigateToCourse = { courseId ->
                navController.navigate(StreamCourseDetail(courseId))
            }
        )

        // Stream / LMS - Course Catalog
        courseCatalogScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToCourse = { courseId ->
                navController.navigate(StreamCourseDetail(courseId))
            }
        )

        // Stream / LMS - Course Detail
        courseDetailScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToChapters = { courseId ->
                navController.navigate(StreamChapters(courseId))
            },
            onNavigateToVideoLesson = { courseId, lessonId ->
                navController.navigate(StreamVideoLesson(courseId, lessonId))
            },
            onNavigateToTextLesson = { courseId, lessonId ->
                navController.navigate(StreamTextLesson(courseId, lessonId))
            },
            onNavigateToQuiz = { courseId, lessonId ->
                navController.navigate(StreamQuiz(courseId, lessonId))
            },
            onNavigateToProgress = { courseId ->
                navController.navigate(StreamProgress(courseId))
            },
            onNavigateToCertificate = { courseId ->
                navController.navigate(StreamCertificate(courseId))
            }
        )

        // Stream / LMS - Chapter List
        chapterListScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToVideoLesson = { courseId, lessonId ->
                navController.navigate(StreamVideoLesson(courseId, lessonId))
            },
            onNavigateToTextLesson = { courseId, lessonId ->
                navController.navigate(StreamTextLesson(courseId, lessonId))
            },
            onNavigateToQuiz = { courseId, lessonId ->
                navController.navigate(StreamQuiz(courseId, lessonId))
            }
        )

        // Stream / LMS - Video Lesson
        videoLessonScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToNext = { courseId, lessonId ->
                navController.navigate(StreamVideoLesson(courseId, lessonId))
            }
        )

        // Stream / LMS - Text Lesson
        textLessonScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToNext = { courseId, lessonId ->
                navController.navigate(StreamTextLesson(courseId, lessonId))
            }
        )

        // Stream / LMS - Lesson Quiz
        streamLessonQuizScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToNext = { courseId, lessonId ->
                navController.navigate(StreamTextLesson(courseId, lessonId))
            }
        )

        // Stream / LMS - Course Progress
        courseProgressScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Stream / LMS - Course Certificate
        courseCertificateScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Lessons - Lesson Plans List
        lessonPlansScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToLesson = { lessonId ->
                navController.navigate(LessonDetail(lessonId))
            },
            onNavigateToCurriculum = {
                navController.navigate(LessonCurriculum)
            },
            onNavigateToResources = {
                navController.navigate(LessonResources)
            },
            onNavigateToForm = {
                navController.navigate(LessonPlanForm)
            }
        )

        // Lessons - Lesson Detail
        lessonsDetailScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEdit = { lessonId ->
                navController.navigate(LessonPlanFormEdit(lessonId))
            }
        )

        // Lessons - Curriculum Map
        curriculumMapScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Lessons - Resource Browser
        resourceBrowserScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Lessons - New Lesson Plan Form
        lessonPlanFormScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Lessons - Edit Lesson Plan Form
        lessonPlanFormEditScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Admin - Dashboard
        adminDashboardScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSchoolInfo = {
                navController.navigate(AdminSchoolInfo)
            },
            onNavigateToStaff = {
                navController.navigate(AdminStaff)
            },
            onNavigateToClassRoster = {
                navController.navigate(AdminClassRoster)
            },
            onNavigateToStats = {
                navController.navigate(AdminStats)
            }
        )

        // Admin - School Info
        schoolInfoScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Admin - Staff Directory
        staffDirectoryScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Admin - Class Roster
        classRosterScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Admin - Class Roster Detail
        classRosterDetailScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Admin - School Stats
        schoolStatsScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // ID Card - Digital ID
        digitalIdScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToWalletMode = {
                navController.navigate(IdCardWallet)
            },
            onNavigateToPdf = {
                navController.navigate(IdCardPdf)
            }
        )

        // ID Card - Wallet Mode
        walletModeScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // ID Card - PDF Export
        idCardPdfScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // =====================================================
        // Phase 2D: Polish & Nice-to-have
        // =====================================================

        // Quiz Game - Game Hub
        gameHubScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToPractice = { navController.navigate(QuizGamePractice) },
            onNavigateToTimed = { navController.navigate(QuizGameTimed) },
            onNavigateToLeaderboard = { navController.navigate(QuizGameLeaderboard) },
            onNavigateToAchievements = { navController.navigate(QuizGameAchievements) },
            onNavigateToTournament = { navController.navigate(QuizGameTournament) }
        )

        // Quiz Game - Practice Mode
        practiceModeScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSession = { sessionId ->
                navController.navigate(QuizGameSession(sessionId))
            }
        )

        // Quiz Game - Timed Challenge
        timedChallengeScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSession = { sessionId ->
                navController.navigate(QuizGameSession(sessionId))
            }
        )

        // Quiz Game - Leaderboard
        quizLeaderboardScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Quiz Game - Achievements
        quizAchievementsScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Quiz Game - Tournament
        tournamentScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Quiz Game - Session
        quizSessionScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Attendance Kiosk Mode
        kioskModeScreen(
            onExitKiosk = { navController.popBackStack() }
        )

        // Parent Portal - Meeting Booking
        meetingBookingScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Parent Portal - Consent Forms
        consentFormsScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Parent Portal - Trip Permissions
        tripPermissionsScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Parent Portal - Communication Preferences
        communicationPreferencesScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Sync - Settings
        syncSettingsScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Sync - Conflict Resolution
        conflictResolutionScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Sync - Progress
        syncProgressScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
