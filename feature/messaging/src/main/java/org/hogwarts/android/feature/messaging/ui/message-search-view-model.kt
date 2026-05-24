package org.hogwarts.android.feature.messaging.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.messaging.data.repository.MessagingRepository
import org.hogwarts.android.feature.messaging.domain.model.MessageSearchResult
import javax.inject.Inject

data class MessageSearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<MessageSearchResult> = emptyList(),
    val error: String? = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class MessageSearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MessagingRepository,
) : ViewModel() {

    /** Null means global search, non-null means per-conversation search. */
    private val conversationId: String? = savedStateHandle["conversationId"]

    private val _state = MutableStateFlow(MessageSearchUiState())
    val state: StateFlow<MessageSearchUiState> = _state.asStateFlow()

    private val _query = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { q ->
                    if (q.length < 2) {
                        _state.update { it.copy(isSearching = false, results = emptyList(), error = null) }
                        return@collectLatest
                    }
                    _state.update { it.copy(isSearching = true, error = null) }
                    try {
                        val results = if (conversationId != null) {
                            repository.searchConversationMessages(conversationId, q)
                        } else {
                            repository.searchMessages(q)
                        }
                        _state.update { it.copy(isSearching = false, results = results) }
                    } catch (e: Exception) {
                        _state.update { it.copy(isSearching = false, error = e.message) }
                    }
                }
        }
    }

    fun onQueryChanged(value: String) {
        _state.update { it.copy(query = value) }
        _query.value = value
    }
}
