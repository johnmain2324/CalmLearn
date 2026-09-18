package com.education.calmlearn.data.progress

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Logic tinh ngay/streak THUAN (khong phu thuoc Firestore/Android framework) - tach rieng khoi
 * FirebaseProgressRepository de unit test doc lap (xem StreakCalculatorTest). Dinh dang ngay luon
 * la "yyyy-MM-dd", theo timezone mac dinh cua thiet bi.
 */
object StreakCalculator {

    /** Streak = so ngay LIEN TIEP co it nhat mot hanh dong hoc tap moi. Tinh toan hoan toan tat
     *  dinh tu ngay hoat dong gan nhat, khong bao gio "bia" gia tri. */
    fun computeStreak(today: String, lastActiveDate: String?, currentStreak: Int): Int {
        if (lastActiveDate == null) return 1
        if (lastActiveDate == today) return currentStreak
        return if (lastActiveDate == previousDateString(today)) currentStreak + 1 else 1
    }

    fun previousDateString(dateStr: String): String {
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat().parse(dateStr) ?: Date()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat().format(calendar.time)
    }

    fun todayDateString(): String = dateFormat().format(Date())

    fun dateFormat(): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
}
