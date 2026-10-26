package com.education.calmlearn.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override suspend fun register(fullName: String, email: String, password: String, gender: Gender): RegisterResult {
        val firebaseUser = try {
            auth.createUserWithEmailAndPassword(email, password).await().user
                ?: return RegisterResult.Error(AuthErrorReason.UNKNOWN)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (collision: FirebaseAuthUserCollisionException) {
            return RegisterResult.Error(AuthErrorReason.EMAIL_ALREADY_IN_USE)
        } catch (weakPassword: FirebaseAuthWeakPasswordException) {
            return RegisterResult.Error(AuthErrorReason.WEAK_PASSWORD)
        } catch (invalidEmail: FirebaseAuthInvalidCredentialsException) {
            return RegisterResult.Error(AuthErrorReason.INVALID_CREDENTIALS)
        } catch (network: FirebaseNetworkException) {
            return RegisterResult.Error(AuthErrorReason.NETWORK_ERROR)
        } catch (unexpected: Exception) {
            return RegisterResult.Error(AuthErrorReason.UNKNOWN)
        }

        val profileSaved = try {
            saveProfile(uid = firebaseUser.uid, fullName = fullName, email = email, gender = gender)
            true
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (profileError: Exception) {
            false
        }

        if (!profileSaved) {
            return RegisterResult.ProfileIncomplete
        }

        try {
            firebaseUser.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(fullName).build()).await()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (ignored: Exception) {

        }

        return RegisterResult.AccountReady
    }

    override suspend fun login(email: String, password: String): LoginResult {
        val user = try {
            auth.signInWithEmailAndPassword(email, password).await().user
                ?: return LoginResult.Error(AuthErrorReason.UNKNOWN)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (invalidUser: FirebaseAuthInvalidUserException) {
            return LoginResult.Error(AuthErrorReason.INVALID_CREDENTIALS)
        } catch (invalidCredentials: FirebaseAuthInvalidCredentialsException) {
            return LoginResult.Error(AuthErrorReason.INVALID_CREDENTIALS)
        } catch (network: FirebaseNetworkException) {
            return LoginResult.Error(AuthErrorReason.NETWORK_ERROR)
        } catch (authError: FirebaseAuthException) {
            return LoginResult.Error(if (isTooManyRequests(authError)) AuthErrorReason.TOO_MANY_REQUESTS else AuthErrorReason.UNKNOWN)
        } catch (unexpected: Exception) {
            return LoginResult.Error(AuthErrorReason.UNKNOWN)
        }

        if (currentUserProfile() is ProfileResult.Missing) {
            return LoginResult.ProfileIncomplete
        }
        if (!user.isEmailVerified) {
            return LoginResult.RequiresVerification
        }
        return LoginResult.Success
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (noSuchUser: FirebaseAuthInvalidUserException) {

            AuthResult.Success
        } catch (network: FirebaseNetworkException) {
            AuthResult.Error(AuthErrorReason.NETWORK_ERROR)
        } catch (authError: FirebaseAuthException) {
            if (isTooManyRequests(authError)) AuthResult.Error(AuthErrorReason.TOO_MANY_REQUESTS) else AuthResult.Error(AuthErrorReason.UNKNOWN)
        } catch (unexpected: Exception) {
            AuthResult.Error(AuthErrorReason.UNKNOWN)
        }
    }

    override suspend fun signInWithGoogle(): AuthResult = AuthResult.Error(AuthErrorReason.SERVICE_NOT_CONFIGURED)

    override fun isAuthenticated(): Boolean {
        val user = auth.currentUser ?: return false

        return user.isEmailVerified
    }

    override fun currentSessionEmail(): String? = auth.currentUser?.email

    override fun logout() {
        auth.signOut()
    }

    override fun cancelPendingSession() {
        auth.signOut()
    }

    override suspend fun currentUserProfile(): ProfileResult {
        val user = auth.currentUser ?: return ProfileResult.NotSignedIn
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION).document(user.uid).get().await()
            if (!snapshot.exists()) return ProfileResult.Missing

            val fullName = snapshot.getString(FIELD_FULL_NAME)?.takeIf { it.isNotBlank() } ?: user.displayName ?: ""

            val gender = snapshot.getString(FIELD_GENDER)?.let { raw -> runCatching { Gender.valueOf(raw) }.getOrNull() }
            ProfileResult.Loaded(UserProfile(uid = user.uid, fullName = fullName, email = user.email ?: "", gender = gender))
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: FirebaseNetworkException) {
            ProfileResult.ReadError(AuthErrorReason.NETWORK_ERROR)
        } catch (readError: Exception) {

            ProfileResult.ReadError(AuthErrorReason.UNKNOWN)
        }
    }

    override suspend fun resendVerificationEmail(): AuthResult {
        val user = auth.currentUser ?: return AuthResult.Error(AuthErrorReason.UNKNOWN)
        return try {
            user.sendEmailVerification().await()
            AuthResult.Success
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: FirebaseNetworkException) {
            AuthResult.Error(AuthErrorReason.NETWORK_ERROR)
        } catch (authError: FirebaseAuthException) {
            if (isTooManyRequests(authError)) AuthResult.Error(AuthErrorReason.TOO_MANY_REQUESTS) else AuthResult.Error(AuthErrorReason.UNKNOWN)
        } catch (unexpected: Exception) {
            AuthResult.Error(AuthErrorReason.UNKNOWN)
        }
    }

    override suspend fun refreshVerificationStatus(): VerificationCheckResult {
        val user = auth.currentUser ?: return VerificationCheckResult.CHECK_FAILED
        return try {

            user.reload().await()
            if (user.isEmailVerified) VerificationCheckResult.VERIFIED else VerificationCheckResult.NOT_YET_VERIFIED
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (unexpected: Exception) {
            VerificationCheckResult.CHECK_FAILED
        }
    }

    override suspend fun completeProfile(fullName: String, gender: Gender): AuthResult {
        val user = auth.currentUser ?: return AuthResult.Error(AuthErrorReason.UNKNOWN)
        return try {
            saveProfile(uid = user.uid, fullName = fullName, email = user.email ?: "", gender = gender)
            try {
                user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(fullName).build()).await()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (ignored: Exception) {

            }
            AuthResult.Success
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: FirebaseNetworkException) {
            AuthResult.Error(AuthErrorReason.NETWORK_ERROR)
        } catch (unexpected: Exception) {
            AuthResult.Error(AuthErrorReason.PROFILE_SAVE_FAILED)
        }
    }

    private fun isTooManyRequests(error: FirebaseAuthException): Boolean =
        error.errorCode == "ERROR_TOO_MANY_REQUESTS"

    private suspend fun saveProfile(uid: String, fullName: String, email: String, gender: Gender) {
        val profile = mapOf(
            FIELD_FULL_NAME to fullName,
            FIELD_EMAIL to email,
            FIELD_GENDER to gender.name,
            FIELD_CREATED_AT to FieldValue.serverTimestamp()
        )
        firestore.collection(USERS_COLLECTION).document(uid).set(profile).await()
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val FIELD_FULL_NAME = "fullName"
        const val FIELD_EMAIL = "email"
        const val FIELD_GENDER = "gender"
        const val FIELD_CREATED_AT = "createdAt"
    }
}
