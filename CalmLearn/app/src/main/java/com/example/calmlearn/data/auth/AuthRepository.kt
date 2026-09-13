package com.example.calmlearn.data.auth

/**
 * Tach biet man hinh (Fragment/ViewModel) khoi dich vu xac thuc that.
 *
 * Implementation dang duoc [AuthRepositoryProvider] su dung la [FirebaseAuthRepository] (Firebase
 * Authentication + Cloud Firestore cho ho so). [UnavailableAuthRepository] van duoc giu lai trong
 * code lam stub/fallback (vd de chay UI khi chua co `google-services.json` that) nhung khong con
 * duoc AuthRepositoryProvider tro toi.
 */
interface AuthRepository {

    suspend fun register(fullName: String, email: String, password: String, gender: Gender): RegisterResult

    /**
     * [rememberMe] chi co y nghia khi duoc trien khai bang co che phien that cua dich vu. Voi
     * [FirebaseAuthRepository] hien tai, Firebase Auth SDK cho Android LUON giu phien giua cac
     * lan mo app (khong co API "session-only" nhu ban Web) nen tham so nay chua tao ra khac biet
     * hanh vi thuc te - xem chu thich trong FirebaseAuthRepository.login(). Khong tu luu mat khau.
     */
    suspend fun login(email: String, password: String, rememberMe: Boolean): AuthResult

    suspend fun sendPasswordResetEmail(email: String): AuthResult

    suspend fun signInWithGoogle(): AuthResult

    /** True chi khi dang co phien dang nhap that (khong phai co lai tu SharedPreferences). */
    fun isAuthenticated(): Boolean

    fun logout()

    /** Ho so (ten, email, gioi tinh) cua nguoi dang dang nhap. Null neu chua dang nhap hoac chua doc duoc. */
    suspend fun currentUserProfile(): UserProfile?
}
