package com.education.calmlearn.data.auth

enum class AuthErrorReason {

    SERVICE_NOT_CONFIGURED,
    NETWORK_ERROR,
    INVALID_CREDENTIALS,

    EMAIL_ALREADY_IN_USE,

    WEAK_PASSWORD,

    TOO_MANY_REQUESTS,

    PROFILE_SAVE_FAILED,
    UNKNOWN
}

enum class VerificationCheckResult {
    VERIFIED,
    NOT_YET_VERIFIED,
    CHECK_FAILED
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val reason: AuthErrorReason) : AuthResult()
}

sealed class RegisterResult {

    object AccountReady : RegisterResult()

    object ProfileIncomplete : RegisterResult()

    data class Error(val reason: AuthErrorReason) : RegisterResult()
}

sealed class LoginResult {
    object Success : LoginResult()
    object RequiresVerification : LoginResult()
    object ProfileIncomplete : LoginResult()
    data class Error(val reason: AuthErrorReason) : LoginResult()
}

sealed class ProfileResult {
    data class Loaded(val profile: UserProfile) : ProfileResult()

    object NotSignedIn : ProfileResult()

    object Missing : ProfileResult()

    data class ReadError(val reason: AuthErrorReason) : ProfileResult()
}
