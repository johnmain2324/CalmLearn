package com.education.calmlearn.data.progress

object ProgressRepositoryProvider {
    val repository: ProgressRepository by lazy { FirebaseProgressRepository() }
}
