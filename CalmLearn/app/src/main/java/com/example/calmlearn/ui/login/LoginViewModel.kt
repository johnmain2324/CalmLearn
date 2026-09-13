package com.example.calmlearn.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmlearn.data.auth.AuthRepository
import com.example.calmlearn.data.auth.AuthRepositoryProvider
import com.example.calmlearn.data.auth.AuthResult
import com.example.calmlearn.ui.common.FormUiState
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

    var email: String = ""
    var password: String = ""
    var rememberMe: Boolean = false

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    private val _fieldError = MutableLiveData<Int?>(null)
    val fieldError: LiveData<Int?> = _fieldError

    private val _uiState = MutableLiveData<FormUiState>(FormUiState.Idle)
    val uiState: LiveData<FormUiState> = _uiState

    private val _googleUnavailableEvent = MutableLiveData<Unit?>(null)
    val googleUnavailableEvent: LiveData<Unit?> = _googleUnavailableEvent

    fun onFieldChanged() {
        _fieldError.value = null
        _isFormValid.value = LoginFormValidator.firstError(currentInput()) == null
    }

    fun submit() {
        if (_uiState.value == FormUiState.Loading) return

        val input = currentInput()
        val error = LoginFormValidator.firstError(input)
        if (error != null) {
            _fieldError.value = error
            return
        }

        _fieldError.value = null
        _uiState.value = FormUiState.Loading
        viewModelScope.launch {
            val result = repository.login(input.email, password, rememberMe)
            _uiState.value = when (result) {
                is AuthResult.Success -> FormUiState.Success
                is AuthResult.Error -> FormUiState.Error(result.reason)
            }
        }
    }

    /** Google Sign-In chua duoc cau hinh trong project - chi bao ro cho nguoi dung, khong vao thang Trang chu. */
    fun onGoogleSignInClicked() {
        _googleUnavailableEvent.value = Unit
    }

    fun onGoogleUnavailableEventConsumed() {
        _googleUnavailableEvent.value = null
    }

    private fun currentInput() = LoginInput(email = email.trim(), password = password)
}
