package com.education.calmlearn.data.model

data class GrammarLesson(
    val id: String,
    val levelTag: String,
    val title: String,
    val formula: String,
    val explanation: String,
    val examples: List<GrammarExample>,
    val note: String,
    val quiz: List<QuizQuestion>
)

data class GrammarExample(
    val english: String,
    val vietnamese: String
)
