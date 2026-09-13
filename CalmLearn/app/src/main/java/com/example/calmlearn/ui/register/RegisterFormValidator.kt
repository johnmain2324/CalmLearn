package com.example.calmlearn.ui.register

import androidx.annotation.StringRes
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

/** Cac truong cua form Dang ky, dung de theo doi truong nao nguoi dung da "tuong tac" (touched). */
enum class RegisterField {
    FULL_NAME, EMAIL, PASSWORD, CONFIRM_PASSWORD, GENDER, TERMS
}

/**
 * Loi cua TUNG truong (khac voi kieu "loi dau tien tim thay" truoc day) de co the hien loi rieng
 * ngay canh moi o nhap thay vi chi mot dong loi dung chung cho ca form.
 */
data class RegisterFieldErrors(
    @StringRes val fullName: Int? = null,
    @StringRes val email: Int? = null,
    @StringRes val password: Int? = null,
    @StringRes val confirmPassword: Int? = null,
    @StringRes val gender: Int? = null,
    @StringRes val terms: Int? = null
) {
    val isValid: Boolean
        get() = fullName == null && email == null && password == null &&
            confirmPassword == null && gender == null && terms == null

    /** Chi giu lai loi cua nhung truong da co trong [touched] - dung de khong hien loi khi vua mo man hinh. */
    fun visibleTo(touched: Set<RegisterField>): RegisterFieldErrors = RegisterFieldErrors(
        fullName = fullName.takeIf { RegisterField.FULL_NAME in touched },
        email = email.takeIf { RegisterField.EMAIL in touched },
        password = password.takeIf { RegisterField.PASSWORD in touched },
        confirmPassword = confirmPassword.takeIf { RegisterField.CONFIRM_PASSWORD in touched },
        gender = gender.takeIf { RegisterField.GENDER in touched },
        terms = terms.takeIf { RegisterField.TERMS in touched }
    )
}

/**
 * Kiem tra form Dang ky, tach rieng khoi Fragment/ViewModel de unit test bang JUnit thuong.
 */
object RegisterFormValidator {

    /**
     * Quy tac tam thoi (chua co chinh sach mat khau that tu backend): toi thieu 6 ky tu.
     * Cap nhat gia tri nay khi nhom chot chinh sach mat khau that.
     */
    const val MIN_PASSWORD_LENGTH = 6

    /** Tra ve loi cua TAT CA cac truong cung luc (khong dung short-circuit "loi dau tien"). */
    fun errors(input: RegisterInput): RegisterFieldErrors = RegisterFieldErrors(
        fullName = if (Validators.isBlank(input.fullName)) R.string.register_error_fullname else null,
        email = if (!Validators.isValidEmail(input.email)) R.string.register_error_email else null,
        password = if (input.password.length < MIN_PASSWORD_LENGTH) R.string.register_error_password else null,
        confirmPassword = if (input.confirmPassword != input.password) {
            R.string.register_error_confirm_password
        } else {
            null
        },
        gender = if (input.gender == null) R.string.register_error_gender else null,
        terms = if (!input.agreedToTerms) R.string.register_error_terms else null
    )
}
