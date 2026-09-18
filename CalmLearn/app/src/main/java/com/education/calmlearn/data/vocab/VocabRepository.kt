package com.education.calmlearn.data.vocab

import com.education.calmlearn.data.model.VocabWord

/**
 * Tach Fragment khoi SQLiteOpenHelper/Cursor that (cung pattern voi
 * AuthRepository/ProgressRepository - xem data/auth/, data/progress/). Fragment
 * chi goi qua interface nay, khong bao gio dung truc tiep VocabDbHelper.
 */
interface VocabRepository {

    /** Toan bo tu (ETL + nguoi dung tu them) - phuc vu man "Danh sach tu". */
    suspend fun getAllWords(): List<VocabWord>

    /** Tu theo 1 chu de - phuc vu man "Hoc tu"/Flashcard. */
    suspend fun getWordsByTopic(topicId: String): List<VocabWord>

    /** Mot tu theo id, null neu khong ton tai. */
    suspend fun getWordById(id: String): VocabWord?

    /** Tim theo tu hoac nghia tieng Viet (khong phan biet hoa/thuong). */
    suspend fun searchWords(query: String): List<VocabWord>

    /**
     * Them 1 tu MOI do nguoi dung tu nhap (man "Them tu") - INSERT that vao
     * bang words (is_manual=1) roi user_words. Tra ve id da sinh khi thanh cong.
     */
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

    /** Xoa 1 tu do nguoi dung tu them (chi cho phep voi is_manual=1). */
    suspend fun deleteUserWord(wordId: String): VocabResult

    /** Sua ghi chu ca nhan (bang user_words) cho 1 tu do nguoi dung tu them. */
    suspend fun updateUserWordNote(wordId: String, note: String): VocabResult
}

sealed class VocabResult {
    data class Inserted(val wordId: String) : VocabResult()
    object Updated : VocabResult()
    object Deleted : VocabResult()
    data class Error(val message: String) : VocabResult()
}
