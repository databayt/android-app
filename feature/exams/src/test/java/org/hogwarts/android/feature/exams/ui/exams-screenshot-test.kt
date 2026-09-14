package org.hogwarts.android.feature.exams.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.captureRoboImage
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.exams.domain.model.Child
import org.hogwarts.android.feature.exams.domain.model.OnlineExam
import org.hogwarts.android.feature.exams.domain.model.OnlineQuestion
import org.hogwarts.android.feature.exams.domain.model.OwnResult
import org.hogwarts.android.feature.exams.domain.model.Question
import org.hogwarts.android.feature.exams.testing.exam
import org.hogwarts.android.feature.exams.testing.grade
import org.hogwarts.android.feature.exams.ui.detail.ExamDetailUiState
import org.hogwarts.android.feature.exams.ui.detail.ExamDetailView
import org.hogwarts.android.feature.exams.ui.landing.AdminLandingUiState
import org.hogwarts.android.feature.exams.ui.landing.AdminLandingView
import org.hogwarts.android.feature.exams.ui.landing.GuardianLandingUiState
import org.hogwarts.android.feature.exams.ui.landing.GuardianLandingView
import org.hogwarts.android.feature.exams.ui.landing.GuardianResult
import org.hogwarts.android.feature.exams.ui.landing.StudentLandingUiState
import org.hogwarts.android.feature.exams.ui.landing.StudentLandingView
import org.hogwarts.android.feature.exams.ui.landing.TeacherLandingUiState
import org.hogwarts.android.feature.exams.ui.landing.TeacherLandingView
import org.hogwarts.android.feature.exams.ui.qbank.QuestionBankUiState
import org.hogwarts.android.feature.exams.ui.qbank.QuestionBankView
import org.hogwarts.android.feature.exams.ui.take.OnlineExamUiState
import org.hogwarts.android.feature.exams.ui.take.OnlineExamView
import org.hogwarts.android.feature.exams.ui.upcoming.UpcomingUiState
import org.hogwarts.android.feature.exams.ui.upcoming.UpcomingView
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.util.TimeZone

/**
 * Each role's `/exams` landing and the native sub-pages at phone width.
 * Record with `./gradlew :feature:exams:recordRoborazziDebug`; goldens live in
 * src/test/screenshots. All data is invented.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExamsScreenshotTest {

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h1900dp-xxhdpi")
    fun admin_en_light() = capture("admin_en_light") { Admin() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1900dp-xxhdpi")
    fun admin_ar_light() = capture("admin_ar_light") { Admin() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1900dp-xxhdpi")
    fun admin_ar_dark() = capture("admin_ar_dark", dark = true) { Admin() }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h844dp-xxhdpi")
    fun teacher_en_light() = capture("teacher_en_light") { Teacher() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h844dp-xxhdpi")
    fun teacher_ar_light() = capture("teacher_ar_light") { Teacher() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h844dp-xxhdpi")
    fun teacher_ar_dark() = capture("teacher_ar_dark", dark = true) { Teacher() }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h1400dp-xxhdpi")
    fun student_en_light() = capture("student_en_light") { Student() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1400dp-xxhdpi")
    fun student_ar_light() = capture("student_ar_light") { Student() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1400dp-xxhdpi")
    fun student_ar_dark() = capture("student_ar_dark", dark = true) { Student() }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h1400dp-xxhdpi")
    fun guardian_en_light() = capture("guardian_en_light") { Guardian() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1400dp-xxhdpi")
    fun guardian_ar_light() = capture("guardian_ar_light") { Guardian() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1400dp-xxhdpi")
    fun guardian_ar_dark() = capture("guardian_ar_dark", dark = true) { Guardian() }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h1100dp-xxhdpi")
    fun upcoming_en_light() = capture("upcoming_en_light") { Upcoming() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1100dp-xxhdpi")
    fun upcoming_ar_light() = capture("upcoming_ar_light") { Upcoming() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1000dp-xxhdpi")
    fun detail_student_ar_light() = capture("detail_student_ar_light") { Detail() }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h1000dp-xxhdpi")
    fun detail_student_en_light() = capture("detail_student_en_light") { Detail() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1000dp-xxhdpi")
    fun qbank_ar_light() = capture("qbank_ar_light") { Qbank() }

    @Test @Config(sdk = [35], qualifiers = "ar-ldrtl-w390dp-h1200dp-xxhdpi")
    fun take_ar_light() = capture("take_ar_light") { Take() }

    @Test @Config(sdk = [35], qualifiers = "en-w390dp-h1200dp-xxhdpi")
    fun take_en_light() = capture("take_en_light") { Take() }

    /** `ar` resolves values-ar and Arabic digits; the direction is forced (library manifests carry no supportsRtl). */
    private fun capture(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        val rtl = name.contains("_ar_")
        captureRoboImage("src/test/screenshots/$name.png") {
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                HogwartsTheme(darkTheme = dark) { content() }
            }
        }
    }
}

private val TODAY = LocalDate.of(2026, 9, 14)

private fun links(role: UserRole) = ExamsLinks(role, onNavigate = {}, onOpenHref = {})

private fun tabs(role: UserRole, selected: ExamsTab = ExamsTab.Overview): @Composable () -> Unit =
    { ExamsTabs(role, selected, links(role)) }

@Composable
private fun Admin() = AdminLandingView(
    state = AdminLandingUiState(
        loading = false,
        nextExam = exam("ex-1", "Term One Midterm", "2026-09-16T06:00:00Z", subject = "Mathematics"),
        total = 48,
        upcoming = 6,
        questionBank = 320,
        students = 412,
        completed = 30,
        pendingMarking = 3,
    ),
    tabs = tabs(UserRole.ADMIN),
    links = links(UserRole.ADMIN),
    onOpenExam = {},
    onRetry = {},
)

