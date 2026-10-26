package com.education.calmlearn.data.auth

interface AuthRepository {

    suspend fun register(fullName: String, email: String, password: String, gender: Gender): RegisterResult

    suspend fun login(email: String, password: String): LoginResult

    suspend fun sendPasswordResetEmail(email: String): AuthResult

    suspend fun signInWithGoogle(): AuthResult

    fun isAuthenticated(): Boolean

    fun currentSessionEmail(): String?

    fun logout()

    suspend fun currentUserProfile(): ProfileResult

    suspend fun resendVerificationEmail(): AuthResult

    suspend fun refreshVerificationStatus(): VerificationCheckResult

    suspend fun completeProfile(fullName: String, gender: Gender): AuthResult

    fun cancelPendingSession()
}
