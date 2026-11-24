package org.hogwarts.android.feature.admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.admin.domain.model.ClassReadiness
import org.hogwarts.android.feature.admin.domain.usecase.GetClassReadinessUseCase
import retrofit2.HttpException
import javax.inject.Inject

enum class ReadinessError { Offline, NotSupervisor, Failed }

data class ClassReadinessUiState(
    /** `YYYY-MM-DD` as the server answered it; null until the first answer. */
    val date: String? = null,
    val classes: List<ClassReadiness> = emptyList(),
    val isLoading: Boolean = true,
    val error: ReadinessError? = null,
    val selectedSectionId: String? = null,
) {
    val selected: ClassReadiness? get() = classes.firstOrNull { it.sectionId == selectedSectionId }
}

/** 403 is the server saying "you supervise no class"; IO is a phone without a connection. */
internal fun classifyReadinessError(e: Throwable): ReadinessError = when {
    e is HttpException && e.code() == 403 -> ReadinessError.NotSupervisor
    e is IOException -> ReadinessError.Offline
    else -> ReadinessError.Failed
}

/** The day `delta` days from [date], or null when [date] is not a `YYYY-MM-DD`. */
internal fun shiftDate(date: String?, delta: Long): String? = try {
    date?.let { LocalDate.parse(it).plusDays(delta).toString() }
} catch (_: DateTimeParseException) {
    null
}

@HiltViewModel
class ClassReadinessViewModel @Inject constructor(
    private val getClassReadiness: GetClassReadinessUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassReadinessUiState())
    val uiState: StateFlow<ClassReadinessUiState> = _uiState.asStateFlow()

    init {
        load(null)
    }

    private fun load(date: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getClassReadiness(date)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        date = result.data.date,
                        classes = result.data.classes,
                        // Keep the open class across a refresh when it is still there.
                        selectedSectionId = it.selectedSectionId?.takeIf { id ->
                            result.data.classes.any { c -> c.sectionId == id }
                        },
                    )
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, classes = emptyList(), error = classifyReadinessError(result.exception))
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun refresh() = load(_uiState.value.date)

    fun previousDay() = shiftDate(_uiState.value.date, -1)?.let(::load)

    fun nextDay() = shiftDate(_uiState.value.date, 1)?.let(::load)

    fun today() = load(null)

    fun select(sectionId: String?) = _uiState.update { it.copy(selectedSectionId = sectionId) }
}
