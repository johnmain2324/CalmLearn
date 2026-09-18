package com.education.calmlearn.data.vocab

/**
 * Logic thuan Kotlin (khong dung android.database) tach rieng khoi
 * AddWordFragment/SqliteVocabRepository de co the unit test tren JVM thuong,
 * cung quy uoc voi StreakCalculatorTest.kt (test ham thuan, khong test lop
 * framework nhu SQLiteOpenHelper).
 */
object VocabInput {

    /** "trip, voyage" -> ["trip", "voyage"] - bo khoang trang thua va cac phan tu rong
     *  (vd dau phay thua ", ,"). Dung boi AddWordFragment truoc khi goi
     *  VocabRepository.insertUserWord(). */
    fun parseSynonyms(raw: String): List<String> =
        raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
