package com.education.calmlearn.data.vocab

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.education.calmlearn.data.model.VocabWord
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MANUAL_ENTRY_SOURCE = "Nguoi dung tu nhap (man Them tu)"

/**
 * Trien khai [VocabRepository] bang API SQLite chuan (SQLiteOpenHelper +
 * ContentValues + Cursor/query/rawQuery) - khong dung Room. Moi thao tac chay
 * tren Dispatchers.IO vi day la I/O dong bo (giong cach cac ham dung Firebase
 * Task.await() trong AuthRepository/ProgressRepository deu chay ngoai main thread,
 * chi khac o day la IO dia phuong thay vi mang).
 */
class SqliteVocabRepository(context: Context) : VocabRepository {

    private val dbHelper = VocabDbHelper(context.applicationContext)

    private fun openReadable(): SQLiteDatabase {
        dbHelper.ensureDatabaseCopiedFromAssets()
        return dbHelper.readableDatabase
    }

    private fun openWritable(): SQLiteDatabase {
        dbHelper.ensureDatabaseCopiedFromAssets()
        return dbHelper.writableDatabase
    }

    override suspend fun getAllWords(): List<VocabWord> = withContext(Dispatchers.IO) {
        val db = openReadable()
        readWords(db, selection = null, selectionArgs = null)
    }

    override suspend fun getWordsByTopic(topicId: String): List<VocabWord> = withContext(Dispatchers.IO) {
        val db = openReadable()
        readWords(db, selection = "${VocabContract.Words.TOPIC_ID} = ?", selectionArgs = arrayOf(topicId))
    }

    override suspend fun getWordById(id: String): VocabWord? = withContext(Dispatchers.IO) {
        val db = openReadable()
        readWords(db, selection = "${VocabContract.Words.ID} = ?", selectionArgs = arrayOf(id)).firstOrNull()
    }

    override suspend fun searchWords(query: String): List<VocabWord> = withContext(Dispatchers.IO) {
        val db = openReadable()
        val like = "%${query.trim()}%"
        // rawQuery + LIKE: tim theo tu HOAC nghia tieng Viet, khong phan biet hoa/thuong.
        val cursor = db.rawQuery(
            """
            SELECT ${VocabContract.Words.ID} FROM ${VocabContract.Words.TABLE}
            WHERE ${VocabContract.Words.WORD} LIKE ? COLLATE NOCASE
               OR ${VocabContract.Words.MEANING_VI} LIKE ?
            ORDER BY ${VocabContract.Words.WORD} ASC
            """.trimIndent(),
            arrayOf(like, like)
        )
        val ids = cursor.use { c -> generateSequence { if (c.moveToNext()) c.getString(0) else null }.toList() }
        ids.mapNotNull { id -> readWords(db, "${VocabContract.Words.ID} = ?", arrayOf(id)).firstOrNull() }
    }

