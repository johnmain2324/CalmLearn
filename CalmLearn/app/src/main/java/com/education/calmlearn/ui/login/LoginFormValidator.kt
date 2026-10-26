package com.education.calmlearn.ui.login

import androidx.annotation.StringRes
import com.education.calmlearn.R
import com.education.calmlearn.util.Validators

data class LoginInput(
    val email: String,
    val password: String
)

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

    fun errors(input: LoginInput): LoginFieldErrors = LoginFieldErrors(
        email = if (!Validators.isValidEmail(input.email)) R.string.login_error_email else null,
        password = if (input.password.isEmpty()) R.string.login_error_password else null
    )
}
