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

/**
 * Implementation THAT cua [AuthRepository], dung Firebase Authentication (email/password) de xac
 * thuc va Cloud Firestore (collection [USERS_COLLECTION]) de luu ho so (ten, gioi tinh) gan voi
 * uid cua nguoi dung - Firebase Auth khong co san truong "gioi tinh" nen phai luu rieng.
 *
 * Trong luc dang ky/dang nhap, mot phien "cho hoan tat" (da xac thuc dung mat khau nhung chua du
 * dieu kien vao app - xem [RegisterResult]/[LoginResult]) duoc GIU LAI (khong signOut) de man hinh
 * xac minh email / hoan tat ho so co the tiep tuc dung chinh FirebaseUser do, khong phai dang nhap
 * lai tu dau va khong can gia dinh biet email la du de goi cac API can xac thuc.
 *
 * DIEU KIEN DE CHAY THAT: can file `app/google-services.json` that (tai tu Firebase Console) va
 * bat Email/Password provider trong Firebase Console > Authentication > Sign-in method. Xem
 * README.md muc "Cau hinh Firebase" de biet chi tiet tung buoc.
 */
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

        // Tu day tro di TAI KHOAN DA TON TAI THAT. KHONG xoa lai du cac buoc sau that bai - khong
        // the dam bao thao tac xoa thanh cong (chinh no cung co the loi/mat mang), va xoa nham se
        // khoa nguoi dung khoi chinh tai khoan cua ho trong khi mat khau da co hieu luc. Thay vao
        // do, nguoi dung se dang nhap lai bang dung mat khau nay de hoan tat cac buoc con thieu
        // (xem login() -> RegisterResult.ProfileIncomplete / LoginResult.ProfileIncomplete).
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

        // Cap nhat displayName tren chinh Auth profile de co san ngay ca khi chua doc duoc Firestore
        // (fallback trong currentUserProfile()). Khong quan trong bang buoc luu Firestore o tren nen
        // loi o day khong lam that bai toan bo dang ky - nhung KHONG dung runCatching de khong
        // "nuot" nham CancellationException cua coroutine (runCatching bat ca Throwable).
        try {
            firebaseUser.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(fullName).build()).await()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (ignored: Exception) {
            // Khong quan trong bang ho so Firestore da luu o tren - bo qua cac loi khac.
        }

        // KHONG gui email xac minh o day: man hinh Xac minh email (goi resendVerificationEmail())
        // se dam nhiem viec nay va bao chinh xac ket qua tai thoi diem gui that su, tranh mot ket
        // qua "da gui/that bai" co dinh tu luc dang ky co the khong con dung khi nguoi dung xem duoc.
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

        // Dang nhap dung mat khau: KHONG signOut o day du chua xac minh/chua co ho so - giu chinh
        // phien nay de man hinh xac minh/hoan tat ho so tiep tuc dung (xem cancelPendingSession()).
        // Chi mot lan doc ho so that bai tam thoi (ReadError) KHONG duoc coi la "thieu ho so" -
        // roi qua kiem tra xac minh binh thuong thay vi chan nham nguoi dung vi mot loi doc thoang qua.
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
            // KHONG tiet lo email co ton tai trong he thong hay khong: tra ve nhu thanh cong (im
            // lang) giong het truong hop email that su ton tai va da duoc gui.
            AuthResult.Success
        } catch (network: FirebaseNetworkException) {
            AuthResult.Error(AuthErrorReason.NETWORK_ERROR)
        } catch (authError: FirebaseAuthException) {
            if (isTooManyRequests(authError)) AuthResult.Error(AuthErrorReason.TOO_MANY_REQUESTS) else AuthResult.Error(AuthErrorReason.UNKNOWN)
        } catch (unexpected: Exception) {
            AuthResult.Error(AuthErrorReason.UNKNOWN)
        }
    }

    /** Chua nam trong pham vi cau hinh hien tai - xem AuthRepository.signInWithGoogle(). */
    override suspend fun signInWithGoogle(): AuthResult = AuthResult.Error(AuthErrorReason.SERVICE_NOT_CONFIGURED)

    override fun isAuthenticated(): Boolean {
        val user = auth.currentUser ?: return false
        // isEmailVerified doc tu du lieu FirebaseUser da cache cuc bo (dong bo, khong goi mang) -
        // du de quyet dinh man hinh mo dau ma khong can trang thai cho/splash rieng. Neu nguoi dung
        // vua xac minh email o thiet bi/phien khac, co the can dang nhap lai de gia tri nay duoc
        // cap nhat (Firebase khong tu day du lieu ve). Khong kiem tra ho so Firestore o day (se can
        // goi mang bat dong bo) - ho so thieu duoc HomeFragment/ProfileFragment tu xu ly rieng.
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
            // Gia tri khong hop le/thieu -> null (chua biet), KHONG tu gan Gender.OTHER thay the.
            val gender = snapshot.getString(FIELD_GENDER)?.let { raw -> runCatching { Gender.valueOf(raw) }.getOrNull() }
            ProfileResult.Loaded(UserProfile(uid = user.uid, fullName = fullName, email = user.email ?: "", gender = gender))
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: FirebaseNetworkException) {
            ProfileResult.ReadError(AuthErrorReason.NETWORK_ERROR)
        } catch (readError: Exception) {
            // Bao gom ca loi quyen truy cap (Firestore permission-denied) - khong fabricate mot ho
            // so "day du" gia de thay the, chi bao ro day la LOI DOC, khac voi "chua co ho so".
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
            // reload() goi mang de tai lai du lieu moi nhat - KHONG chi doc gia tri isEmailVerified
            // da cache cuc bo (do se khong bao gio thay doi neu chi doc lai cache).
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
                // Khong quan trong bang ho so Firestore da luu o tren - bo qua cac loi khac.
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

    /** Firebase Android SDK khong co class rieng cho loi "qua nhieu yeu cau" - phai kiem tra qua
     *  errorCode cua FirebaseAuthException (vd khi gui email xac minh/dat lai mat khau lien tuc). */
    private fun isTooManyRequests(error: FirebaseAuthException): Boolean =
        error.errorCode == "ERROR_TOO_MANY_REQUESTS"

    /**
     * Ghi de toan bo document ho so theo [uid] (khong theo email - email chi la mot truong du
     * lieu, khong phai khoa chinh). `.set()` luon ghi vao CUNG MOT document (id = uid) nen an toan
     * khi goi lai nhieu lan (retry) - khong bao gio tao ban ghi trung.
     */
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
