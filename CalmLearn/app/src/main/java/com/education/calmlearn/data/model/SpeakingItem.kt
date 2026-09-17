package com.education.calmlearn.data.model

enum class SpeakingLevel { WORD, PHRASE, SENTENCE }

data class SpeakingItem(
    val id: String,
    val level: SpeakingLevel,
    val text: String,
    val phonetic: String,
    val meaningVi: String,
    val tip: String
)
