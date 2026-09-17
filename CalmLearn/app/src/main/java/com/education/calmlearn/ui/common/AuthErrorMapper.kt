package com.education.calmlearn.ui.common

import androidx.annotation.StringRes
import com.education.calmlearn.R
import com.education.calmlearn.data.auth.AuthErrorReason

/** Anh xa ly do loi (tang data, khong phu thuoc Android) sang string resource de hien thi. */
@StringRes
fun AuthErrorReason.toMessageRes(): Int = when (this) {
    AuthErrorReason.SERVICE_NOT_CONFIGURED -> R.string.auth_error_service_not_configured
    AuthErrorReason.NETWORK_ERROR -> R.string.auth_error_network
    AuthErrorReason.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
    AuthErrorReason.EMAIL_ALREADY_IN_USE -> R.string.auth_error_email_in_use
    AuthErrorReason.WEAK_PASSWORD -> R.string.auth_error_weak_password
    AuthErrorReason.TOO_MANY_REQUESTS -> R.string.auth_error_too_many_requests
    AuthErrorReason.PROFILE_SAVE_FAILED -> R.string.auth_error_profile_save_failed
    AuthErrorReason.UNKNOWN -> R.string.auth_error_unknown
}
