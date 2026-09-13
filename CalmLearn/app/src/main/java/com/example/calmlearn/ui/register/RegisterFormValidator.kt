package com.example.calmlearn.ui.register

import com.example.calmlearn.R
import com.example.calmlearn.data.auth.Gender
import com.example.calmlearn.util.Validators

data class RegisterInput(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val gender: Gender?,
    val agreedToTerms: Boolean
)

/**
 * Kiem tra form Dang ky, tach rieng khoi Fragment/ViewModel de unit test bang JUnit thuong.
 */
object RegisterFormValidator {

    /**
     * Quy tac tam thoi (chua co chinh sach mat khau that tu backend): toi thieu 6 ky tu.
     * Cap nhat gia tri nay khi nhom chot chinh sach mat khau that.
     */
    const val MIN_PASSWORD_LENGTH = 6

    /** Tra ve string resource id cua loi dau tien tim thay, hoac null neu form hop le. */
    fun firstError(input: RegisterInput): Int? = when {
        Validators.isBlank(input.fullName) -> R.string.register_error_fullname
        !Validators.isValidEmail(input.email) -> R.string.register_error_email
        input.password.length < MIN_PASSWORD_LENGTH -> R.string.register_error_password
        input.confirmPassword != input.password -> R.string.register_error_confirm_password
        input.gender == null -> R.string.register_error_gender
        !input.agreedToTerms -> R.string.register_error_terms
        else -> null
    }
}
