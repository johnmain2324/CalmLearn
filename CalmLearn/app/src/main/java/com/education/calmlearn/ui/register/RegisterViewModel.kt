package com.education.calmlearn.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.education.calmlearn.data.auth.AuthErrorReason
import com.education.calmlearn.data.auth.AuthRepository
import com.education.calmlearn.data.auth.AuthRepositoryProvider
import com.education.calmlearn.data.auth.Gender
import com.education.calmlearn.data.auth.RegisterResult
import com.education.calmlearn.ui.common.FormUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

    var fullName: String = ""
        private set
    var email: String = ""
        private set
    var password: String = ""
        private set
    var confirmPassword: String = ""
        private set
    var gender: Gender? = null
        private set
    var agreedToTerms: Boolean = false
        private set

    /** Trang thai an/hien cua tung o mat khau - luu o day de song sot qua luc xoay man hinh
     *  (o mat khau duoc dat android:saveEnabled="false" nen Fragment phai tu ap lai tu day). */
    var isPasswordVisible: Boolean = false
        private set
    var isConfirmPasswordVisible: Boolean = false
        private set

    /** Cac truong nguoi dung da tung tuong tac (go chu hoac roi khoi o) - chi loi cua truong da
     *  "touched" moi duoc hien ra, tranh do loi ngay khi vua mo man hinh. */
    private val touchedFields = mutableSetOf<RegisterField>()

    private val _fieldErrors = MutableLiveData(RegisterFieldErrors())
    val fieldErrors: LiveData<RegisterFieldErrors> = _fieldErrors

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    private val _uiState = MutableLiveData<FormUiState>(FormUiState.Idle)
    val uiState: LiveData<FormUiState> = _uiState

    fun onFullNameChanged(value: String) {
        fullName = value
        onFieldTouched(RegisterField.FULL_NAME)
    }

    fun onEmailChanged(value: String) {
        email = value
        onFieldTouched(RegisterField.EMAIL)
    }

    fun onPasswordChanged(value: String) {
        password = value
        // Doi mat khau goc co the lam sai lech xac nhan mat khau - recompute() ben duoi se tu
        // danh gia lai loi confirmPassword neu o do da duoc tuong tac truoc do.
        onFieldTouched(RegisterField.PASSWORD)
    }

    fun onConfirmPasswordChanged(value: String) {
        confirmPassword = value
        onFieldTouched(RegisterField.CONFIRM_PASSWORD)
    }

    fun onGenderChanged(value: Gender?) {
        gender = value
        onFieldTouched(RegisterField.GENDER)
    }

    fun onTermsChanged(value: Boolean) {
        agreedToTerms = value
        onFieldTouched(RegisterField.TERMS)
    }

    fun onPasswordVisibilityChanged(visible: Boolean) {
        isPasswordVisible = visible
    }

    fun onConfirmPasswordVisibilityChanged(visible: Boolean) {
        isConfirmPasswordVisible = visible
    }

    /** Goi khi mot o rieng le mat focus (vd nguoi dung tab qua ma khong go gi) de danh dau touched. */
    fun onFieldBlurred(field: RegisterField) {
        onFieldTouched(field)
    }

    private fun onFieldTouched(field: RegisterField) {
        touchedFields.add(field)
        clearServiceErrorIfStale()
        recomputeErrors()
    }

    /** Loi tu dich vu (backend) khong con dung sau khi nguoi dung sua du lieu - xoa de tranh
     *  hien lai loi cu gay hieu lam. */
    private fun clearServiceErrorIfStale() {
        if (_uiState.value is FormUiState.Error) {
            _uiState.value = FormUiState.Idle
        }
    }

    private fun recomputeErrors() {
        val all = RegisterFormValidator.errors(currentInput())
        _isFormValid.value = all.isValid
        _fieldErrors.value = all.visibleTo(touchedFields)
    }

    fun submit() {
        if (_uiState.value == FormUiState.Loading) return // chan gui yeu cau trung

        // Bam Dang ky = coi nhu da "tuong tac" toan bo form, de hien du loi con thieu/sai.
        touchedFields.addAll(RegisterField.entries)
        val input = currentInput()
        val allErrors = RegisterFormValidator.errors(input)
        _fieldErrors.value = allErrors
        if (!allErrors.isValid) {
            _isFormValid.value = false
            return
        }

        _uiState.value = FormUiState.Loading
        viewModelScope.launch {
            val result = try {
                repository.register(input.fullName, input.email, password, input.gender!!)
            } catch (cancellation: CancellationException) {
                throw cancellation // khong duoc nuot cancellation cua coroutine
            } catch (unexpected: Exception) {
                RegisterResult.Error(AuthErrorReason.UNKNOWN)
            }
            _uiState.value = when (result) {
                RegisterResult.AccountReady,
                RegisterResult.ProfileIncomplete -> FormUiState.RequiresNextStep
                is RegisterResult.Error -> FormUiState.Error(result.reason)
            }
        }
    }

    /** Goi ngay sau khi Fragment da xu ly xong Success/RequiresNextStep (vd da dieu huong) de
     *  trang thai nay khong bi "phat lai" va dieu huong them lan nua neu observer duoc dang ky lai
     *  (vi du do xoay man hinh dung luc). */
    fun consumeTerminalState() {
        if (_uiState.value == FormUiState.Success || _uiState.value == FormUiState.RequiresNextStep) {
            _uiState.value = FormUiState.Idle
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
