package com.education.calmlearn.data.vocab

import android.content.Context

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
