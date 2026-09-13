package com.example.calmlearn.ui.login

import androidx.annotation.StringRes
import com.example.calmlearn.R
import com.example.calmlearn.util.Validators

data class LoginInput(
    val email: String,
    val password: String
)

/** Cac truong cua form Dang nhap, dung de theo doi truong nao nguoi dung da tuong tac (touched). */
enum class LoginField { EMAIL, PASSWORD }

data class LoginFieldErrors(
    @StringRes val email: Int? = null,
    @StringRes val password: Int? = null
) {
    val isValid: Boolean get() = email == null && password == null

    fun visibleTo(touched: Set<LoginField>): LoginFieldErrors = LoginFieldErrors(
        email = email.takeIf { LoginField.EMAIL in touched },
        password = password.takeIf { LoginField.PASSWORD in touched }
    )
}

object LoginFormValidator {

    /**
     * Tra ve loi cua TAT CA cac truong cung luc. Luu y: mat khau CHI kiem tra rong hay khong -
     * khong ap lai chinh sach do dai/dinh dang cua form Dang ky cho tai khoan cu (email dung dinh
     * dang va mat khau khong rong khong co nghia da xac thuc thanh cong - viec do thuoc ve
     * AuthRepository that, xem LoginViewModel.submit()).
     */
    fun errors(input: LoginInput): LoginFieldErrors = LoginFieldErrors(
        email = if (!Validators.isValidEmail(input.email)) R.string.login_error_email else null,
        password = if (input.password.isEmpty()) R.string.login_error_password else null
    )
}
