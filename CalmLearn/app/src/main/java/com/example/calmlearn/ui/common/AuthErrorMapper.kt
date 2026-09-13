package com.example.calmlearn.ui.common

import androidx.annotation.StringRes
import com.example.calmlearn.R
import com.example.calmlearn.data.auth.AuthErrorReason

/** Anh xa ly do loi (tang data, khong phu thuoc Android) sang string resource de hien thi. */
@StringRes
fun AuthErrorReason.toMessageRes(): Int = when (this) {
    AuthErrorReason.SERVICE_NOT_CONFIGURED -> R.string.auth_error_service_not_configured
    AuthErrorReason.NETWORK_ERROR -> R.string.auth_error_network
    AuthErrorReason.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
    AuthErrorReason.UNKNOWN -> R.string.auth_error_unknown
}
