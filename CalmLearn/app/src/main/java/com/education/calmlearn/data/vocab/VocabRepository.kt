package com.education.calmlearn.data.vocab

import com.education.calmlearn.data.model.VocabWord

interface VocabRepository {

    suspend fun getAllWords(): List<VocabWord>

    suspend fun getWordsByTopic(topicId: String): List<VocabWord>

    suspend fun getWordById(id: String): VocabWord?

    suspend fun searchWords(query: String): List<VocabWord>

    suspend fun insertUserWord(
        word: String,
        topicId: String,
        pos: String,
        meaningVi: String,
        definitionEn: String,
        exampleEn: String,
        exampleVi: String,
        synonyms: List<String>,
        note: String?
    ): VocabResult

    suspend fun deleteUserWord(wordId: String): VocabResult

    suspend fun updateUserWordNote(wordId: String, note: String): VocabResult
}

sealed class VocabResult {
    data class Inserted(val wordId: String) : VocabResult()
    object Updated : VocabResult()
    object Deleted : VocabResult()
    data class Error(val message: String) : VocabResult()
}
