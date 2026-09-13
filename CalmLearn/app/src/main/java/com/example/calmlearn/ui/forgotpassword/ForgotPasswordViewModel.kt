package com.example.calmlearn.ui.forgotpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmlearn.data.auth.AuthErrorReason
import com.example.calmlearn.data.auth.AuthRepository
import com.example.calmlearn.data.auth.AuthRepositoryProvider
import com.example.calmlearn.data.auth.AuthResult
import com.example.calmlearn.ui.common.FormUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val repository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

    var email: String = ""
        private set

    /** Chi hien loi email sau khi nguoi dung da tuong tac (go chu hoac roi khoi o), khong do loi
     *  ngay khi vua mo man hinh. */
    private var emailTouched = false

    private val _fieldError = MutableLiveData<Int?>(null)
    val fieldError: LiveData<Int?> = _fieldError

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    private val _uiState = MutableLiveData<FormUiState>(FormUiState.Idle)
    val uiState: LiveData<FormUiState> = _uiState

    fun onEmailChanged(value: String) {
        email = value
        onEmailTouched()
    }

    fun onEmailBlurred() {
        onEmailTouched()
    }

    private fun onEmailTouched() {
        emailTouched = true
        if (_uiState.value is FormUiState.Error) {
            _uiState.value = FormUiState.Idle
        }
        recomputeError()
    }

    private fun recomputeError() {
        val error = ForgotPasswordFormValidator.emailError(email.trim())
        _isFormValid.value = error == null
        _fieldError.value = if (emailTouched) error else null
    }

    fun submit() {
        if (_uiState.value == FormUiState.Loading) return

        emailTouched = true
        val trimmedEmail = email.trim()
        val error = ForgotPasswordFormValidator.emailError(trimmedEmail)
        _fieldError.value = error
        if (error != null) {
            _isFormValid.value = false
            return
        }

        _uiState.value = FormUiState.Loading
        viewModelScope.launch {
            // Thong bao trung tinh: chi bao thanh cong khi dich vu that tra ve Success,
            // khong tiet lo email co ton tai trong he thong hay khong.
            val result = try {
                repository.sendPasswordResetEmail(trimmedEmail)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (unexpected: Exception) {
                AuthResult.Error(AuthErrorReason.UNKNOWN)
            }
            _uiState.value = when (result) {
                is AuthResult.Success -> FormUiState.Success
                is AuthResult.Error -> FormUiState.Error(result.reason)
            }
        }
    }
}
