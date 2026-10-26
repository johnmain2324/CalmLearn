package com.education.calmlearn.data.progress

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object StreakCalculator {

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
