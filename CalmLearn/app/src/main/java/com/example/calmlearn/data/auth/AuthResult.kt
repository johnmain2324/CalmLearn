package com.example.calmlearn.data.auth

/** Ly do that bai o muc dich hien thi (UI anh xa sang string resource, khong hardcode chuoi o day). */
enum class AuthErrorReason {
    /** Chua co dich vu xac thuc that duoc noi (xem [UnavailableAuthRepository]). */
    SERVICE_NOT_CONFIGURED,
    NETWORK_ERROR,
    INVALID_CREDENTIALS,
    UNKNOWN
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val reason: AuthErrorReason) : AuthResult()
}
