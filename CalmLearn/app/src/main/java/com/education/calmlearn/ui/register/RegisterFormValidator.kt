package com.education.calmlearn.ui.register

import androidx.annotation.StringRes
import com.education.calmlearn.R
import com.education.calmlearn.data.auth.Gender
import com.education.calmlearn.util.Validators

data class RegisterInput(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val gender: Gender?,
    val agreedToTerms: Boolean
)

enum class RegisterField {
    FULL_NAME, EMAIL, PASSWORD, CONFIRM_PASSWORD, GENDER, TERMS
}

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

    fun visibleTo(touched: Set<RegisterField>): RegisterFieldErrors = RegisterFieldErrors(
        fullName = fullName.takeIf { RegisterField.FULL_NAME in touched },
        email = email.takeIf { RegisterField.EMAIL in touched },
        password = password.takeIf { RegisterField.PASSWORD in touched },
        confirmPassword = confirmPassword.takeIf { RegisterField.CONFIRM_PASSWORD in touched },
        gender = gender.takeIf { RegisterField.GENDER in touched },
        terms = terms.takeIf { RegisterField.TERMS in touched }
    )
}

object RegisterFormValidator {

    const val MIN_PASSWORD_LENGTH = 6

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
