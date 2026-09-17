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

/** Man hinh nay co 2 che do tuy vao trang thai that cua tai khoan dang "cho hoan tat". */
sealed class VerifyEmailMode {
    object Loading : VerifyEmailMode()

    /** Ho so Firestore CHUA co (dang ky bi gian doan truoc buoc luu ho so) - can nhap lai ten/gioi tinh. */
    data class ProfileForm(val email: String) : VerifyEmailMode()

    /** Ho so da co, chi con thieu xac minh email. */
    data class Verify(val email: String) : VerifyEmailMode()
}

/**
 * ViewModel cho man hinh Xac minh email / Hoan tat ho so - noi den tu Dang ky (tai khoan vua tao)
 * hoac Dang nhap (phat hien tai khoan chua xac minh/chua co ho so). Dung CHUNG mot FirebaseUser
 * dang duoc giu (xem FirebaseAuthRepository) cho ca hai truong hop, khong yeu cau dang nhap lai.
 */
class VerifyEmailViewModel(
    private val repository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

    private val _mode = MutableLiveData<VerifyEmailMode>(VerifyEmailMode.Loading)
    val mode: LiveData<VerifyEmailMode> = _mode

    /** Trang thai cho CA HAI thao tac (luu ho so, gui lai, kiem tra lai) - chi mot thao tac chay
     *  tai mot thoi diem theo tung che do nen dung chung an toan. */
    private val _uiState = MutableLiveData<FormUiState>(FormUiState.Idle)
    val uiState: LiveData<FormUiState> = _uiState

    /** Thong bao ngan hen (Toast) khong phai loi - vd "da gui lai", "van chua xac minh". */
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

    /** Goi mot lan tu Fragment.onViewCreated(). [autoSend] chi true khi vua tu Dang ky sang (chua
     *  tung gui email xac minh nao) - tu Dang nhap phat hien "chua xac minh" thi KHONG tu gui lai
     *  (co the da gui truoc do, tranh spam). */
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

    /** Luu ho so (khi thieu) roi gui email xac minh ngay sau do - gop lam mot thao tac cho nguoi dung. */
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

    /** Gui (hoac gui lai) email xac minh. An toan khi goi nhieu lan - tu chan trung luc dang Loading. */
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

    /** "Toi da xac minh - Kiem tra lai": tai lai du lieu tu Firebase (khong chi doc cache cu). */
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

    /** Nguoi dung chu dong roi man hinh ma chua hoan tat (Back / "De sau") - huy phien dang cho. */
    fun abandon() {
        repository.cancelPendingSession()
    }

    fun onInfoEventConsumed() {
        _infoEvent.value = null
    }

    /** Xem RegisterViewModel.consumeTerminalState(): tranh Success bi "phat lai" gay dieu huong nhieu lan. */
    fun consumeTerminalState() {
        if (_uiState.value == FormUiState.Success) {
            _uiState.value = FormUiState.Idle
        }
    }
}
