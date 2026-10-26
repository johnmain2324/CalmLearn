package com.education.calmlearn.data.auth

object AuthRepositoryProvider {
    val repository: AuthRepository by lazy { FirebaseAuthRepository() }
}
