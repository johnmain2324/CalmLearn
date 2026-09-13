package com.example.calmlearn.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmlearn.data.auth.AuthRepository
import com.example.calmlearn.data.auth.AuthRepositoryProvider
import com.example.calmlearn.data.auth.AuthResult
import com.example.calmlearn.data.auth.Gender
import com.example.calmlearn.ui.common.FormUiState
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

    var fullName: String = ""
    var email: String = ""
    var password: String = ""
    var confirmPassword: String = ""
    var gender: Gender? = null
    var agreedToTerms: Boolean = false

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    private val _fieldError = MutableLiveData<Int?>(null)
    val fieldError: LiveData<Int?> = _fieldError

    private val _uiState = MutableLiveData<FormUiState>(FormUiState.Idle)
    val uiState: LiveData<FormUiState> = _uiState

    /** Goi moi khi mot truong bat ky thay doi de cap nhat trang thai bat/tat nut Dang ky. */
    fun onFieldChanged() {
        _fieldError.value = null
        _isFormValid.value = RegisterFormValidator.firstError(currentInput()) == null
    }

    fun submit() {
        if (_uiState.value == FormUiState.Loading) return

        val input = currentInput()
        val error = RegisterFormValidator.firstError(input)
        if (error != null) {
            _fieldError.value = error
            return
        }

        _fieldError.value = null
        _uiState.value = FormUiState.Loading
        viewModelScope.launch {
            val result = repository.register(input.fullName, input.email, password, input.gender!!)
            _uiState.value = when (result) {
                is AuthResult.Success -> FormUiState.Success
                is AuthResult.Error -> FormUiState.Error(result.reason)
            }
        }
    }

    private fun currentInput() = RegisterInput(
        fullName = fullName.trim(),
        email = email.trim(),
        password = password,
        confirmPassword = confirmPassword,
        gender = gender,
        agreedToTerms = agreedToTerms
    )
}
