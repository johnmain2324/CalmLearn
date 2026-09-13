package com.example.calmlearn.data.auth

/**
 * Tach biet man hinh (Fragment/ViewModel) khoi dich vu xac thuc that.
 *
 * DIEM CHO TICH HOP: project hien chua chon giai phap xac thuc that (Firebase Auth, REST API
 * backend rieng, ...). [UnavailableAuthRepository] la implementation duy nhat hien co va luon
 * tra ve loi [AuthErrorReason.SERVICE_NOT_CONFIGURED]. Khi nhom chot duoc phuong an, hay them
 * mot implementation moi (vi du FirebaseAuthRepository / RestAuthRepository) va doi lai
 * [AuthRepositoryProvider] tro toi implementation do - khong can sua UI.
 */
interface AuthRepository {

    suspend fun register(fullName: String, email: String, password: String, gender: Gender): RegisterResult

    /**
     * [rememberMe] chi co y nghia khi duoc trien khai bang co che phien that cua dich vu (vd
     * Firebase Auth persistence / refresh token that). Khong tu luu mat khau. Voi
     * [UnavailableAuthRepository] hien tai, tham so nay bi bo qua hoan toan - form van hien
     * checkbox de nguoi dung lam quen giao dien nhung chua co hieu luc thuc te.
     */
    suspend fun login(email: String, password: String, rememberMe: Boolean): AuthResult

    suspend fun sendPasswordResetEmail(email: String): AuthResult

    suspend fun signInWithGoogle(): AuthResult

    /** True chi khi dang co phien dang nhap that (khong phai co lai tu SharedPreferences). */
    fun isAuthenticated(): Boolean

    fun logout()
}
