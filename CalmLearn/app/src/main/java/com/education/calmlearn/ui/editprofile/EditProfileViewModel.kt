package com.education.calmlearn.ui.editprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.education.calmlearn.data.auth.AuthErrorReason
import com.education.calmlearn.data.auth.AuthRepository
import com.education.calmlearn.data.auth.AuthRepositoryProvider
import com.education.calmlearn.data.auth.AuthResult
import com.education.calmlearn.data.auth.Gender
import com.education.calmlearn.data.auth.ProfileResult
import com.education.calmlearn.ui.common.FormUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val repository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

    var fullName: String = ""
        private set
    var gender: Gender? = null
        private set
    var email: String = ""
        private set

    private val touchedFields = mutableSetOf<EditProfileField>()

    private val _loadResult = MutableLiveData<ProfileResult?>(null)
    val loadResult: LiveData<ProfileResult?> = _loadResult

    private val _fieldErrors = MutableLiveData(EditProfileFieldErrors())
    val fieldErrors: LiveData<EditProfileFieldErrors> = _fieldErrors

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    private val _uiState = MutableLiveData<FormUiState>(FormUiState.Idle)
    val uiState: LiveData<FormUiState> = _uiState

    init {
        load()
    }

    fun load() {
        _loadResult.value = null
        viewModelScope.launch {
            val result = try {
                repository.currentUserProfile()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (unexpected: Exception) {
                ProfileResult.ReadError(AuthErrorReason.UNKNOWN)
            }
            if (result is ProfileResult.Loaded) {
                fullName = result.profile.fullName
                gender = result.profile.gender
                email = result.profile.email
            } else {
                email = repository.currentSessionEmail().orEmpty()
            }
            recomputeErrors()
            _loadResult.value = result
        }
    }

    fun onFullNameChanged(value: String) {
        fullName = value
        onFieldTouched(EditProfileField.FULL_NAME)
    }

    fun onGenderChanged(value: Gender?) {
        gender = value
        onFieldTouched(EditProfileField.GENDER)
    }

    fun onFieldBlurred(field: EditProfileField) {
        onFieldTouched(field)
    }

    private fun onFieldTouched(field: EditProfileField) {
        touchedFields.add(field)
        clearServiceErrorIfStale()
        recomputeErrors()
    }

    private fun clearServiceErrorIfStale() {
        if (_uiState.value is FormUiState.Error) {
            _uiState.value = FormUiState.Idle
        }
    }

    private fun recomputeErrors() {
        val all = EditProfileFormValidator.errors(currentInput())
        _isFormValid.value = all.isValid
        _fieldErrors.value = all.visibleTo(touchedFields)
    }

    fun submit() {
        if (_uiState.value == FormUiState.Loading) return

        touchedFields.addAll(EditProfileField.entries)
        val input = currentInput()
        val allErrors = EditProfileFormValidator.errors(input)
        _fieldErrors.value = allErrors
        if (!allErrors.isValid) {
            _isFormValid.value = false
            return
        }

        _uiState.value = FormUiState.Loading
        viewModelScope.launch {
            val result = try {
                repository.completeProfile(input.fullName, input.gender!!)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (unexpected: Exception) {
                AuthResult.Error(AuthErrorReason.UNKNOWN)
            }
            _uiState.value = when (result) {
                AuthResult.Success -> FormUiState.Success
                is AuthResult.Error -> FormUiState.Error(result.reason)
            }
        }
    }

    fun consumeTerminalState() {
        if (_uiState.value == FormUiState.Success) {
            _uiState.value = FormUiState.Idle
        }
    }

    private fun currentInput() = EditProfileInput(
        fullName = fullName.trim(),
        gender = gender
    )
}
