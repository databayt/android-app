package org.hogwarts.android.navigation

import androidx.compose.runtime.Composable
import org.hogwarts.android.shell.LocalHrefOpener
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
import org.hogwarts.android.feature.live.navigation.liveHomeScreen
import org.hogwarts.android.shell.search.searchScreen
import org.hogwarts.android.feature.dashboard.navigation.dashboardScreen
import org.hogwarts.android.feature.fees.navigation.Fees
import org.hogwarts.android.feature.grades.navigation.Grades
import org.hogwarts.android.feature.announcements.navigation.AnnouncementDetail
import org.hogwarts.android.feature.announcements.navigation.Announcements
import org.hogwarts.android.feature.announcements.navigation.announcementsGraph
import org.hogwarts.android.feature.exams.navigation.examsGraph
import org.hogwarts.android.feature.fees.navigation.feesGraph
import org.hogwarts.android.feature.grades.navigation.gradesScreen
import org.hogwarts.android.feature.messaging.navigation.Chat
import org.hogwarts.android.feature.messaging.navigation.Messaging
import org.hogwarts.android.feature.messaging.navigation.messagesGraph
import org.hogwarts.android.feature.notifications.navigation.NotificationPreferences
import org.hogwarts.android.feature.notifications.navigation.Notifications
import org.hogwarts.android.feature.notifications.navigation.notificationsGraph
import org.hogwarts.android.feature.profile.navigation.Profile
import org.hogwarts.android.feature.profile.navigation.profileScreen
import org.hogwarts.android.feature.settings.navigation.Settings
import org.hogwarts.android.feature.settings.navigation.settingsGraph
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
import org.hogwarts.android.feature.timetable.navigation.timetableGraph
import org.hogwarts.android.feature.lumos.navigation.LumosCatalog
import org.hogwarts.android.feature.lumos.navigation.LumosCourseDetail
import org.hogwarts.android.feature.lumos.navigation.LumosChapters
import org.hogwarts.android.feature.lumos.navigation.LumosHome
import org.hogwarts.android.feature.lumos.navigation.LumosVideoLesson
import org.hogwarts.android.feature.lumos.navigation.LumosTextLesson
import org.hogwarts.android.feature.lumos.navigation.LumosQuiz
import org.hogwarts.android.feature.lumos.navigation.LumosProgress
import org.hogwarts.android.feature.lumos.navigation.LumosCertificate
import org.hogwarts.android.feature.lumos.navigation.LumosTeacherVideos
import org.hogwarts.android.feature.lumos.navigation.LumosAdminReview
import org.hogwarts.android.feature.lumos.navigation.courseCatalogScreen
import org.hogwarts.android.feature.lumos.navigation.courseDetailScreen
import org.hogwarts.android.feature.lumos.navigation.chapterListScreen
import org.hogwarts.android.feature.lumos.navigation.lumosHomeScreen
import org.hogwarts.android.feature.lumos.navigation.videoLessonScreen
import org.hogwarts.android.feature.lumos.navigation.textLessonScreen
import org.hogwarts.android.feature.lumos.navigation.lessonQuizScreen as lumosLessonQuizScreen
import org.hogwarts.android.feature.lumos.navigation.courseProgressScreen
import org.hogwarts.android.feature.lumos.navigation.courseCertificateScreen
import org.hogwarts.android.feature.lumos.navigation.lumosTeacherVideosScreen
import org.hogwarts.android.feature.lumos.navigation.lumosAdminReviewScreen
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
    val hrefOpener = LocalHrefOpener.current
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

        // Main app flow: the phone dashboard. Its doors are web paths the shell resolves.
        dashboardScreen(onOpenHref = { href -> hrefOpener.open(href) })

        // The live landing: what is on now, and what was.
        liveHomeScreen(onOpenHref = { href -> hrefOpener.open(href) })

        // The menu's first control. Its rows are web paths too, resolved the
        // same way — a native screen where there is one, the site otherwise.
        searchScreen(
            onOpen = { href ->
                navController.popBackStack()
                hrefOpener.open(href)
            },
            onBack = { navController.popBackStack() },
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
            onNavigateBack = { navController.popBackStack() },
            onNavigate = { route -> navController.navigate(route) },
            onOpenMessages = { navController.navigate(Messaging) },
        )

        // Grades
        gradesScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Finance (web /finance): family money or the finance hub; deeper pages are web paths.
        feesGraph(onOpenHref = { href -> hrefOpener.open(href) })

        // Timetable (web /timetable): the role's view; admin sub-pages and live rooms are web paths.
        timetableGraph(onOpenHref = { href -> hrefOpener.open(href) })

        // Messages: the five-tab shell and its threads (web /messages)
        messagesGraph(
            onOpenChat = { conversationId -> navController.navigate(Chat(conversationId)) },
            onCloseChat = { navController.popBackStack() },
            onOpenDashboard = { hrefOpener.open("/dashboard") },
            onOpenHref = { href -> hrefOpener.open(href) },
        )

        // Notifications (web /notifications, /unread, /preferences)
        notificationsGraph(
            onOpenHref = { href -> hrefOpener.open(href) },
            onNavigate = { route -> navController.navigate(route) { launchSingleTop = true } },
        )

        // Settings (web /settings)
        settingsGraph(
            onOpenHref = { href -> hrefOpener.open(href) },
            onOpenNotificationPreferences = { navController.navigate(NotificationPreferences) { launchSingleTop = true } },
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

        // Exams: role landings, upcoming, detail, question bank, online exam (web /exams)
        examsGraph(
            onNavigate = { route -> navController.navigate(route) { launchSingleTop = true } },
            onOpenHref = { href -> hrefOpener.open(href) },
            onBack = { navController.popBackStack() },
        )

        // Announcements (list + reading page)
        announcementsGraph(
            onOpenAnnouncement = { announcementId ->
                navController.navigate(AnnouncementDetail(announcementId))
            },
            onOpenHref = { href -> hrefOpener.open(href) },
            onBack = { navController.popBackStack() },
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
            },
            // The catalog and contribute entries in the level strip are web
            // pages with no native mirror; the shell resolves them.
            onOpenHref = { href -> hrefOpener.open(href) },
            role = tenantContext.userRole,
        )

        // Subjects - Detail
        subjectDetailScreen(
            onNavigateBack = { navController.popBackStack() },
            onOpenHref = { href -> hrefOpener.open(href) },
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

        // Lumos / LMS - Home (landing page, mirrors web /lumos)
        lumosHomeScreen(
            onNavigateToCourses = { navController.navigate(LumosCatalog()) },
            onNavigateToMyLearning = { navController.navigate(LumosCatalog()) },
            onNavigateToCourse = { courseId ->
                navController.navigate(LumosCourseDetail(courseId))
            },
            onNavigateToTeacherVideos = {
                navController.navigate(LumosTeacherVideos)
            }
        )

        // Lumos / LMS - Course Catalog
        courseCatalogScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToCourse = { courseId ->
                navController.navigate(LumosCourseDetail(courseId))
            }
        )

        // Lumos / LMS - Course Detail
        courseDetailScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToChapters = { courseId ->
                navController.navigate(LumosChapters(courseId))
            },
            onNavigateToVideoLesson = { courseId, lessonId ->
                navController.navigate(LumosVideoLesson(courseId, lessonId))
            },
            onNavigateToTextLesson = { courseId, lessonId ->
                navController.navigate(LumosTextLesson(courseId, lessonId))
            },
            onNavigateToQuiz = { courseId, lessonId ->
                navController.navigate(LumosQuiz(courseId, lessonId))
            },
            onNavigateToProgress = { courseId ->
                navController.navigate(LumosProgress(courseId))
            },
            onNavigateToCertificate = { courseId ->
                navController.navigate(LumosCertificate(courseId))
            }
        )

        // Lumos / LMS - Chapter List
        chapterListScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToVideoLesson = { courseId, lessonId ->
                navController.navigate(LumosVideoLesson(courseId, lessonId))
            },
            onNavigateToTextLesson = { courseId, lessonId ->
                navController.navigate(LumosTextLesson(courseId, lessonId))
            },
            onNavigateToQuiz = { courseId, lessonId ->
                navController.navigate(LumosQuiz(courseId, lessonId))
            }
        )

        // Lumos / LMS - Video Lesson
        videoLessonScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToNext = { courseId, lessonId ->
                navController.navigate(LumosVideoLesson(courseId, lessonId))
            }
        )

        // Lumos / LMS - Text Lesson
        textLessonScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToNext = { courseId, lessonId ->
                navController.navigate(LumosTextLesson(courseId, lessonId))
            }
        )

        // Lumos / LMS - Lesson Quiz
        lumosLessonQuizScreen(
            onNavigateBack = { navController.popBackStack() },
            onQuizCompleted = { navController.popBackStack() }
        )

        // Lumos / LMS - Course Progress
        courseProgressScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Lumos / LMS - Course Certificate
        courseCertificateScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Lumos / LMS - Teacher Videos
        lumosTeacherVideosScreen(
            onNavigateBack = { navController.popBackStack() }
        )

        // Lumos / LMS - Admin Review
        lumosAdminReviewScreen(
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