@Composable
private fun Teacher() = TeacherLandingView(
    state = TeacherLandingUiState(loading = false, classes = 3, students = 84),
    tabs = tabs(UserRole.TEACHER),
    links = links(UserRole.TEACHER),
    onRetry = {},
)

@Composable
private fun Student() = StudentLandingView(
    state = StudentLandingUiState(
        loading = false,
        today = TODAY,
        upcoming = listOf(
            exam("ex-1", "Science Quiz", "2026-09-14T06:00:00Z", subject = "Science", start = "09:00"),
            exam("ex-2", "Algebra Test", "2026-09-15T06:00:00Z", subject = "Mathematics", start = "10:30"),
            exam("ex-3", "Reading Check", "2026-09-19T06:00:00Z", subject = "Language", start = null),
        ),
        results = listOf(
            grade("g-1", "Unit 1 Test", 92.0, "2026-09-08T10:00:00Z", subject = "Mathematics"),
            grade("g-2", "Lab Report", 64.0, "2026-09-02T10:00:00Z", subject = "Science"),
            grade("g-3", "Spelling", 38.0, "2026-08-28T10:00:00Z", subject = "Language"),
        ),
    ),
    tabs = tabs(UserRole.STUDENT),
    links = links(UserRole.STUDENT),
    onRetry = {},
)

@Composable
private fun Guardian() {
    val one = Child("c-1", "Sami Noor")
    val two = Child("c-2", "Lina Noor")
    GuardianLandingView(
        state = GuardianLandingUiState(
            loading = false,
            today = TODAY,
            children = listOf(one, two),
            upcoming = listOf(
                exam("ex-1", "Science Quiz", "2026-09-14T06:00:00Z", subject = "Science", start = "09:00"),
                exam("ex-2", "Algebra Test", "2026-09-15T06:00:00Z", subject = "Mathematics", start = "10:30"),
            ),
            results = listOf(
                GuardianResult(grade("g-1", "Unit 1 Test", 88.0, "2026-09-08T10:00:00Z", subject = "Mathematics"), one.id, one.name),
                GuardianResult(grade("g-2", "Lab Report", 45.0, "2026-09-02T10:00:00Z", subject = "Science"), two.id, two.name),
            ),
        ),
        tabs = tabs(UserRole.GUARDIAN),
        links = links(UserRole.GUARDIAN),
        onRetry = {},
    )
}

@Composable
private fun Upcoming() = UpcomingView(
    state = UpcomingUiState(
        loading = false,
        today = TODAY,
        exams = listOf(
            exam("ex-1", "Science Quiz", "2026-09-14T06:00:00Z", subject = "Science", start = "09:00"),
            exam("ex-2", "Algebra Test", "2026-09-15T06:00:00Z", subject = "Mathematics", start = "10:30"),
            exam("ex-3", "Reading Check", "2026-09-18T06:00:00Z", subject = "Language"),
            exam("ex-4", "Term Final", "2026-10-02T06:00:00Z", subject = "History"),
        ),
    ),
    role = UserRole.TEACHER,
    tabs = tabs(UserRole.TEACHER, ExamsTab.Upcoming),
    onOpenExam = {},
    onSchedule = {},
    onRetry = {},
)

@Composable
private fun Detail() = ExamDetailView(
    state = ExamDetailUiState(
        loading = false,
        role = UserRole.STUDENT,
        exam = exam("ex-1", "Algebra Test", "2026-09-15T06:00:00Z", status = "COMPLETED", subject = "Mathematics", start = "10:30")
            .copy(description = "Chapters one to three.", instructions = "Show your working for every answer."),
        result = OwnResult(score = 42.0, maxScore = 50.0, percentage = 84.0, grade = "A"),
    ),
    tabs = tabs(UserRole.STUDENT),
    onTake = {},
    onEdit = {},
    onRetry = {},
)

@Composable
private fun Qbank() = QuestionBankView(
    state = QuestionBankUiState(
        loading = false,
        total = 64,
        questions = listOf(
            Question("q-1", "What is the value of x when 2x + 4 = 10?", "SHORT_ANSWER", "EASY", 2.0, "Mathematics"),
            Question("q-2", "Plants make food through photosynthesis.", "TRUE_FALSE", "MEDIUM", 1.0, "Science"),
            Question("q-3", "Choose the correct past tense of the verb.", "MULTIPLE_CHOICE", "HARD", 1.5, "Language"),
        ),
    ),
    tabs = tabs(UserRole.TEACHER, ExamsTab.Qbank),
    onAdd = {},
    onOpenQuestion = {},
    onMore = {},
    onRetry = {},
)

@Composable
private fun Take() = OnlineExamView(
    state = OnlineExamUiState(
        starting = false,
        exam = OnlineExam(
            sessionId = "sess-1",
            examId = "ex-1",
            title = "Science Quiz",
            durationMinutes = 20,
            totalMarks = 4,
            instructions = "Answer every question before you submit.",
            secondsRemaining = 1_140,
            questions = listOf(
                OnlineQuestion("q-1", "Which planet is known as the red planet?", "MULTIPLE_CHOICE", listOf("Planet one", "Planet two", "Planet three"), 1.0),
                OnlineQuestion("q-2", "Water boils at 100 degrees at sea level.", "TRUE_FALSE", emptyList(), 1.0),
                OnlineQuestion("q-3", "Name one source of renewable energy.", "SHORT_ANSWER", emptyList(), 2.0),
            ),
        ),
        secondsLeft = 1_140,
        answers = mapOf("q-1" to "Planet two"),
    ),
    onAnswer = { _, _ -> },
    onAskSubmit = {},
    onKeepGoing = {},
    onSubmit = {},
    onRetry = {},
    onDone = {},
)
