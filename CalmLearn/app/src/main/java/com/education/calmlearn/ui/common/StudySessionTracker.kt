package com.education.calmlearn.ui.common

import android.os.SystemClock

class StudySessionTracker {

    private var resumedAtMillis: Long? = null

    fun start() {
        resumedAtMillis = SystemClock.elapsedRealtime()
    }

    fun elapsedSecondsAndReset(): Int {
        val startedAt = resumedAtMillis ?: return 0
        resumedAtMillis = null
        return ((SystemClock.elapsedRealtime() - startedAt) / 1000L).toInt()
    }
}
