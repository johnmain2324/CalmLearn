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
    /** Da goi qua nhieu lan trong thoi gian ngan (FirebaseTooManyRequestsException) - Firebase tu
     *  gioi han toc do gui email xac minh / dat lai mat khau. */
    TOO_MANY_REQUESTS,
    /** Tai khoan xac thuc da co (hoac da tao) nhung buoc luu ho so (ten/gioi tinh) that bai. */
    PROFILE_SAVE_FAILED,
    UNKNOWN
}

/** Ket qua kiem tra lai trang thai xac minh email (nut "Toi da xac minh - Kiem tra lai"). */
enum class VerificationCheckResult {
    VERIFIED,
    NOT_YET_VERIFIED,
    CHECK_FAILED
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val reason: AuthErrorReason) : AuthResult()
}

/**
 * Ket qua dang ky. register() CHI chiu trach nhiem tao tai khoan Auth + luu ho so Firestore - viec
 * gui email xac minh duoc tach rieng sang man hinh Xac minh email (VerifyEmailViewModel, goi
 * resendVerificationEmail()) de co the bao chinh xac "da gui" hay "gui that bai" tai THOI DIEM gui
 * that su, thay vi mot ket qua co dinh luc dang ky co the khong con dung khi nguoi dung xem duoc.
 */
sealed class RegisterResult {
    /** Tai khoan Auth da tao VA ho so Firestore da luu xong. Buoc tiep theo (gui email xac minh)
     *  do man hinh Xac minh email dam nhiem. */
    object AccountReady : RegisterResult()

    /** Tai khoan Auth da duoc tao nhung ho so Firestore CHUA luu duoc (vd mat mang ngay sau khi
     *  tao tai khoan, hoac nguoi dung dong app giua chung). Tai khoan van con, mat khau van dung -
     *  se duoc dua vao man hinh hoan tat ho so (khong phai dang ky lai). */
    object ProfileIncomplete : RegisterResult()

    data class Error(val reason: AuthErrorReason) : RegisterResult()
}

/**
 * Ket qua dang nhap. Tach rieng khoi [AuthResult] vi dang nhap co the phat hien 2 tinh huong "gan
 * thanh cong" khac Success/Error thuan tuy: dung mat khau nhung email chua xac minh, hoac dung mat
 * khau nhung ho so chua duoc luu (dang ky bi gian doan truoc do). Ca hai truong hop nay KHONG duoc
 * signOut ngay - giu phien de man hinh xac minh/hoan tat ho so co the tiep tuc xu ly.
 */
sealed class LoginResult {
    object Success : LoginResult()
    object RequiresVerification : LoginResult()
    object ProfileIncomplete : LoginResult()
    data class Error(val reason: AuthErrorReason) : LoginResult()
}

/** Ket qua doc ho so nguoi dung - phan biet "chua dang nhap", "chua co ho so" va "doc loi tam thoi",
 *  KHONG duoc gop chung thanh mot gia tri null duy nhat (xem AuthRepository.currentUserProfile()). */
sealed class ProfileResult {
    data class Loaded(val profile: UserProfile) : ProfileResult()

    /** Chua dang nhap (khong co FirebaseUser). */
    object NotSignedIn : ProfileResult()

    /** Da dang nhap nhung Firestore CHUA co document ho so (khac voi loi doc). */
    object Missing : ProfileResult()

    /** Da dang nhap, co the co hoac khong co ho so, nhung LAN DOC NAY that bai (mang/quyen truy
     *  cap) - khong duoc suy dien thanh "khong co ho so" hay fabricate du lieu thay the. */
    data class ReadError(val reason: AuthErrorReason) : ProfileResult()
}
