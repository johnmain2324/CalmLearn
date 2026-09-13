package com.example.calmlearn.testutil

import com.example.calmlearn.data.auth.AuthErrorReason
import com.example.calmlearn.data.auth.AuthRepository
import com.example.calmlearn.data.auth.AuthResult
import com.example.calmlearn.data.auth.Gender
import com.example.calmlearn.data.auth.RegisterResult
import com.example.calmlearn.data.auth.UserProfile
import kotlinx.coroutines.CompletableDeferred

/**
 * AuthRepository gia lap CHI dung trong unit test (khong duoc dua vao luong ung dung chinh -
 * AuthRepositoryProvider dung FirebaseAuthRepository that). Cho phep:
 * - Dat san ket qua tra ve (Success/Error/Verification...) cho tung phuong thuc.
 * - "Giu" (hold) mot loi goi de kiem tra trang thai Loading giua chung, roi "tha" (release) khi test san sang.
 * - Gia lap ngoai le bat ngo tu dich vu (throwOnNextCall) de kiem tra ViewModel khong bi ket Loading.
 * - Dem so lan goi de kiem tra khong gui yeu cau trung.
 */
class FakeAuthRepository : AuthRepository {

    var registerCallCount = 0
        private set
    var loginCallCount = 0
        private set
    var resetCallCount = 0
        private set

    var registerResult: RegisterResult = RegisterResult.SignedIn
    var loginResult: AuthResult = AuthResult.Success
    var resetResult: AuthResult = AuthResult.Success
    var throwOnNextCall: Throwable? = null
    var profile: UserProfile? = null

    private var authenticated = false

    fun setAuthenticated(value: Boolean) {
        authenticated = value
    }

    private var registerGate: CompletableDeferred<Unit>? = null
    private var loginGate: CompletableDeferred<Unit>? = null

    fun holdRegister() {
        registerGate = CompletableDeferred()
    }

    fun releaseRegisterGate() {
        registerGate?.complete(Unit)
    }

    fun holdLogin() {
        loginGate = CompletableDeferred()
    }

    fun releaseLoginGate() {
        loginGate?.complete(Unit)
    }

    override suspend fun register(fullName: String, email: String, password: String, gender: Gender): RegisterResult {
        registerCallCount++
        registerGate?.await()
        throwOnNextCall?.let { pending -> throwOnNextCall = null; throw pending }
        return registerResult
    }

    override suspend fun login(email: String, password: String, rememberMe: Boolean): AuthResult {
        loginCallCount++
        loginGate?.await()
        throwOnNextCall?.let { pending -> throwOnNextCall = null; throw pending }
        return loginResult
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        resetCallCount++
        throwOnNextCall?.let { pending -> throwOnNextCall = null; throw pending }
        return resetResult
    }

    override suspend fun signInWithGoogle(): AuthResult = AuthResult.Error(AuthErrorReason.SERVICE_NOT_CONFIGURED)

    override fun isAuthenticated(): Boolean = authenticated

    override fun logout() {
        authenticated = false
        profile = null
    }

    override suspend fun currentUserProfile(): UserProfile? = profile
}
