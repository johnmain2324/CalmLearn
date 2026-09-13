package com.example.calmlearn.ui.forgotpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmlearn.data.auth.AuthRepository
import com.example.calmlearn.data.auth.AuthRepositoryProvider
import com.example.calmlearn.data.auth.AuthResult
import com.example.calmlearn.ui.common.FormUiState
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val repository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

    var email: String = ""

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    private val _fieldError = MutableLiveData<Int?>(null)
    val fieldError: LiveData<Int?> = _fieldError

    private val _uiState = MutableLiveData<FormUiState>(FormUiState.Idle)
    val uiState: LiveData<FormUiState> = _uiState

    fun onFieldChanged() {
        _fieldError.value = null
        _isFormValid.value = ForgotPasswordFormValidator.firstError(email.trim()) == null
    }

    fun submit() {
        if (_uiState.value == FormUiState.Loading) return

        val trimmedEmail = email.trim()
        val error = ForgotPasswordFormValidator.firstError(trimmedEmail)
        if (error != null) {
            _fieldError.value = error
            return
        }

        _fieldError.value = null
        _uiState.value = FormUiState.Loading
        viewModelScope.launch {
            // Thong bao trung tinh: chi bao thanh cong khi dich vu that tra ve Success,
            // khong tiet lo email co ton tai trong he thong hay khong.
            val result = repository.sendPasswordResetEmail(trimmedEmail)
            _uiState.value = when (result) {
                is AuthResult.Success -> FormUiState.Success
                is AuthResult.Error -> FormUiState.Error(result.reason)
            }
        }
    }
}
