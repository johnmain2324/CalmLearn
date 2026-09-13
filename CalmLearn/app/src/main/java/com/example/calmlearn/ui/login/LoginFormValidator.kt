package com.example.calmlearn.ui.login

import com.example.calmlearn.R
import com.example.calmlearn.util.Validators

data class LoginInput(
    val email: String,
    val password: String
)

object LoginFormValidator {

    /** Tra ve string resource id cua loi dau tien tim thay, hoac null neu form hop le. */
    fun firstError(input: LoginInput): Int? = when {
        !Validators.isValidEmail(input.email) -> R.string.login_error_email
        input.password.isEmpty() -> R.string.login_error_password
        else -> null
    }
}
