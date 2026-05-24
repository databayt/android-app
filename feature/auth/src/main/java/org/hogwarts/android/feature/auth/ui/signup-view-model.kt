package org.hogwarts.android.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.domain.usecase.SignUpUseCase
import org.hogwarts.android.feature.auth.domain.validation.SignupValidator
import javax.inject.Inject

data class SignUpUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val schoolId: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value, fieldErrors = it.fieldErrors - "firstName", generalError = null) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value, fieldErrors = it.fieldErrors - "lastName", generalError = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, fieldErrors = it.fieldErrors - "email", generalError = null) }
    }

    fun onSchoolIdChange(value: String) {
        _uiState.update { it.copy(schoolId = value, fieldErrors = it.fieldErrors - "schoolId", generalError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, fieldErrors = it.fieldErrors - "password", generalError = null) }
    }

    fun signUp() {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (state.firstName.isBlank()) errors["firstName"] = "First name is required"
        if (state.lastName.isBlank()) errors["lastName"] = "Last name is required"
        SignupValidator.validateEmail(state.email)?.let { errors["email"] = it }
        SignupValidator.validatePassword(state.password)?.let { errors["password"] = it }
        if (state.schoolId.isBlank()) errors["schoolId"] = "School is required"

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            when (val result = signUpUseCase(
                firstName = state.firstName,
                lastName = state.lastName,
                email = state.email,
                password = state.password,
                schoolId = state.schoolId
            )) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = result.exception.message ?: "Registration failed"
                        )
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun signUpWithGoogle(idToken: String) {
        // OAuth signup — handled via login with Google (creates account if new)
        // The auth backend handles account creation for new OAuth users
    }
}
