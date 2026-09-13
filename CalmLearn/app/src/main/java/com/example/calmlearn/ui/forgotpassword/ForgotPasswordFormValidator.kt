package com.example.calmlearn.ui.forgotpassword

import com.example.calmlearn.R
import com.example.calmlearn.util.Validators

object ForgotPasswordFormValidator {

    /** Tra ve string resource id cua loi, hoac null neu email hop le. */
    fun firstError(email: String): Int? = when {
        !Validators.isValidEmail(email) -> R.string.register_error_email
        else -> null
    }
}
