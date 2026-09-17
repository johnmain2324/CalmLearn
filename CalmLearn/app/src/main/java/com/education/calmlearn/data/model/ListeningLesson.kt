package com.education.calmlearn.data.model

data class ListeningLesson(
    val id: String,
    val title: String,
    val levelTag: String,
    val durationSeconds: Int,
    val transcript: List<TranscriptLine>,
    val questions: List<QuizQuestion>
)

data class TranscriptLine(
    val speaker: String,
    val line: String
)
