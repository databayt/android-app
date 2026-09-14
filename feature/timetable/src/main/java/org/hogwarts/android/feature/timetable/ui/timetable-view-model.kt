package org.hogwarts.android.feature.timetable.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.timetable.data.remote.dto.TimetableBundle
import org.hogwarts.android.feature.timetable.data.repository.TimetableRepository
import org.hogwarts.android.feature.timetable.data.repository.TimetableResult
import org.hogwarts.android.feature.timetable.data.repository.TimetableScope
import org.hogwarts.android.feature.timetable.di.TimetableClock
import org.hogwarts.android.feature.timetable.domain.model.Child
import org.hogwarts.android.feature.timetable.domain.model.RangeMode
import org.hogwarts.android.feature.timetable.domain.model.Slot
import org.hogwarts.android.feature.timetable.domain.model.WeekTimetable
import org.hogwarts.android.feature.timetable.domain.model.toChild
import org.hogwarts.android.feature.timetable.domain.model.toWeek
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

/** Which view `role-router.tsx` renders for a role (`getPersonalizedTimetable`'s `viewType`). */
enum class TimetableSurface { Student, Teacher, Guardian, Admin }

fun surfaceFor(role: UserRole?): TimetableSurface = when (role) {
    UserRole.ADMIN, UserRole.DEVELOPER, UserRole.ACCOUNTANT, UserRole.STAFF -> TimetableSurface.Admin
    UserRole.TEACHER -> TimetableSurface.Teacher
    UserRole.GUARDIAN -> TimetableSurface.Guardian
    else -> TimetableSurface.Student
}

/** The section's tabs — `timetable/layout.tsx`. [href] is set for pages the app hands to the web. */
enum class TimetableTab(val href: String?) {
    All(null),
    Analytics("/timetable/analytics"),
    Generate("/timetable/generate"),
    Conflicts("/timetable/conflicts"),
    Settings("/timetable/settings"),
    Today(null),
    Full(null),
}

/** `layout.tsx` + `permissions-config.ts` (`PERMISSION_MATRIX`), tab for tab. */
fun tabsFor(role: UserRole?): List<TimetableTab> {
    val isAdmin = role == UserRole.ADMIN || role == UserRole.DEVELOPER
    val isStudent = role == UserRole.STUDENT
    val analytics = role in setOf(UserRole.ADMIN, UserRole.DEVELOPER, UserRole.TEACHER, UserRole.ACCOUNTANT)
    return buildList {
        if (isAdmin) add(TimetableTab.All)
        if (analytics) add(TimetableTab.Analytics)
        if (isAdmin) {
            add(TimetableTab.Generate)
            add(TimetableTab.Conflicts)
            add(TimetableTab.Settings)
        }
        if (!isAdmin && !isStudent && role != null) {
            add(TimetableTab.Today)
            add(TimetableTab.Full)
        }
    }
}

data class TimetableUiState(
    val role: UserRole?,
    val userName: String? = null,
    val isLoading: Boolean = true,
    /** Served from the last good load because the network failed. */
    val isOffline: Boolean = false,
    /** Nothing could be loaded and nothing was cached. */
    val failed: Boolean = false,
    val week: WeekTimetable? = null,
    /** The page's own tab (the web's `/timetable` vs `/timetable/full`). */
    val tab: TimetableTab = tabsFor(role).firstOrNull { it.href == null } ?: TimetableTab.Today,
    /** The student's week/day pick; null follows the phone's default, the single day. */
    val pickedRange: RangeMode? = null,
    val children: List<Child> = emptyList(),
    val selectedChildId: String? = null,
    val classroomFilter: String? = null,
    val subjectFilter: String? = null,
    /** Device wall clock, compared against the periods' wall-clock times. */
    val nowMinutes: Int = 0,
    val inspectedSlotId: String? = null,
) {
    val surface: TimetableSurface get() = surfaceFor(role)
    val tabs: List<TimetableTab> get() = tabsFor(role)
    val range: RangeMode get() = pickedRange ?: RangeMode.Day
    val selectedChild: Child? get() = children.firstOrNull { it.id == selectedChildId }
    val inspectedSlot: Slot? get() = week?.slots?.firstOrNull { it.id == inspectedSlotId }
}

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val repository: TimetableRepository,
    tenantContext: TenantContext,
    @TimetableClock private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TimetableUiState(role = tenantContext.userRole, userName = tenantContext.userName, nowMinutes = nowMinutes()),
    )
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val scope = scopeFor(_uiState.value.surface) ?: run {
                // No mobile route reads an admin's grid: there is nothing to fetch.
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            repository.cached(scope)?.let { cached -> apply(cached, offline = false, loading = true) }
            load()
        }
    }

    fun refresh() {
        viewModelScope.launch { load() }
    }

    /** Re-read the clock; the screen calls this every minute so the lamp and the current class move on. */
    fun tick() {
        _uiState.update { it.copy(nowMinutes = nowMinutes()) }
    }

    fun selectTab(tab: TimetableTab) {
        if (tab.href == null) _uiState.update { it.copy(tab = tab) }
    }

    fun pickRange(range: RangeMode) {
        _uiState.update { it.copy(pickedRange = range) }
    }

    fun selectChild(childId: String) {
        if (childId == _uiState.value.selectedChildId) return
        _uiState.update { it.copy(selectedChildId = childId, week = null, isLoading = true) }
        viewModelScope.launch { load() }
    }

    fun filterClassroom(value: String?) = _uiState.update { it.copy(classroomFilter = value) }

    fun filterSubject(value: String?) = _uiState.update { it.copy(subjectFilter = value) }

    fun inspect(slotId: String?) = _uiState.update { it.copy(inspectedSlotId = slotId) }

    private suspend fun load() {
        val state = _uiState.value
        val result = when (state.surface) {
            TimetableSurface.Student, TimetableSurface.Teacher -> repository.loadMine()
            TimetableSurface.Guardian -> repository.loadGuardian(state.selectedChildId)
            TimetableSurface.Admin -> return
        }
        when (result) {
            is TimetableResult.Fresh -> apply(result.bundle, offline = false, loading = false)
            is TimetableResult.Cached -> apply(result.bundle, offline = true, loading = false)
            is TimetableResult.Failed -> _uiState.update {
                it.copy(isLoading = false, failed = it.week == null, isOffline = it.week != null)
            }
        }
    }

    private fun apply(bundle: TimetableBundle, offline: Boolean, loading: Boolean) {
        val deviceToday = LocalDateTime.now(clock).dayOfWeek.value % 7
        _uiState.update {
            it.copy(
                isLoading = loading,
                isOffline = offline,
                failed = false,
                week = bundle.toWeek(deviceToday),
                children = if (it.surface == TimetableSurface.Guardian) bundle.children.map { c -> c.toChild() } else it.children,
                selectedChildId = bundle.childId ?: it.selectedChildId,
                nowMinutes = nowMinutes(),
            )
        }
    }

    private fun nowMinutes(): Int = LocalDateTime.now(clock).let { it.hour * 60 + it.minute }

    private fun scopeFor(surface: TimetableSurface): TimetableScope? = when (surface) {
        TimetableSurface.Student, TimetableSurface.Teacher -> TimetableScope.Mine
        TimetableSurface.Guardian -> TimetableScope.Guardian
        TimetableSurface.Admin -> null
    }
}
