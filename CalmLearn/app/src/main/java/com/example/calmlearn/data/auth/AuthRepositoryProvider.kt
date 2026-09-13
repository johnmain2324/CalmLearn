package com.example.calmlearn.data.auth

/**
 * Noi cung cap AuthRepository don gian (project chua dung Hilt/Koin). Cac ViewModel doc
 * [repository] tu day thay vi tu tao instance, de sau nay chi can doi mot cho khi co
 * implementation that.
 */
object AuthRepositoryProvider {
    val repository: AuthRepository by lazy { UnavailableAuthRepository() }
}
