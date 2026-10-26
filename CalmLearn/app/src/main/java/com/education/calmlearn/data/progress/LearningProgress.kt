package com.education.calmlearn.data.progress

data class LearningProgress(
    val learnedWordIds: Set<String> = emptySet(),
    val favoriteWordIds: Set<String> = emptySet(),
    val xpAwardedWordIds: Set<String> = emptySet(),

    val quizXpAwardedKeys: Set<String> = emptySet(),
    val xp: Int = 0,
    val streak: Int = 0,
    val lastActiveDate: String? = null,

    val lastQuizAccuracyPercent: Int? = null,

    val highAccuracyQuizCount: Int = 0,

    val speakingSessionAwardedKeys: Set<String> = emptySet(),

    val speakingSessionCount: Int = 0,

    val listeningSessionAwardedKeys: Set<String> = emptySet(),

    val listeningSessionCount: Int = 0,

    val todayStudySeconds: Int = 0,
    val todayStudyDate: String? = null,

    val totalStudySeconds: Int = 0,

    val completedGrammarLessonIds: Set<String> = emptySet()
)
