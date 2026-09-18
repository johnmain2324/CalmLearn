package com.education.calmlearn.ui.common

import android.os.SystemClock

/**
 * Do thoi gian THAT nguoi dung o trang thai foreground tren MOT man hinh hoc tap cu the (Flashcard,
 * Word Detail, Topic Detail, Grammar Detail, Quiz, Speaking, Listening). Goi start() trong
 * onResume() va elapsedSecondsAndReset() trong onPause() cua Fragment, roi bao cao ve
 * ProgressRepository.addStudySeconds() - xem cach dung trong cac Fragment tren.
 *
 * Dung SystemClock.elapsedRealtime() (khong bi anh huong boi nguoi dung doi gio he thong), khong
 * phu thuoc Android framework nao khac nen co the goi truc tiep tu onResume/onPause cua Fragment.
 */
class StudySessionTracker {

    private var resumedAtMillis: Long? = null

    fun start() {
        resumedAtMillis = SystemClock.elapsedRealtime()
    }

    /** Tra ve so giay da o man hinh nay ke tu lan start() gan nhat, roi reset lai - goi 0 neu
     *  chua tung start() hoac da stop() roi (tranh cong trung khi onPause() goi nhieu lan). */
    fun elapsedSecondsAndReset(): Int {
        val startedAt = resumedAtMillis ?: return 0
        resumedAtMillis = null
        return ((SystemClock.elapsedRealtime() - startedAt) / 1000L).toInt()
    }
}
