package com.education.calmlearn.ui.forgotpassword

import com.education.calmlearn.R
import com.education.calmlearn.util.Validators

object ForgotPasswordFormValidator {

    fun emailError(email: String): Int? = when {
        !Validators.isValidEmail(email) -> R.string.register_error_email
        else -> null
    }
}
