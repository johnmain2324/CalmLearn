package com.education.calmlearn.data.auth

/**
 * Tach biet man hinh (Fragment/ViewModel) khoi dich vu xac thuc that.
 *
 * Implementation dang duoc [AuthRepositoryProvider] su dung la [FirebaseAuthRepository] (Firebase
 * Authentication + Cloud Firestore cho ho so). [UnavailableAuthRepository] van duoc giu lai trong
 * code lam stub/fallback (vd de chay UI khi chua co `google-services.json` that) nhung khong con
 * duoc AuthRepositoryProvider tro toi.
 */
interface AuthRepository {

    /** Xem [RegisterResult]: tai khoan, ho so va email xac minh la 3 buoc co the thanh cong/that bai doc lap. */
    suspend fun register(fullName: String, email: String, password: String, gender: Gender): RegisterResult

    /**
     * Xem [LoginResult]. Firebase Auth SDK cho Android LUON giu phien dang nhap giua cac lan mo
     * app (khong co lua chon "chi trong phien nay" nhu ban Web) nen khong co tham so rememberMe -
     * phien duoc giu cho den khi goi [logout] tuong minh.
     */
    suspend fun login(email: String, password: String): LoginResult

    suspend fun sendPasswordResetEmail(email: String): AuthResult

    suspend fun signInWithGoogle(): AuthResult

    /** True chi khi dang co phien dang nhap that VA da dap ung chinh sach xac minh cua app. */
    fun isAuthenticated(): Boolean

    /** Email cua phien dang "cho hoan tat" hien tai (co the chua xac minh/chua co ho so) - null
     *  neu khong co phien nao. Doc dong bo tu FirebaseUser da cache, khong goi mang. */
    fun currentSessionEmail(): String?

    fun logout()

    /** Xem [ProfileResult]: null gop chung "chua co ho so" voi "doc loi" la nguon goc loi - tach rieng. */
    suspend fun currentUserProfile(): ProfileResult

    /**
     * Gui lai email xac minh cho FirebaseUser DANG DUOC XAC THUC (khong nhan tham so email - phai
     * dang co phien, tranh gia dinh biet email la du de goi API nay). Loi neu chua dang nhap.
     */
    suspend fun resendVerificationEmail(): AuthResult

    /**
     * Tai lai du lieu nguoi dung tu Firebase (reload, khong chi doc gia tri cache cu) roi tra ve
     * isEmailVerified moi nhat. Dung cho nut "Toi da xac minh - Kiem tra lai".
     */
    suspend fun refreshVerificationStatus(): VerificationCheckResult

    /**
     * Thu luu/luu lai ho so (ten, gioi tinh) cho FirebaseUser dang duoc xac thuc - dung khi dang
     * ky bi gian doan truoc buoc luu ho so ([RegisterResult.ProfileIncomplete] /
     * [LoginResult.ProfileIncomplete]). An toan khi goi nhieu lan (ghi de cung 1 document theo
     * uid, khong tao ban ghi trung).
     */
    suspend fun completeProfile(fullName: String, gender: Gender): AuthResult

    /**
     * Huy phien "cho xac minh/hoan tat ho so" tam thoi khi nguoi dung roi man hinh xac minh ma
     * chua hoan tat (vd bam Back) - tranh de lai mot phien Firebase con "treo" khong ro trang thai.
     */
    fun cancelPendingSession()
}