    override suspend fun insertUserWord(
        word: String,
        topicId: String,
        pos: String,
        meaningVi: String,
        definitionEn: String,
        exampleEn: String,
        exampleVi: String,
        synonyms: List<String>,
        note: String?
    ): VocabResult = withContext(Dispatchers.IO) {
        val db = openWritable()
        val wordId = "manual_${UUID.randomUUID()}"
        val now = System.currentTimeMillis()

        db.beginTransaction()
        try {
            val wordValues = ContentValues().apply {
                put(VocabContract.Words.ID, wordId)
                put(VocabContract.Words.TOPIC_ID, topicId)
                put(VocabContract.Words.WORD, word)
                put(VocabContract.Words.LEMMA, word)
                put(VocabContract.Words.POS, pos)
                putNull(VocabContract.Words.CEFR_LEVEL)
                putNull(VocabContract.Words.FREQUENCY)
                put(VocabContract.Words.MEANING_VI, meaningVi)
                putNull(VocabContract.Words.PHONETIC)
                put(VocabContract.Words.VI_SOURCE, MANUAL_ENTRY_SOURCE)
                put(VocabContract.Words.SOURCE, MANUAL_ENTRY_SOURCE)
                put(VocabContract.Words.IS_MANUAL, 1)
                put(VocabContract.Words.CREATED_AT, now)
            }
            db.insert(VocabContract.Words.TABLE, null, wordValues)

            val definitionValues = ContentValues().apply {
                put(VocabContract.Definitions.WORD_ID, wordId)
                putNull(VocabContract.Definitions.SENSE_KEY)
                put(VocabContract.Definitions.DEFINITION, definitionEn)
                putNull(VocabContract.Definitions.CEFR_LEVEL)
                put(VocabContract.Definitions.SOURCE, MANUAL_ENTRY_SOURCE)
            }
            db.insert(VocabContract.Definitions.TABLE, null, definitionValues)

            synonyms.map { it.trim() }.filter { it.isNotEmpty() }.forEach { synonym ->
                val synonymValues = ContentValues().apply {
                    put(VocabContract.Synonyms.WORD_ID, wordId)
                    put(VocabContract.Synonyms.SYNONYM, synonym)
                    put(VocabContract.Synonyms.SOURCE, MANUAL_ENTRY_SOURCE)
                }
                db.insert(VocabContract.Synonyms.TABLE, null, synonymValues)
            }

            if (exampleEn.isNotBlank()) {
                val exampleValues = ContentValues().apply {
                    put(VocabContract.Examples.WORD_ID, wordId)
                    put(VocabContract.Examples.SOURCE, MANUAL_ENTRY_SOURCE)
                    put(VocabContract.Examples.SENTENCE, exampleEn)
                    if (exampleVi.isNotBlank()) put(VocabContract.Examples.SENTENCE_VI, exampleVi) else putNull(VocabContract.Examples.SENTENCE_VI)
                    put(VocabContract.Examples.VI_SOURCE, MANUAL_ENTRY_SOURCE)
                }
                db.insert(VocabContract.Examples.TABLE, null, exampleValues)
            }

            val userWordValues = ContentValues().apply {
                put(VocabContract.UserWords.WORD_ID, wordId)
                put(VocabContract.UserWords.NOTE, note)
                put(VocabContract.UserWords.ADDED_AT, now)
            }
            db.insert(VocabContract.UserWords.TABLE, null, userWordValues)

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            return@withContext VocabResult.Error(e.message ?: "Khong the them tu moi")
        } finally {
            db.endTransaction()
        }
        VocabResult.Inserted(wordId)
    }

    override suspend fun deleteUserWord(wordId: String): VocabResult = withContext(Dispatchers.IO) {
        val db = openWritable()
        val isManual = db.query(
            VocabContract.Words.TABLE,
            arrayOf(VocabContract.Words.IS_MANUAL),
            "${VocabContract.Words.ID} = ?",
            arrayOf(wordId),
            null, null, null
        ).use { c -> c.moveToFirst() && c.getInt(0) == 1 }

        if (!isManual) {
            return@withContext VocabResult.Error("Chi duoc xoa tu do ban tu them")
        }

        db.beginTransaction()
        try {
            db.delete(VocabContract.UserWords.TABLE, "${VocabContract.UserWords.WORD_ID} = ?", arrayOf(wordId))
            db.delete(VocabContract.Examples.TABLE, "${VocabContract.Examples.WORD_ID} = ?", arrayOf(wordId))
            db.delete(VocabContract.Synonyms.TABLE, "${VocabContract.Synonyms.WORD_ID} = ?", arrayOf(wordId))
            db.delete(VocabContract.Definitions.TABLE, "${VocabContract.Definitions.WORD_ID} = ?", arrayOf(wordId))
            db.delete(VocabContract.Words.TABLE, "${VocabContract.Words.ID} = ? AND ${VocabContract.Words.IS_MANUAL} = 1", arrayOf(wordId))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        VocabResult.Deleted
    }

    override suspend fun updateUserWordNote(wordId: String, note: String): VocabResult = withContext(Dispatchers.IO) {
        val db = openWritable()
        val values = ContentValues().apply { put(VocabContract.UserWords.NOTE, note) }
        val rows = db.update(VocabContract.UserWords.TABLE, values, "${VocabContract.UserWords.WORD_ID} = ?", arrayOf(wordId))
        if (rows == 0) {
            val insertValues = ContentValues().apply {
                put(VocabContract.UserWords.WORD_ID, wordId)
                put(VocabContract.UserWords.NOTE, note)
                put(VocabContract.UserWords.ADDED_AT, System.currentTimeMillis())
            }
            db.insert(VocabContract.UserWords.TABLE, null, insertValues)
        }
        VocabResult.Updated
    }

    /** Doc danh sach words that hop dinh nghia dau tien + toan bo dong nghia + vi du uu tien. */
    private fun readWords(db: SQLiteDatabase, selection: String?, selectionArgs: Array<String>?): List<VocabWord> {
        val words = mutableListOf<VocabWord>()
        db.query(
            VocabContract.Words.TABLE,
            null, selection, selectionArgs, null, null,
            "${VocabContract.Words.TOPIC_ID} ASC, ${VocabContract.Words.WORD} ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                words += cursorToWord(db, cursor)
            }
        }
        return words
    }

