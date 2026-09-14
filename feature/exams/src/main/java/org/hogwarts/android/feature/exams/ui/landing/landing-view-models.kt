package org.hogwarts.android.feature.exams.ui.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.exams.data.repository.ExamsRepository
import org.hogwarts.android.feature.exams.di.ExamsClock
import org.hogwarts.android.feature.exams.domain.model.Child
import org.hogwarts.android.feature.exams.domain.model.Exam
import org.hogwarts.android.feature.exams.domain.model.GradeRow
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

/*
 * One ViewModel per landing in `exams/content.tsx`. Every figure is nullable:
 * null means the mobile API has no route for it (or refused this role), and
 * the screen shows the web's own "—" instead of inventing a number.
 */

private suspend fun <T> attempt(block: suspend () -> T): T? = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    null
}

private const val UPCOMING_PAGE = 100

/** Web: `status in [PLANNED, IN_PROGRESS]`, soonest first, from the server's `upcoming=true` list. */
private suspend fun ExamsRepository.openUpcoming(): Pair<List<Exam>, Int> {
    val page = exams(upcoming = true, perPage = UPCOMING_PAGE)
    val open = page.items.filter { it.isOpen }.sortedBy { it.examDate ?: Instant.MAX }
    val count = if (page.total <= page.items.size) open.size else page.total
    return open to count
}

private fun Clock.startOfToday(): Instant = LocalDate.now(this).atStartOfDay(zone).toInstant()

// ---- Admin (ADMIN, DEVELOPER, ACCOUNTANT, STAFF, anyone else) ---------------

data class AdminLandingUiState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val nextExam: Exam? = null,
    val total: Int? = null,
    val upcoming: Int? = null,
    val questionBank: Int? = null,
    val students: Int? = null,
    val completed: Int? = null,
    val pendingMarking: Int? = null,
) {
    /** `Math.round(completed / total * 100)`, 0 without exams. */
    val completionRate: Int?
        get() = if (total == null || completed == null) null else if (total > 0) (completed * 100.0 / total).roundToInt() else 0

    /** `completed / (pending + completed)`, 100 when nothing is left to mark. */
    val markingProgress: Int?
        get() {
            if (pendingMarking == null || completed == null) return null
            val toMark = pendingMarking + completed
            return if (toMark > 0) (completed * 100.0 / toMark).roundToInt() else 100
        }
}

@HiltViewModel
class AdminLandingViewModel @Inject constructor(
    private val repository: ExamsRepository,
    @ExamsClock private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminLandingUiState())
    val uiState: StateFlow<AdminLandingUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, loadFailed = false) }
        viewModelScope.launch {
            val today = clock.startOfToday()
            val next = coroutineScope {
                val total = async { attempt { repository.exams(perPage = 1).total } }
                val upcoming = async { attempt { repository.openUpcoming() } }
                val completed = async { attempt { repository.exams(status = "COMPLETED", perPage = 1).total } }
                val pending = async {
                    attempt {
                        repository.exams(status = "IN_PROGRESS", perPage = UPCOMING_PAGE).items
                            .count { exam -> exam.examDate?.isBefore(today) == true }
                    }
                }
                val questions = async { attempt { repository.questionBank(perPage = 1).total } }
                val students = async { attempt { repository.totalStudents() } }
                AdminLandingUiState(
                    loading = false,
                    total = total.await(),
                    nextExam = upcoming.await()?.first?.firstOrNull(),
                    upcoming = upcoming.await()?.second,
                    completed = completed.await(),
                    pendingMarking = pending.await(),
                    questionBank = questions.await(),
                    students = students.await(),
                )
            }
            val nothing = next.total == null && next.upcoming == null && next.completed == null && next.pendingMarking == null
            _uiState.value = next.copy(loadFailed = nothing)
        }
    }
}

// ---- Teacher ----------------------------------------------------------------

data class TeacherLandingUiState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val noRecord: Boolean = false,
    /** Distinct sections the teacher is timetabled in. */
    val classes: Int? = null,
    val students: Int? = null,
)

