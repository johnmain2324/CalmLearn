package com.example.calmlearn.data.auth

/**
 * Noi cung cap AuthRepository don gian (project chua dung Hilt/Koin). Cac ViewModel doc
 * [repository] tu day thay vi tu tao instance.
 *
 * Dang dung [FirebaseAuthRepository] - can `app/google-services.json` that (tai tu Firebase
 * Console) va bat provider Email/Password trong Firebase Console > Authentication > Sign-in
 * method thi cac loi goi that (dang ky/dang nhap/quen mat khau) moi hoat dong. Xem README.md.
 */
object AuthRepositoryProvider {
    val repository: AuthRepository by lazy { FirebaseAuthRepository() }
}
