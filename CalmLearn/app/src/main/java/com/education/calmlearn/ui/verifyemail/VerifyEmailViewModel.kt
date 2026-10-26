package com.education.calmlearn.ui.verifyemail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.education.calmlearn.R
import com.education.calmlearn.data.auth.AuthErrorReason
import com.education.calmlearn.data.auth.AuthRepository
import com.education.calmlearn.data.auth.AuthRepositoryProvider
import com.education.calmlearn.data.auth.AuthResult
import com.education.calmlearn.data.auth.Gender
import com.education.calmlearn.data.auth.ProfileResult
import com.education.calmlearn.data.auth.VerificationCheckResult
import com.education.calmlearn.ui.common.FormUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

sealed class VerifyEmailMode {
    object Loading : VerifyEmailMode()

    data class ProfileForm(val email: String) : VerifyEmailMode()

    data class Verify(val email: String) : VerifyEmailMode()
}

class VerifyEmailViewModel(
    private val repository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

    private val _mode = MutableLiveData<VerifyEmailMode>(VerifyEmailMode.Loading)
    val mode: LiveData<VerifyEmailMode> = _mode

    private val _uiState = MutableLiveData<FormUiState>(FormUiState.Idle)
    val uiState: LiveData<FormUiState> = _uiState

    private val _infoEvent = MutableLiveData<Int?>(null)
    val infoEvent: LiveData<Int?> = _infoEvent

    var fullName: String = ""
        private set
    var gender: Gender? = null
        private set

    private val _isProfileFormValid = MutableLiveData(false)
    val isProfileFormValid: LiveData<Boolean> = _isProfileFormValid

    private var initialized = false

    fun onFullNameChanged(value: String) {
        fullName = value
        recomputeProfileFormValid()
    }

    fun onGenderChanged(value: Gender?) {
        gender = value
        recomputeProfileFormValid()
    }

    private fun recomputeProfileFormValid() {
        _isProfileFormValid.value = fullName.trim().isNotEmpty() && gender != null
    }

    fun initialize(autoSend: Boolean) {
        if (initialized) return
        initialized = true

        viewModelScope.launch {
            when (val result = repository.currentUserProfile()) {
                is ProfileResult.Loaded -> {
                    _mode.value = VerifyEmailMode.Verify(result.profile.email)
                    if (autoSend) resend()
                }
                ProfileResult.Missing, ProfileResult.NotSignedIn -> {
                    _mode.value = VerifyEmailMode.ProfileForm(repository.currentSessionEmail().orEmpty())
                }
                is ProfileResult.ReadError -> {
                    _mode.value = VerifyEmailMode.Verify(repository.currentSessionEmail().orEmpty())
                    _uiState.value = FormUiState.Error(result.reason)
                }
            }
        }
    }

    fun submitProfile() {
        if (_uiState.value == FormUiState.Loading) return
        val name = fullName.trim()
        val selectedGender = gender
        if (name.isEmpty() || selectedGender == null) {
            recomputeProfileFormValid()
            return
        }

        _uiState.value = FormUiState.Loading
        viewModelScope.launch {
            val result = try {
                repository.completeProfile(name, selectedGender)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (unexpected: Exception) {
                AuthResult.Error(AuthErrorReason.UNKNOWN)
            }
            when (result) {
                is AuthResult.Success -> {
                    _mode.value = VerifyEmailMode.Verify(repository.currentSessionEmail().orEmpty())
                    _uiState.value = FormUiState.Idle
                    resend()
                }
                is AuthResult.Error -> _uiState.value = FormUiState.Error(result.reason)
            }
        }
    }

    fun resend() {
        if (_uiState.value == FormUiState.Loading) return

        _uiState.value = FormUiState.Loading
        viewModelScope.launch {
            val result = try {
                repository.resendVerificationEmail()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (unexpected: Exception) {
                AuthResult.Error(AuthErrorReason.UNKNOWN)
            }
            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = FormUiState.Idle
                    _infoEvent.value = R.string.verify_resent_success
                }
                is AuthResult.Error -> _uiState.value = FormUiState.Error(result.reason)
            }
        }
    }

    fun checkAgain() {
        if (_uiState.value == FormUiState.Loading) return

        _uiState.value = FormUiState.Loading
        viewModelScope.launch {
            val result = try {
                repository.refreshVerificationStatus()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (unexpected: Exception) {
                VerificationCheckResult.CHECK_FAILED
            }
            when (result) {
                VerificationCheckResult.VERIFIED -> _uiState.value = FormUiState.Success
                VerificationCheckResult.NOT_YET_VERIFIED -> {
                    _uiState.value = FormUiState.Idle
                    _infoEvent.value = R.string.verify_still_not_verified
                }
                VerificationCheckResult.CHECK_FAILED -> _uiState.value = FormUiState.Error(AuthErrorReason.UNKNOWN)
            }
        }
    }

    fun abandon() {
        repository.cancelPendingSession()
    }

    fun onInfoEventConsumed() {
        _infoEvent.value = null
    }

    fun consumeTerminalState() {
        if (_uiState.value == FormUiState.Success) {
            _uiState.value = FormUiState.Idle
        }
    }
}
