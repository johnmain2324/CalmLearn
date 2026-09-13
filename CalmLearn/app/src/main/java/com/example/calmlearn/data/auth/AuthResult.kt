package com.example.calmlearn.data.auth

/** Ly do that bai o muc dich hien thi (UI anh xa sang string resource, khong hardcode chuoi o day). */
enum class AuthErrorReason {
    /** Chua co dich vu xac thuc that duoc noi (xem [UnavailableAuthRepository]). */
    SERVICE_NOT_CONFIGURED,
    NETWORK_ERROR,
    INVALID_CREDENTIALS,
    /** Dang ky voi email da co tai khoan (FirebaseAuthUserCollisionException). */
    EMAIL_ALREADY_IN_USE,
    /** Mat khau khong dap ung chinh sach cua dich vu (FirebaseAuthWeakPasswordException). */
    WEAK_PASSWORD,
    /** Dang nhap dung nhung email chua duoc xac minh - da tu dong dang xuat, chua coi la co phien. */
    EMAIL_NOT_VERIFIED,
    /** Tai khoan xac thuc da tao thanh cong nhung buoc luu ho so (ten/gioi tinh) bi loi. */
    PROFILE_SAVE_FAILED,
    UNKNOWN
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val reason: AuthErrorReason) : AuthResult()
}

/**
 * Ket qua rieng cho dang ky (khac AuthResult vi dang ky co nhieu kha nang hon dang nhap/reset):
 * dich vu that co the tra ve phien dang nhap ngay, hoac yeu cau buoc tiep theo (vd xac minh email)
 * truoc khi coi la da dang nhap. UI dua vao day de quyet dinh co vao thang Trang chu hay khong
 * (xem RegisterViewModel/RegisterFragment).
 */
sealed class RegisterResult {
    /** Tai khoan da duoc tao VA dich vu da cap phien dang nhap hop le ngay. */
    object SignedIn : RegisterResult()
    /** Tai khoan da duoc tao nhung CHUA co phien (can xac minh email / dang nhap lai...). */
    object RequiresVerification : RegisterResult()
    data class Error(val reason: AuthErrorReason) : RegisterResult()
}
