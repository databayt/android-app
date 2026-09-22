package org.hogwarts.android.feature.live.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.live.data.remote.dto.SessionPageDto
import org.hogwarts.android.feature.live.data.repository.LiveRepository
import org.hogwarts.android.feature.live.domain.model.LiveRecording
import javax.inject.Inject

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Ready<T>(val value: T) : LoadState<T>
    data class Failed(val message: String?) : LoadState<Nothing>
}

/** `/live/[id]`, from the page's own loader. */
@HiltViewModel
class LiveSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LiveRepository,
) : ViewModel() {
    private val id: String = checkNotNull(savedStateHandle["id"])
    private val _state = MutableStateFlow<LoadState<SessionPageDto>>(LoadState.Loading)
    val state: StateFlow<LoadState<SessionPageDto>> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = runCatching { repository.getSession(id) }
                .fold({ LoadState.Ready(it) }, { LoadState.Failed(it.message) })
        }
    }
}

/** `/live/[id]/recordings`: the list, and a signed URL minted per Play. */
@HiltViewModel
class LiveRecordingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LiveRepository,
) : ViewModel() {
    private val id: String = checkNotNull(savedStateHandle["id"])
    private val _state = MutableStateFlow<LoadState<List<LiveRecording>>>(LoadState.Loading)
    val state: StateFlow<LoadState<List<LiveRecording>>> = _state.asStateFlow()

    /** recording id → signed URL, or null while it loads; a failure clears it. */
    private val _urls = MutableStateFlow<Map<String, String?>>(emptyMap())
    val urls: StateFlow<Map<String, String?>> = _urls.asStateFlow()
    private val _failed = MutableStateFlow<Set<String>>(emptySet())
    val failed: StateFlow<Set<String>> = _failed.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = runCatching { repository.getRecordings(id) }
                .fold({ LoadState.Ready(it) }, { LoadState.Failed(it.message) })
        }
    }

    fun play(recordingId: String) {
        _failed.value = _failed.value - recordingId
        _urls.value = _urls.value + (recordingId to null)
        viewModelScope.launch {
            runCatching { repository.getRecordingUrl(recordingId) }
                .onSuccess { url -> _urls.value = _urls.value + (recordingId to url) }
                .onFailure { playbackFailed(recordingId) }
        }
    }

    /** The signature expired or the file would not load: back to the Play button, with the error. */
    fun playbackFailed(recordingId: String) {
        _urls.value = _urls.value - recordingId
        _failed.value = _failed.value + recordingId
    }
}
