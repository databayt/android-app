package org.hogwarts.android.feature.lessons.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.lessons.domain.model.ActivityType
import org.hogwarts.android.feature.lessons.domain.model.LessonActivity
import org.hogwarts.android.feature.lessons.domain.model.LessonPlan
import org.hogwarts.android.feature.lessons.domain.model.LessonPlanStatus
import org.hogwarts.android.feature.lessons.domain.model.LessonResource
import org.hogwarts.android.feature.lessons.domain.model.ResourceType
import org.hogwarts.android.feature.lessons.domain.usecase.CreateLessonPlanUseCase
import org.hogwarts.android.feature.lessons.domain.usecase.GetLessonDetailUseCase
import org.hogwarts.android.feature.lessons.domain.usecase.UpdateLessonPlanUseCase
import javax.inject.Inject

data class LessonPlanFormUiState(
    val id: String = "",
    val classId: String = "",
    val subjectName: String = "",
    val topic: String = "",
    val date: String = "",
    val objectives: List<String> = emptyList(),
    val activities: List<LessonActivity> = emptyList(),
    val resources: List<LessonResource> = emptyList(),
    val homework: String = "",
    val teacherNotes: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LessonPlanFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLessonDetailUseCase: GetLessonDetailUseCase,
    private val createLessonPlanUseCase: CreateLessonPlanUseCase,
    private val updateLessonPlanUseCase: UpdateLessonPlanUseCase
) : ViewModel() {

    private val lessonId: String? = savedStateHandle.get<String>("lessonId")?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(LessonPlanFormUiState())
    val uiState: StateFlow<LessonPlanFormUiState> = _uiState.asStateFlow()

    init {
        if (lessonId != null) {
            loadExistingPlan(lessonId)
        }
    }

    private fun loadExistingPlan(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getLessonDetailUseCase(id)) {
                is Result.Success -> {
                    val plan = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditing = true,
                            id = plan.id,
                            classId = plan.classId,
                            subjectName = plan.subjectName,
                            topic = plan.topic,
                            date = plan.date,
                            objectives = plan.objectives,
                            activities = plan.activities,
                            resources = plan.resources,
                            homework = plan.homework ?: "",
                            teacherNotes = plan.teacherNotes ?: ""
                        )
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun updateTopic(value: String) { _uiState.update { it.copy(topic = value) } }
    fun updateClassId(value: String) { _uiState.update { it.copy(classId = value) } }
    fun updateSubjectName(value: String) { _uiState.update { it.copy(subjectName = value) } }
    fun updateDate(value: String) { _uiState.update { it.copy(date = value) } }
    fun updateHomework(value: String) { _uiState.update { it.copy(homework = value) } }
    fun updateTeacherNotes(value: String) { _uiState.update { it.copy(teacherNotes = value) } }

    fun addObjective(objective: String) {
        if (objective.isBlank()) return
        _uiState.update { it.copy(objectives = it.objectives + objective) }
    }

    fun removeObjective(index: Int) {
        _uiState.update { state ->
            state.copy(objectives = state.objectives.filterIndexed { i, _ -> i != index })
        }
    }

    fun addActivity(description: String, duration: Int, type: ActivityType) {
        if (description.isBlank()) return
        val activity = LessonActivity(description = description, duration = duration, type = type)
        _uiState.update { it.copy(activities = it.activities + activity) }
    }

    fun removeActivity(index: Int) {
        _uiState.update { state ->
            state.copy(activities = state.activities.filterIndexed { i, _ -> i != index })
        }
    }

    fun addResource(name: String, type: ResourceType, url: String) {
        if (name.isBlank() || url.isBlank()) return
        val resource = LessonResource(
            id = "",
            name = name,
            type = type,
            url = url
        )
        _uiState.update { it.copy(resources = it.resources + resource) }
    }

    fun removeResource(index: Int) {
        _uiState.update { state ->
            state.copy(resources = state.resources.filterIndexed { i, _ -> i != index })
        }
    }

    fun saveDraft() = save(LessonPlanStatus.DRAFT)
    fun publish() = save(LessonPlanStatus.PUBLISHED)

    private fun save(status: LessonPlanStatus) {
        val state = _uiState.value
        if (state.topic.isBlank() || state.subjectName.isBlank() || state.date.isBlank()) {
            _uiState.update { it.copy(error = "Topic, subject, and date are required.") }
            return
        }

        val plan = LessonPlan(
            id = state.id,
            schoolId = "", // Repository will inject schoolId via TenantContext
            classId = state.classId,
            subjectName = state.subjectName,
            topic = state.topic,
            date = state.date,
            objectives = state.objectives,
            activities = state.activities,
            resources = state.resources,
            homework = state.homework.ifBlank { null },
            teacherNotes = state.teacherNotes.ifBlank { null },
            status = status
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = if (state.isEditing) {
                updateLessonPlanUseCase(plan)
            } else {
                createLessonPlanUseCase(plan)
            }
            when (result) {
                is Result.Success -> _uiState.update {
                    it.copy(isSaving = false, saveSuccess = true)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }
}
