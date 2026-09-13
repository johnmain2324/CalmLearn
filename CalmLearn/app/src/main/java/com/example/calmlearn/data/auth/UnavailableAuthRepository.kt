package com.example.calmlearn.data.auth

import kotlinx.coroutines.delay

/**
 * Implementation "tam" duy nhat cua [AuthRepository] khi project chua co backend/dich vu xac
 * thuc that. Khong tao tai khoan gia, khong bao gio tra ve Success - chi mo phong do tre mang
 * (de UI co the demo trang thai Loading) roi bao loi ro rang la dich vu chua duoc cau hinh.
 *
 * TODO(nhom): thay the bang implementation that (Firebase Auth / REST API backend) sau khi
 * nhom chot phuong an xac thuc.
 */
class UnavailableAuthRepository : AuthRepository {

    override suspend fun register(fullName: String, email: String, password: String, gender: Gender): RegisterResult {
        delay(SIMULATED_DELAY_MS)
        return RegisterResult.Error(AuthErrorReason.SERVICE_NOT_CONFIGURED)
    }

    override suspend fun login(email: String, password: String, rememberMe: Boolean): AuthResult {
        delay(SIMULATED_DELAY_MS)
        return AuthResult.Error(AuthErrorReason.SERVICE_NOT_CONFIGURED)
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        delay(SIMULATED_DELAY_MS)
        return AuthResult.Error(AuthErrorReason.SERVICE_NOT_CONFIGURED)
    }

    override suspend fun signInWithGoogle(): AuthResult {
        return AuthResult.Error(AuthErrorReason.SERVICE_NOT_CONFIGURED)
    }

    override fun isAuthenticated(): Boolean = false

    override fun logout() = Unit

    private companion object {
        const val SIMULATED_DELAY_MS = 600L
    }
}
