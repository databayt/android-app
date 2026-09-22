package org.hogwarts.android.feature.lumos.ui.course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.lumos.data.remote.LumosApi
import org.hogwarts.android.feature.lumos.data.remote.dto.CoursePageDto
import java.util.Locale
import javax.inject.Inject

data class CoursePageState(
    val page: CoursePageDto? = null,
    val loading: Boolean = true,
    val enrolling: Boolean = false,
    val error: String? = null,
)

/** `/lumos/courses/[slug]`, from the page's own server reads. */
@HiltViewModel
class CoursePageViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val api: LumosApi,
    private val tenant: TenantContext,
) : ViewModel() {
    private val courseId: String = checkNotNull(savedStateHandle["courseId"])
    private val _state = MutableStateFlow(CoursePageState())
    val state: StateFlow<CoursePageState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            runCatching { api.getCoursePage(courseId, if (Locale.getDefault().language == "en") "en" else "ar") }
                .onSuccess { p -> _state.update { it.copy(page = p, loading = false) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
        }
    }

    /** `enrollInSubject`, then the page again — the web's action revalidates the same way. */
    fun enroll() {
        val page = _state.value.page ?: return
        viewModelScope.launch {
            _state.update { it.copy(enrolling = true, error = null) }
            runCatching { api.enrollCourse(page.id, tenant.requireSchoolId()) }
                .onSuccess { _state.update { it.copy(enrolling = false) }; load() }
                .onFailure { _ -> _state.update { it.copy(enrolling = false, error = page.labels["enroll_error"]) } }
        }
    }
}
