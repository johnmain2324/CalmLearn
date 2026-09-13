package com.example.calmlearn.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmlearn.data.auth.AuthErrorReason
import com.example.calmlearn.data.auth.AuthRepository
import com.example.calmlearn.data.auth.AuthRepositoryProvider
import com.example.calmlearn.data.auth.LoginResult
import com.example.calmlearn.ui.common.FormUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

    var email: String = ""
        private set
    var password: String = ""
        private set
    var isPasswordVisible: Boolean = false
        private set

    private val touchedFields = mutableSetOf<LoginField>()

    private val _fieldErrors = MutableLiveData(LoginFieldErrors())
    val fieldErrors: LiveData<LoginFieldErrors> = _fieldErrors

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    private val _uiState = MutableLiveData<FormUiState>(FormUiState.Idle)
    val uiState: LiveData<FormUiState> = _uiState

    private val _googleUnavailableEvent = MutableLiveData<Unit?>(null)
    val googleUnavailableEvent: LiveData<Unit?> = _googleUnavailableEvent

    fun onEmailChanged(value: String) {
        email = value
        onFieldTouched(LoginField.EMAIL)
    }

    fun onPasswordChanged(value: String) {
        password = value
        onFieldTouched(LoginField.PASSWORD)
    }

    fun onPasswordVisibilityChanged(visible: Boolean) {
        isPasswordVisible = visible
    }

    fun onFieldBlurred(field: LoginField) {
        onFieldTouched(field)
    }

    private fun onFieldTouched(field: LoginField) {
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
        val all = LoginFormValidator.errors(currentInput())
        _isFormValid.value = all.isValid
        _fieldErrors.value = all.visibleTo(touchedFields)
    }

    fun submit() {
        if (_uiState.value == FormUiState.Loading) return

        touchedFields.addAll(LoginField.entries)
        val input = currentInput()
        val allErrors = LoginFormValidator.errors(input)
        _fieldErrors.value = allErrors
        if (!allErrors.isValid) {
            _isFormValid.value = false
            return
        }

        _uiState.value = FormUiState.Loading
        viewModelScope.launch {
            val result = try {
                repository.login(input.email, password)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (unexpected: Exception) {
                LoginResult.Error(AuthErrorReason.UNKNOWN)
            }
            _uiState.value = when (result) {
                LoginResult.Success -> FormUiState.Success
                LoginResult.RequiresVerification,
                LoginResult.ProfileIncomplete -> FormUiState.RequiresNextStep
                is LoginResult.Error -> FormUiState.Error(result.reason)
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

    /** Xem RegisterViewModel.consumeTerminalState(): tranh Success/RequiresNextStep bi "phat lai" gay dieu huong nhieu lan. */
    fun consumeTerminalState() {
        if (_uiState.value == FormUiState.Success || _uiState.value == FormUiState.RequiresNextStep) {
            _uiState.value = FormUiState.Idle
        }
    }

    private fun currentInput() = LoginInput(email = email.trim(), password = password)
}
