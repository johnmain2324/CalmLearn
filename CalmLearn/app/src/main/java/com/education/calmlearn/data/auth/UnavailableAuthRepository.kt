package com.education.calmlearn.data.auth

import kotlinx.coroutines.delay

/**
 * Implementation "cho" cua [AuthRepository]: khong tao tai khoan gia, khong bao gio tra ve
 * Success - chi mo phong do tre mang (de UI co the demo trang thai Loading) roi bao loi ro rang la
 * dich vu chua duoc cau hinh. Khong con duoc [AuthRepositoryProvider] su dung (da chuyen sang
 * [FirebaseAuthRepository]) - giu lai lam stub tham khao / de dung khi can chay UI ma khong co
 * `google-services.json` that (vd demo giao dien don thuan).
 */
class UnavailableAuthRepository : AuthRepository {

    override suspend fun register(fullName: String, email: String, password: String, gender: Gender): RegisterResult {
        delay(SIMULATED_DELAY_MS)
        return RegisterResult.Error(AuthErrorReason.SERVICE_NOT_CONFIGURED)
    }

    override suspend fun login(email: String, password: String): LoginResult {
        delay(SIMULATED_DELAY_MS)
        return LoginResult.Error(AuthErrorReason.SERVICE_NOT_CONFIGURED)
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        delay(SIMULATED_DELAY_MS)
        return AuthResult.Error(AuthErrorReason.SERVICE_NOT_CONFIGURED)
    }

    override suspend fun signInWithGoogle(): AuthResult {
        return AuthResult.Error(AuthErrorReason.SERVICE_NOT_CONFIGURED)
    }

    override fun isAuthenticated(): Boolean = false

    override fun currentSessionEmail(): String? = null

    override fun logout() = Unit

    override fun cancelPendingSession() = Unit

    override suspend fun currentUserProfile(): ProfileResult = ProfileResult.NotSignedIn

    override suspend fun resendVerificationEmail(): AuthResult {
        delay(SIMULATED_DELAY_MS)
        return AuthResult.Error(AuthErrorReason.SERVICE_NOT_CONFIGURED)
    }

    override suspend fun refreshVerificationStatus(): VerificationCheckResult {
        delay(SIMULATED_DELAY_MS)
        return VerificationCheckResult.CHECK_FAILED
    }

    override suspend fun completeProfile(fullName: String, gender: Gender): AuthResult {
        delay(SIMULATED_DELAY_MS)
        return AuthResult.Error(AuthErrorReason.SERVICE_NOT_CONFIGURED)
    }

    private companion object {
        const val SIMULATED_DELAY_MS = 600L
    }
}
