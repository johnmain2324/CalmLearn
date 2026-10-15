package com.education.calmlearn.data.vocab

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
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
     * Ban sao du lieu NGUOI DUNG trong bo nho - gom cac dong `words(is_manual=1)`
     * va cac dong con (user_words/definitions/synonyms/examples) tro toi chung.
     */
    private class UserDataBackup(
        val words: List<ContentValues>,
        val definitions: List<ContentValues>,
        val synonyms: List<ContentValues>,
        val examples: List<ContentValues>,
        val userWords: List<ContentValues>
    )

    /**
     * Thu tu BAT BUOC: (1) backup vao bo nho -> (2) DROP + onCreate -> (3) insert
     * lai backup. Phai backup TRUOC khi DROP vi sau DROP du lieu nguoi dung da
     * mat vinh vien; phai insert lai SAU onCreate vi luc do bang moi co cau truc
     * dung de nhan du lieu. Insert lai trong 1 transaction de hoac khoi phuc het,
     * hoac khong khoi phuc gi (khong de lai du lieu nua chung).
     *
     * Luu y: trong onUpgrade() file .db dang duoc framework mo + dang trong
     * transaction nen KHONG the xoa/ghi de file tu assets o day. Vi vay day chi
     * la lop an toan (giu duoc du lieu nguoi dung, bang ETL tam thoi rong). Duong
     * nang cap day du (nap lai vocab goc tu assets) nam o
     * [ensureDatabaseCopiedFromAssets] - chay TRUOC khi helper mo file, nen xoa va
     * copy file duoc an toan; do do onUpgrade() thuong khong con bi goi.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val backup = backupUserData(db)
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.UserWords.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.Examples.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.Synonyms.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.Definitions.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.Words.TABLE}")
        onCreate(db)
        restoreUserData(db, backup)
    }

    /** Doc cac dong nguoi dung tu them ra Cursor -> ContentValues (giu trong bo nho). */
    private fun backupUserData(db: SQLiteDatabase): UserDataBackup {
        val manualIds =
            "SELECT ${VocabContract.Words.ID} FROM ${VocabContract.Words.TABLE} WHERE ${VocabContract.Words.IS_MANUAL} = 1"

        fun readAll(sql: String): List<ContentValues> {
            val rows = mutableListOf<ContentValues>()
            db.rawQuery(sql, null).use { c ->
                while (c.moveToNext()) {
                    val values = ContentValues()
                    DatabaseUtils.cursorRowToContentValues(c, values)
                    rows += values
                }
            }
            return rows
        }

        fun readChildren(table: String, wordIdColumn: String) =
            readAll("SELECT * FROM $table WHERE $wordIdColumn IN ($manualIds)")

        return UserDataBackup(
            words = readAll("SELECT * FROM ${VocabContract.Words.TABLE} WHERE ${VocabContract.Words.IS_MANUAL} = 1"),
            definitions = readChildren(VocabContract.Definitions.TABLE, VocabContract.Definitions.WORD_ID),
            synonyms = readChildren(VocabContract.Synonyms.TABLE, VocabContract.Synonyms.WORD_ID),
            examples = readChildren(VocabContract.Examples.TABLE, VocabContract.Examples.WORD_ID),
            userWords = readChildren(VocabContract.UserWords.TABLE, VocabContract.UserWords.WORD_ID)
        )
    }

    /**
     * Insert lai backup vao 5 bang trong 1 transaction. words truoc, cac bang con
     * sau (khoa ngoai word_id). Bo cot `id` tu tang cua bang con de SQLite tu cap
     * id moi - tranh dung id voi cac dong ETL vua nap lai.
     */
    private fun restoreUserData(db: SQLiteDatabase, backup: UserDataBackup) {
        db.beginTransaction()
        try {
            backup.words.forEach {
                db.insertWithOnConflict(VocabContract.Words.TABLE, null, it, SQLiteDatabase.CONFLICT_REPLACE)
            }
            fun restoreChildren(table: String, idColumn: String, rows: List<ContentValues>) {
                rows.forEach { row ->
                    val values = ContentValues(row).apply { remove(idColumn) }
                    db.insert(table, null, values)
                }
            }
            restoreChildren(VocabContract.Definitions.TABLE, VocabContract.Definitions.ID, backup.definitions)
            restoreChildren(VocabContract.Synonyms.TABLE, VocabContract.Synonyms.ID, backup.synonyms)
            restoreChildren(VocabContract.Examples.TABLE, VocabContract.Examples.ID, backup.examples)
            restoreChildren(VocabContract.UserWords.TABLE, VocabContract.UserWords.ID, backup.userWords)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Copy nguyen byte file `assets/vocab.db` (da co san du lieu tu ETL) vao
     * dung duong dan database cua app. An toan de goi nhieu lan. Phai goi truoc
     * lan dau [getReadableDatabase]/[getWritableDatabase] de framework mo dung
     * file da co du lieu thay vi tao moi database rong roi goi [onCreate].
     *
     * Truong hop NANG CAP (file da ton tai nhung user_version < DATABASE_VERSION):
     * lam o day vi helper chua mo file nen duoc phep xoa/ghi de. Thu tu: backup
     * du lieu nguoi dung vao bo nho -> dong helper + xoa file cu (kem -journal/
     * -wal/-shm) -> copy vocab.db moi tu assets (nap lai tu vung goc) -> mo lai
     * va insert backup. File assets moi PHAI co `PRAGMA user_version` = so
     * DATABASE_VERSION moi (build_sqlite.py), neu khong onUpgrade() se bi goi.
     */
    fun ensureDatabaseCopiedFromAssets() {
        val destFile: File = context.getDatabasePath(VocabContract.DATABASE_NAME)
        if (!destFile.exists()) {
            copyAssetTo(destFile)
            return
        }

        val installedVersion = SQLiteDatabase.openDatabase(destFile.path, null, SQLiteDatabase.OPEN_READONLY)
            .use { it.version }
        if (installedVersion >= VocabContract.DATABASE_VERSION) return

        val backup = SQLiteDatabase.openDatabase(destFile.path, null, SQLiteDatabase.OPEN_READONLY)
            .use { backupUserData(it) }
        close()
        listOf("", "-journal", "-wal", "-shm").forEach { File(destFile.path + it).delete() }
        copyAssetTo(destFile)
        SQLiteDatabase.openDatabase(destFile.path, null, SQLiteDatabase.OPEN_READWRITE)
            .use { restoreUserData(it, backup) }
    }

    private fun copyAssetTo(destFile: File) {
        destFile.parentFile?.mkdirs()
        context.assets.open(VocabContract.DATABASE_NAME).use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}
