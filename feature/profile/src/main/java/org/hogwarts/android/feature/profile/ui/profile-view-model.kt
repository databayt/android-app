package org.hogwarts.android.feature.profile.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.profile.data.repository.ProfileRepository
import org.hogwarts.android.feature.profile.domain.validation.ProfileUpdateForm
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val tenantContext: TenantContext,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val targetUserId: String? = savedStateHandle["userId"]

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun refresh() = load()

    fun setEditing(editing: Boolean) {
        _uiState.update { it.copy(isEditing = editing) }
    }

    fun save(form: ProfileUpdateForm) {
        if (!_uiState.value.isOwner) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            runCatching { repository.updateProfile(username = form.displayName, bio = form.bio) }
                .onSuccess { updated ->
                    _uiState.update { it.copy(profile = updated, isSaving = false, isEditing = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val isOwner = targetUserId == null || targetUserId == tenantContext.userId
                val profile = if (targetUserId == null) repository.getOwnProfile()
                else repository.getProfileById(targetUserId)

                val contributionsDeferred = async {
                    runCatching { repository.getContributions(userId = profile.userId) }.getOrNull()
                }
                val activityDeferred = async {
                    runCatching { repository.getActivity(userId = profile.userId) }.getOrDefault(emptyList())
                }
                val pinnedDeferred = async {
                    runCatching { repository.getPinnedItems(userId = profile.userId) }.getOrDefault(emptyList())
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profile = profile,
                        contributions = contributionsDeferred.await(),
                        activity = activityDeferred.await(),
                        pinned = pinnedDeferred.await(),
                        isOwner = isOwner,
                        viewerUserId = tenantContext.userId,
                        viewerRole = tenantContext.userRole,
                        viewerSchoolId = tenantContext.schoolId,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
