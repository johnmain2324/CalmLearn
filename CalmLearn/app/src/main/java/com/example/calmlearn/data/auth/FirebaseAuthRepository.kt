package com.example.calmlearn.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * Implementation THAT cua [AuthRepository], dung Firebase Authentication (email/password) de xac
 * thuc va Cloud Firestore (collection [USERS_COLLECTION]) de luu ho so (ten, gioi tinh) gan voi
 * uid cua nguoi dung - Firebase Auth khong co san truong "gioi tinh" nen phai luu rieng.
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

        // Luu ho so (ten + gioi tinh) gan voi uid. Neu buoc nay loi, KHONG duoc bao dang ky thanh
        // cong toan bo - xoa lai tai khoan Auth vua tao de nguoi dung dang ky lai sach se, tranh
        // "tai khoan mo coi" co Auth nhung khong co ho so.
        try {
            saveProfile(uid = firebaseUser.uid, fullName = fullName, email = email, gender = gender)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (profileError: Exception) {
            runCatching { firebaseUser.delete().await() }
            auth.signOut()
            return RegisterResult.Error(AuthErrorReason.PROFILE_SAVE_FAILED)
        }

        // Cap nhat displayName tren chinh Auth profile de co san ngay ca khi chua doc duoc Firestore
        // (fallback trong currentUserProfile()). Khong quan trong bang buoc luu Firestore o tren nen
        // loi o day khong lam that bai toan bo dang ky.
        runCatching {
            firebaseUser.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(fullName).build()).await()
        }

        return try {
            firebaseUser.sendEmailVerification().await()
            // Chua xac minh email -> khong coi la co phien hop le, dang xuat ngay va yeu cau
            // nguoi dung tu dang nhap lai sau khi da xac minh (xem LoginFragment/isAuthenticated()).
            auth.signOut()
            RegisterResult.RequiresVerification
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (sendEmailError: Exception) {
            // Tai khoan + ho so DA duoc tao that; chi la khong gui duoc email xac minh luc nay (vd
            // mat mang). Van coi la can xac minh/dang nhap lai, khong duoc coi la loi lam mat tai khoan.
            auth.signOut()
            RegisterResult.RequiresVerification
        }
    }

    override suspend fun login(email: String, password: String, rememberMe: Boolean): AuthResult {
        // Ghi chu ve rememberMe: Firebase Auth SDK cho Android LUON giu phien giua cac lan mo app
        // (khong co API "session-only" nhu ban Web) - tham so nay hien khong tao ra khac biet hanh
        // vi thuc te, xem chu thich o AuthRepository.login().
        val user = try {
            auth.signInWithEmailAndPassword(email, password).await().user
                ?: return AuthResult.Error(AuthErrorReason.UNKNOWN)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (invalidUser: FirebaseAuthInvalidUserException) {
            return AuthResult.Error(AuthErrorReason.INVALID_CREDENTIALS)
        } catch (invalidCredentials: FirebaseAuthInvalidCredentialsException) {
            return AuthResult.Error(AuthErrorReason.INVALID_CREDENTIALS)
        } catch (network: FirebaseNetworkException) {
            return AuthResult.Error(AuthErrorReason.NETWORK_ERROR)
        } catch (unexpected: Exception) {
            return AuthResult.Error(AuthErrorReason.UNKNOWN)
        }

        if (!user.isEmailVerified) {
            auth.signOut()
            return AuthResult.Error(AuthErrorReason.EMAIL_NOT_VERIFIED)
        }
        return AuthResult.Success
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
        // cap nhat (Firebase khong tu day du lieu ve).
        return user.isEmailVerified
    }

    override fun logout() {
        auth.signOut()
    }

    override suspend fun currentUserProfile(): UserProfile? {
        val user = auth.currentUser ?: return null
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION).document(user.uid).get().await()
            val fullName = snapshot.getString(FIELD_FULL_NAME)?.takeIf { it.isNotBlank() } ?: user.displayName ?: ""
            val gender = snapshot.getString(FIELD_GENDER)
                ?.let { raw -> runCatching { Gender.valueOf(raw) }.getOrNull() }
                ?: Gender.OTHER
            UserProfile(uid = user.uid, fullName = fullName, email = user.email ?: "", gender = gender)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (readError: Exception) {
            // Doc ho so that bai (vd mat mang tam thoi) - fallback ve du lieu co san tren Auth
            // profile thay vi tra ve null hoan toan, de Trang chu/Ho so van hien duoc ten thay vi
            // trong rong chi vi mot loi doc tam thoi.
            UserProfile(uid = user.uid, fullName = user.displayName ?: "", email = user.email ?: "", gender = Gender.OTHER)
        }
    }

    private suspend fun saveProfile(uid: String, fullName: String, email: String, gender: Gender) {
        val profile = mapOf(
            FIELD_FULL_NAME to fullName,
            FIELD_EMAIL to email,
            FIELD_GENDER to gender.name,
            FIELD_CREATED_AT to com.google.firebase.firestore.FieldValue.serverTimestamp()
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
