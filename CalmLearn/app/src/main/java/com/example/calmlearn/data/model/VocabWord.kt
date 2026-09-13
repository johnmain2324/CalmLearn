package com.example.calmlearn.data.model

data class VocabWord(
    val id: String,
    val topicId: String,
    val word: String,
    val phonetic: String,
    val wordType: String,
    val meaningVi: String,
    val definitionEn: String,
    val exampleEn: String,
    val exampleVi: String,
    val synonyms: List<String>,
    var isFavorite: Boolean = false,
    var isLearned: Boolean = false
)
