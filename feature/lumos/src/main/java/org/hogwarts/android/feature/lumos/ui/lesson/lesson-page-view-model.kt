package org.hogwarts.android.feature.lumos.ui.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.lumos.data.remote.LumosApi
import org.hogwarts.android.feature.lumos.data.remote.dto.LessonPageDto
import org.hogwarts.android.feature.lumos.data.remote.dto.LessonProgressDto
import org.hogwarts.android.feature.lumos.data.remote.dto.QuizGradeDto
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class LessonPageState(
    val page: LessonPageDto? = null,
    val loading: Boolean = true,
    val error: String? = null,
    /** The title card gives way to the player once Play is pressed, as on the web. */
    val playing: Boolean = false,
    /** A playable URL for the player — protected paths resolved to their signed target. */
    val playUrl: String? = null,
    val completed: Boolean = false,
    val completing: Boolean = false,
    val choices: Map<String, Int> = emptyMap(),
    val texts: Map<String, String> = emptyMap(),
    val submitting: Boolean = false,
    val quizError: String? = null,
    val grade: QuizGradeDto? = null,
)

/** `/lumos/courses/[slug]/[lessonId]`, from the page's own server reads. */
@HiltViewModel
class LessonPageViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val api: LumosApi,
    private val tenant: TenantContext,
) : ViewModel() {
    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])
    private val _state = MutableStateFlow(LessonPageState())
    val state: StateFlow<LessonPageState> = _state.asStateFlow()
    private val attemptId = UUID.randomUUID().toString()

    init {
        viewModelScope.launch {
            runCatching { api.getLessonPage(lessonId, if (Locale.getDefault().language == "en") "en" else "ar") }
                .onSuccess { p -> _state.update { it.copy(page = p, loading = false, completed = p.progress?.isCompleted == true) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
        }
    }

    fun play() {
        val url = _state.value.page?.videoUrl ?: return
        viewModelScope.launch {
            _state.update { it.copy(playing = true, playUrl = resolve(url)) }
        }
    }

    /** Opens a resource: its protected path resolved to the signed URL it redirects to. */
    suspend fun resolve(url: String): String =
        if (url.startsWith("/")) {
            runCatching { api.resolveMedia(url.removePrefix("/")).raw().request.url.toString() }.getOrDefault(url)
        } else url

    fun markComplete() {
        val page = _state.value.page ?: return
        if (_state.value.completed) return
        viewModelScope.launch {
            _state.update { it.copy(completing = true) }
            runCatching {
                api.updateLessonProgress(
                    page.course.id, page.id, tenant.requireSchoolId(),
                    LessonProgressDto(lessonId = page.id, status = "COMPLETED", score = null, watchedSeconds = 0, totalSeconds = 0),
                )
            }.onSuccess { _state.update { it.copy(completing = false, completed = true) } }
                .onFailure { _state.update { it.copy(completing = false) } }
        }
    }

    fun choose(questionId: String, index: Int) = _state.update { it.copy(choices = it.choices + (questionId to index)) }
    fun type(questionId: String, text: String) = _state.update { it.copy(texts = it.texts + (questionId to text)) }

    fun submitQuiz() {
        val page = _state.value.page ?: return
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, quizError = null) }
            val body = buildJsonObject {
                putJsonObject("answers") {
                    s.choices.forEach { (id, i) -> put(id, JsonPrimitive(i)) }
                    s.texts.forEach { (id, t) -> put(id, JsonPrimitive(t)) }
                }
                put("attempt_id", attemptId)
            }
            runCatching { api.gradeQuiz(page.course.id, page.id, body) }
                .onSuccess { g -> _state.update { it.copy(submitting = false, grade = g) } }
                .onFailure { _state.update { it.copy(submitting = false, quizError = page.labels["quiz_failed"]) } }
        }
    }
}
