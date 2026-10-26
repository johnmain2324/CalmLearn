package com.education.calmlearn.data.progress

import com.education.calmlearn.data.auth.AuthErrorReason

interface ProgressRepository {

    suspend fun loadProgress(): ProgressResult

    suspend fun setWordLearned(wordId: String, learned: Boolean): ProgressResult

    suspend fun setFavorite(wordId: String, favorite: Boolean): ProgressResult

    suspend fun recordQuizResult(quizId: String, correctCount: Int, totalQuestions: Int): ProgressResult

    suspend fun recordSpeakingSessionCompleted(level: String): ProgressResult

    suspend fun recordListeningLessonCompleted(lessonId: String): ProgressResult

    suspend fun addStudySeconds(seconds: Int): ProgressResult

    suspend fun recordGrammarLessonCompleted(lessonId: String): ProgressResult
}

sealed class ProgressResult {
    data class Loaded(val progress: LearningProgress) : ProgressResult()
    object NotSignedIn : ProgressResult()
    data class ReadError(val reason: AuthErrorReason) : ProgressResult()
}
