package com.education.calmlearn.data.vocab

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream

/**
 * CSDL tu vung that (SQLite chuan, khong dung Room) - xem so do bang + giai
 * thich day du tai SQLITE_VOCAB.md o goc project.
 *
 * App dung pattern "prepackaged SQLite database": [ensureDatabaseCopiedFromAssets]
 * copy nguyen file `assets/vocab.db` (da duoc dong goi san du lieu boi
 * scripts/vocab_etl/build_sqlite.py tren may tinh, KHONG tai/xu ly gi luc app
 * chay) vao dung thu muc database cua app TRUOC khi SQLiteOpenHelper mo no lan
 * dau - nen trong tinh huong binh thuong, [onCreate] se KHONG duoc framework
 * goi (file da ton tai san). [onCreate] van chua day du CREATE TABLE that (khop
 * 1-1 voi schema trong build_sqlite.py) de: (1) la "luoi an toan" neu vi ly do
 * nao do buoc copy o tren khong chay (vd test/mo truc tiep DATABASE_NAME ma
 * chua goi ham copy), va (2) chung minh ro rang lop nay dung dung API
 * SQLiteOpenHelper.onCreate()/onUpgrade() nhu yeu cau.
 */
class VocabDbHelper(private val context: Context) :
    SQLiteOpenHelper(context, VocabContract.DATABASE_NAME, null, VocabContract.DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${VocabContract.Words.TABLE} (
              ${VocabContract.Words.ID} TEXT PRIMARY KEY,
              ${VocabContract.Words.TOPIC_ID} TEXT NOT NULL,
              ${VocabContract.Words.WORD} TEXT NOT NULL,
              ${VocabContract.Words.LEMMA} TEXT NOT NULL,
              ${VocabContract.Words.POS} TEXT,
              ${VocabContract.Words.CEFR_LEVEL} TEXT,
              ${VocabContract.Words.FREQUENCY} REAL,
              ${VocabContract.Words.MEANING_VI} TEXT,
              ${VocabContract.Words.PHONETIC} TEXT,
              ${VocabContract.Words.VI_SOURCE} TEXT,
              ${VocabContract.Words.SOURCE} TEXT,
              ${VocabContract.Words.IS_MANUAL} INTEGER NOT NULL DEFAULT 0,
              ${VocabContract.Words.CREATED_AT} INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ${VocabContract.Definitions.TABLE} (
              ${VocabContract.Definitions.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
              ${VocabContract.Definitions.WORD_ID} TEXT NOT NULL REFERENCES ${VocabContract.Words.TABLE}(${VocabContract.Words.ID}),
              ${VocabContract.Definitions.SENSE_KEY} TEXT,
              ${VocabContract.Definitions.DEFINITION} TEXT NOT NULL,
              ${VocabContract.Definitions.CEFR_LEVEL} TEXT,
              ${VocabContract.Definitions.SOURCE} TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ${VocabContract.Synonyms.TABLE} (
              ${VocabContract.Synonyms.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
              ${VocabContract.Synonyms.WORD_ID} TEXT NOT NULL REFERENCES ${VocabContract.Words.TABLE}(${VocabContract.Words.ID}),
              ${VocabContract.Synonyms.SYNONYM} TEXT NOT NULL,
              ${VocabContract.Synonyms.SOURCE} TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ${VocabContract.Examples.TABLE} (
              ${VocabContract.Examples.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
              ${VocabContract.Examples.WORD_ID} TEXT NOT NULL REFERENCES ${VocabContract.Words.TABLE}(${VocabContract.Words.ID}),
              ${VocabContract.Examples.SOURCE} TEXT NOT NULL,
              ${VocabContract.Examples.SENTENCE} TEXT NOT NULL,
              ${VocabContract.Examples.SENTENCE_VI} TEXT,
              ${VocabContract.Examples.VI_SOURCE} TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ${VocabContract.UserWords.TABLE} (
              ${VocabContract.UserWords.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
              ${VocabContract.UserWords.WORD_ID} TEXT NOT NULL REFERENCES ${VocabContract.Words.TABLE}(${VocabContract.Words.ID}),
              ${VocabContract.UserWords.NOTE} TEXT,
              ${VocabContract.UserWords.ADDED_AT} INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_definitions_word_id ON ${VocabContract.Definitions.TABLE}(${VocabContract.Definitions.WORD_ID})")
        db.execSQL("CREATE INDEX idx_synonyms_word_id ON ${VocabContract.Synonyms.TABLE}(${VocabContract.Synonyms.WORD_ID})")
        db.execSQL("CREATE INDEX idx_examples_word_id ON ${VocabContract.Examples.TABLE}(${VocabContract.Examples.WORD_ID})")
        db.execSQL("CREATE INDEX idx_user_words_word_id ON ${VocabContract.UserWords.TABLE}(${VocabContract.UserWords.WORD_ID})")
        db.execSQL("CREATE INDEX idx_words_topic_id ON ${VocabContract.Words.TABLE}(${VocabContract.Words.TOPIC_ID})")
    }

    /**
     * Du an sinh vien, moi ban phat hanh (DATABASE_VERSION) tuong ung mot ban
     * vocab.db moi duoc dong lai tu dau boi ETL - nen nang cap don gian bang
     * xoa + tao lai toan bo bang, roi de [ensureDatabaseCopiedFromAssets] ghi
     * de bang du lieu moi (khong viet ALTER TABLE tung buoc nhu app san xuat
     * that, vi chua co du lieu nguoi dung can giu lai o cac bang ETL - rieng
     * du lieu nguoi dung tu them o `user_words`/`words(is_manual=1)` khong bi
     * dung tren duoi 1 nang cap thuong vi ETL chi ghi lai file moi khi co thay
     * doi schema, khong xoa database dang chay tren may nguoi dung).
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.UserWords.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.Examples.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.Synonyms.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.Definitions.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.Words.TABLE}")
        onCreate(db)
    }

    /**
     * Copy nguyen byte file `assets/vocab.db` (da co san du lieu tu ETL) vao
     * dung duong dan database cua app NEU CHUA TON TAI - khong parse/chay SQL
     * gi ca vi asset da la 1 file SQLite hoan chinh. An toan de goi nhieu lan
     * (kiem tra ton tai truoc). Phai goi truoc lan dau [getReadableDatabase]/
     * [getWritableDatabase] de framework mo dung file da co du lieu thay vi
     * tao moi database rong roi goi [onCreate].
     */
    fun ensureDatabaseCopiedFromAssets() {
        val destFile: File = context.getDatabasePath(VocabContract.DATABASE_NAME)
        if (destFile.exists()) return

        destFile.parentFile?.mkdirs()
        context.assets.open(VocabContract.DATABASE_NAME).use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}
