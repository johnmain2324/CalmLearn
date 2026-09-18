package com.education.calmlearn.data.vocab

import android.content.Context

/**
 * Noi cung cap VocabRepository don gian (project chua dung Hilt/Koin) - cung
 * pattern voi AuthRepositoryProvider/ProgressRepositoryProvider. Khac 2 repo
 * kia (goi Firebase SDK tu khoi tao san), SQLiteOpenHelper can Context that
 * nen can goi [init] MOT LAN (tu MainActivity.onCreate) truoc khi dung
 * [repository] o bat ky Fragment nao.
 */
object VocabRepositoryProvider {

    private var appContext: Context? = null

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    val repository: VocabRepository by lazy {
        val context = requireNotNull(appContext) {
            "VocabRepositoryProvider.init(context) phai duoc goi truoc (xem MainActivity.onCreate)"
        }
        SqliteVocabRepository(context)
    }
}
