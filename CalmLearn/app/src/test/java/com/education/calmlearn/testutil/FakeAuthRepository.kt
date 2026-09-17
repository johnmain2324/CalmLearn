package com.education.calmlearn.testutil

import com.education.calmlearn.data.auth.AuthErrorReason
import com.education.calmlearn.data.auth.AuthRepository
import com.education.calmlearn.data.auth.AuthResult
import com.education.calmlearn.data.auth.Gender
import com.education.calmlearn.data.auth.LoginResult
import com.education.calmlearn.data.auth.ProfileResult
import com.education.calmlearn.data.auth.RegisterResult
import com.education.calmlearn.data.auth.UserProfile
import com.education.calmlearn.data.auth.VerificationCheckResult
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
    var resendCallCount = 0
        private set
    var refreshCallCount = 0
        private set
    var completeProfileCallCount = 0
        private set
    var cancelPendingSessionCallCount = 0
        private set

    var registerResult: RegisterResult = RegisterResult.AccountReady
    var loginResult: LoginResult = LoginResult.Success
    var resetResult: AuthResult = AuthResult.Success
    var resendResult: AuthResult = AuthResult.Success
    var refreshResult: VerificationCheckResult = VerificationCheckResult.VERIFIED
    var completeProfileResult: AuthResult = AuthResult.Success
    var throwOnNextCall: Throwable? = null
    var profile: ProfileResult = ProfileResult.NotSignedIn
    var sessionEmail: String? = null

    private var authenticated = false

    private var registerGate: CompletableDeferred<Unit>? = null
    private var loginGate: CompletableDeferred<Unit>? = null

    fun setAuthenticated(value: Boolean) {
        authenticated = value
    }

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

    override suspend fun login(email: String, password: String): LoginResult {
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

    override fun currentSessionEmail(): String? = sessionEmail

    override fun logout() {
        authenticated = false
        profile = ProfileResult.NotSignedIn
    }

    override fun cancelPendingSession() {
        cancelPendingSessionCallCount++
    }

    override suspend fun currentUserProfile(): ProfileResult = profile

    override suspend fun resendVerificationEmail(): AuthResult {
        resendCallCount++
        throwOnNextCall?.let { pending -> throwOnNextCall = null; throw pending }
        return resendResult
    }

    override suspend fun refreshVerificationStatus(): VerificationCheckResult {
        refreshCallCount++
        throwOnNextCall?.let { pending -> throwOnNextCall = null; throw pending }
        return refreshResult
    }

    override suspend fun completeProfile(fullName: String, gender: Gender): AuthResult {
        completeProfileCallCount++
        throwOnNextCall?.let { pending -> throwOnNextCall = null; throw pending }
        if (completeProfileResult is AuthResult.Success) {
            profile = ProfileResult.Loaded(UserProfile(uid = "uid", fullName = fullName, email = sessionEmail.orEmpty(), gender = gender))
        }
        return completeProfileResult
    }
}