@HiltViewModel
class TeacherLandingViewModel @Inject constructor(
    private val repository: ExamsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeacherLandingUiState())
    val uiState: StateFlow<TeacherLandingUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, loadFailed = false) }
        viewModelScope.launch {
            val profile = attempt { repository.profile() }
            if (profile == null) {
                _uiState.value = TeacherLandingUiState(loading = false, loadFailed = true)
                return@launch
            }
            if (profile.teacherId == null) {
                _uiState.value = TeacherLandingUiState(loading = false, noRecord = true)
                return@launch
            }
            val sections = attempt { repository.teacherClasses().distinctBy { it.sectionId } }
            _uiState.value = TeacherLandingUiState(
                loading = false,
                classes = sections?.size,
                students = sections?.sumOf { it.studentCount },
            )
        }
    }
}

// ---- Student ----------------------------------------------------------------

data class StudentLandingUiState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val noRecord: Boolean = false,
    val today: LocalDate = LocalDate.MIN,
    val upcoming: List<Exam> = emptyList(),
    val results: List<GradeRow> = emptyList(),
)

@HiltViewModel
class StudentLandingViewModel @Inject constructor(
    private val repository: ExamsRepository,
    @ExamsClock private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StudentLandingUiState(today = LocalDate.now(clock)))
    val uiState: StateFlow<StudentLandingUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, loadFailed = false, today = LocalDate.now(clock)) }
        viewModelScope.launch {
            val profile = attempt { repository.profile() }
            val studentId = profile?.studentId
            if (profile == null || studentId == null) {
                _uiState.update { it.copy(loading = false, loadFailed = profile == null, noRecord = profile != null) }
                return@launch
            }
            coroutineScope {
                val upcoming = async { attempt { repository.openUpcoming().first.take(10) } }
                val results = async { attempt { repository.studentGrades(studentId, perPage = 10).take(10) } }
                val u = upcoming.await()
                val r = results.await()
                _uiState.update {
                    it.copy(
                        loading = false,
                        loadFailed = u == null && r == null,
                        upcoming = u.orEmpty(),
                        results = r.orEmpty(),
                    )
                }
            }
        }
    }
}

// ---- Guardian ---------------------------------------------------------------

data class GuardianResult(val row: GradeRow, val childId: String, val childName: String)

data class GuardianLandingUiState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val today: LocalDate = LocalDate.MIN,
    val children: List<Child> = emptyList(),
    val upcoming: List<Exam> = emptyList(),
    /** The 15 most recent results across the children, newest first. */
    val results: List<GuardianResult> = emptyList(),
) {
    val noChildren: Boolean get() = !loading && !loadFailed && children.isEmpty()

    /** The child's average over [results], as `averageOf` computes it; null without rows. */
    fun averageOf(childId: String): Int? {
        val rows = results.filter { it.childId == childId }.mapNotNull { it.row.percentage }
        return if (rows.isEmpty()) null else (rows.sum() / rows.size).roundToInt()
    }
}

@HiltViewModel
class GuardianLandingViewModel @Inject constructor(
    private val repository: ExamsRepository,
    @ExamsClock private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GuardianLandingUiState(today = LocalDate.now(clock)))
    val uiState: StateFlow<GuardianLandingUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, loadFailed = false, today = LocalDate.now(clock)) }
        viewModelScope.launch {
            val children = attempt { repository.children() }
            if (children == null) {
                _uiState.update { it.copy(loading = false, loadFailed = true) }
                return@launch
            }
            if (children.isEmpty()) {
                _uiState.update { it.copy(loading = false, children = emptyList()) }
                return@launch
            }
            coroutineScope {
                val upcoming = async { attempt { repository.openUpcoming().first.take(10) } }
                val grades = children.map { child ->
                    async { attempt { repository.childGrades(child.id, perPage = 15).map { GuardianResult(it, child.id, child.name) } } }
                }
                val results = grades.mapNotNull { it.await() }.flatten()
                    .sortedByDescending { it.row.gradedAt ?: Instant.MIN }
                    .take(15)
                _uiState.update {
                    it.copy(
                        loading = false,
                        children = children,
                        upcoming = upcoming.await().orEmpty(),
                        results = results,
                    )
                }
            }
        }
    }
}
