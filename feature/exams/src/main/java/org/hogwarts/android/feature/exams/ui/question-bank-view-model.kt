package org.hogwarts.android.feature.exams.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.exams.domain.model.Difficulty
import org.hogwarts.android.feature.exams.domain.model.QuestionBankItem
import org.hogwarts.android.feature.exams.domain.usecase.GenerateQuestionsUseCase
import org.hogwarts.android.feature.exams.domain.usecase.GetQuestionBankUseCase
import org.hogwarts.android.feature.exams.domain.usecase.ToggleBookmarkUseCase
import javax.inject.Inject

data class QuestionBankUiState(
    val isLoading: Boolean = true,
    val questions: List<QuestionBankItem> = emptyList(),
    val filteredQuestions: List<QuestionBankItem> = emptyList(),
    val selectedSubject: String? = null,
    val selectedTopic: String? = null,
    val selectedDifficulty: Difficulty? = null,
    val showBookmarkedOnly: Boolean = false,
    val availableSubjects: List<String> = emptyList(),
    val availableTopics: List<String> = emptyList(),
    val practiceMode: Boolean = false,
    val practiceQuestionIndex: Int = 0,
    val practiceAnswer: String? = null,
    val showExplanation: Boolean = false,
    val isGenerating: Boolean = false,
    val error: String? = null
) {
    val currentPracticeQuestion: QuestionBankItem?
        get() = filteredQuestions.getOrNull(practiceQuestionIndex)
    val practiceProgress: Float
        get() = if (filteredQuestions.isNotEmpty()) {
            (practiceQuestionIndex + 1f) / filteredQuestions.size
        } else 0f
}

@HiltViewModel
class QuestionBankViewModel @Inject constructor(
    private val getQuestionBankUseCase: GetQuestionBankUseCase,
    private val generateQuestionsUseCase: GenerateQuestionsUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionBankUiState())
    val uiState: StateFlow<QuestionBankUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value
            when (val result = getQuestionBankUseCase(
                subject = state.selectedSubject,
                topic = state.selectedTopic,
                difficulty = state.selectedDifficulty?.name
            )) {
                is Resource.Success -> {
                    val questions = result.data ?: emptyList()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            questions = questions,
                            filteredQuestions = applyFilters(questions, it),
                            availableSubjects = questions.map { q -> q.subject }.distinct().sorted(),
                            availableTopics = questions.map { q -> q.topic }.distinct().sorted(),
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error?.message ?: "Failed to load questions"
                        )
                    }
                }
                is Resource.Loading -> { /* handled above */ }
            }
        }
    }

    fun onSubjectFilterChanged(subject: String?) {
        _uiState.update { state ->
            val newState = state.copy(selectedSubject = subject, practiceQuestionIndex = 0)
            newState.copy(filteredQuestions = applyFilters(state.questions, newState))
        }
    }

    fun onTopicFilterChanged(topic: String?) {
        _uiState.update { state ->
            val newState = state.copy(selectedTopic = topic, practiceQuestionIndex = 0)
            newState.copy(filteredQuestions = applyFilters(state.questions, newState))
        }
    }

    fun onDifficultyFilterChanged(difficulty: Difficulty?) {
        _uiState.update { state ->
            val newState = state.copy(selectedDifficulty = difficulty, practiceQuestionIndex = 0)
            newState.copy(filteredQuestions = applyFilters(state.questions, newState))
        }
    }

    fun onBookmarkedOnlyToggled() {
        _uiState.update { state ->
            val newState = state.copy(showBookmarkedOnly = !state.showBookmarkedOnly, practiceQuestionIndex = 0)
            newState.copy(filteredQuestions = applyFilters(state.questions, newState))
        }
    }

    fun togglePracticeMode() {
        _uiState.update {
            it.copy(
                practiceMode = !it.practiceMode,
                practiceQuestionIndex = 0,
                practiceAnswer = null,
                showExplanation = false
            )
        }
    }

    fun selectPracticeAnswer(answer: String) {
        _uiState.update { it.copy(practiceAnswer = answer) }
    }

    fun revealExplanation() {
        _uiState.update { it.copy(showExplanation = true) }
    }

    fun nextPracticeQuestion() {
        _uiState.update { state ->
            if (state.practiceQuestionIndex < state.filteredQuestions.size - 1) {
                state.copy(
                    practiceQuestionIndex = state.practiceQuestionIndex + 1,
                    practiceAnswer = null,
                    showExplanation = false
                )
            } else state
        }
    }

    fun previousPracticeQuestion() {
        _uiState.update { state ->
            if (state.practiceQuestionIndex > 0) {
                state.copy(
                    practiceQuestionIndex = state.practiceQuestionIndex - 1,
                    practiceAnswer = null,
                    showExplanation = false
                )
            } else state
        }
    }

    fun generateMoreQuestions() {
        val state = _uiState.value
        val subject = state.selectedSubject ?: state.availableSubjects.firstOrNull() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            when (val result = generateQuestionsUseCase(
                subject = subject,
                topic = state.selectedTopic,
                difficulty = state.selectedDifficulty?.name,
                count = 10
            )) {
                is Resource.Success -> {
                    val newQuestions = result.data ?: emptyList()
                    _uiState.update { currentState ->
                        val allQuestions = currentState.questions + newQuestions
                        currentState.copy(
                            isGenerating = false,
                            questions = allQuestions,
                            filteredQuestions = applyFilters(allQuestions, currentState),
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            error = result.error?.message ?: "Failed to generate questions"
                        )
                    }
                }
                is Resource.Loading -> { /* handled above */ }
            }
        }
    }

    fun toggleBookmark(questionId: String) {
        viewModelScope.launch {
            when (val result = toggleBookmarkUseCase(questionId)) {
                is Resource.Success -> {
                    val isBookmarked = result.data ?: false
                    _uiState.update { state ->
                        val updatedQuestions = state.questions.map { q ->
                            if (q.id == questionId) q.copy(isBookmarked = isBookmarked) else q
                        }
                        state.copy(
                            questions = updatedQuestions,
                            filteredQuestions = applyFilters(updatedQuestions, state)
                        )
                    }
                }
                is Resource.Error -> { /* silently fail bookmark */ }
                is Resource.Loading -> { /* no-op */ }
            }
        }
    }

    private fun applyFilters(
        questions: List<QuestionBankItem>,
        state: QuestionBankUiState
    ): List<QuestionBankItem> {
        return questions.filter { q ->
            (state.selectedSubject == null || q.subject == state.selectedSubject) &&
                (state.selectedTopic == null || q.topic == state.selectedTopic) &&
                (state.selectedDifficulty == null || q.difficulty == state.selectedDifficulty) &&
                (!state.showBookmarkedOnly || q.isBookmarked)
        }
    }
}