    private fun cursorToWord(db: SQLiteDatabase, wordCursor: Cursor): VocabWord {
        val id = wordCursor.getString(wordCursor.getColumnIndexOrThrow(VocabContract.Words.ID))
        val topicId = wordCursor.getString(wordCursor.getColumnIndexOrThrow(VocabContract.Words.TOPIC_ID))
        val word = wordCursor.getString(wordCursor.getColumnIndexOrThrow(VocabContract.Words.WORD))
        val pos = wordCursor.getStringOrNull(VocabContract.Words.POS) ?: ""
        val cefrLevel = wordCursor.getStringOrNull(VocabContract.Words.CEFR_LEVEL)
        val meaningVi = wordCursor.getStringOrNull(VocabContract.Words.MEANING_VI) ?: ""
        val phonetic = wordCursor.getStringOrNull(VocabContract.Words.PHONETIC) ?: ""
        val isManual = wordCursor.getInt(wordCursor.getColumnIndexOrThrow(VocabContract.Words.IS_MANUAL)) == 1

        val definition = db.query(
            VocabContract.Definitions.TABLE,
            arrayOf(VocabContract.Definitions.DEFINITION),
            "${VocabContract.Definitions.WORD_ID} = ?", arrayOf(id),
            null, null, "${VocabContract.Definitions.ID} ASC", "1"
        ).use { c -> if (c.moveToFirst()) c.getString(0) else "" }

        val synonyms = mutableListOf<String>()
        db.query(
            VocabContract.Synonyms.TABLE,
            arrayOf(VocabContract.Synonyms.SYNONYM),
            "${VocabContract.Synonyms.WORD_ID} = ?", arrayOf(id),
            null, null, "${VocabContract.Synonyms.ID} ASC"
        ).use { c -> while (c.moveToNext()) synonyms += c.getString(0) }

        var exampleEn = ""
        var exampleVi = ""
        db.query(
            VocabContract.Examples.TABLE,
            arrayOf(VocabContract.Examples.SENTENCE, VocabContract.Examples.SENTENCE_VI),
            "${VocabContract.Examples.WORD_ID} = ?", arrayOf(id),
            null, null,
            // Uu tien vi du CO san ban dich tieng Viet (sentence_vi khong NULL) de hien thi du cap song ngu.
            "${VocabContract.Examples.SENTENCE_VI} IS NULL ASC, ${VocabContract.Examples.ID} ASC",
            "1"
        ).use { c ->
            if (c.moveToFirst()) {
                exampleEn = c.getString(0)
                exampleVi = c.getStringOrNull(1) ?: ""
            }
        }

        return VocabWord(
            id = id,
            topicId = topicId,
            word = word,
            phonetic = phonetic,
            wordType = pos,
            meaningVi = meaningVi,
            definitionEn = definition,
            exampleEn = exampleEn,
            exampleVi = exampleVi,
            synonyms = synonyms,
            cefrLevel = cefrLevel,
            isManual = isManual
        )
    }

    private fun Cursor.getStringOrNull(column: String): String? {
        val index = getColumnIndexOrThrow(column)
        return if (isNull(index)) null else getString(index)
    }

    private fun Cursor.getStringOrNull(columnIndex: Int): String? =
        if (isNull(columnIndex)) null else getString(columnIndex)
}
