package com.education.calmlearn.data.progress

/**
 * Noi cung cap ProgressRepository don gian (project chua dung Hilt/Koin) - cung pattern voi
 * AuthRepositoryProvider (xem data/auth/AuthRepositoryProvider.kt).
 */
object ProgressRepositoryProvider {
    val repository: ProgressRepository by lazy { FirebaseProgressRepository() }
}
