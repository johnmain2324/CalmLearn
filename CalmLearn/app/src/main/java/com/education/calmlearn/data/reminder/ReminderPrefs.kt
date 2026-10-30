package com.education.calmlearn.data.reminder

import android.content.Context

class ReminderPrefs(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_ENABLED, value).apply()
        }

    var hour: Int
        get() = prefs.getInt(KEY_HOUR, DEFAULT_HOUR)
        private set(value) {
            prefs.edit().putInt(KEY_HOUR, value).apply()
        }

    var minute: Int
        get() = prefs.getInt(KEY_MINUTE, DEFAULT_MINUTE)
        private set(value) {
            prefs.edit().putInt(KEY_MINUTE, value).apply()
        }

    fun setTime(hour: Int, minute: Int) {
        this.hour = hour
        this.minute = minute
    }

    private companion object {
        const val PREFS_NAME = "calmlearn_reminder"
        const val KEY_ENABLED = "enabled"
        const val KEY_HOUR = "hour"
        const val KEY_MINUTE = "minute"
        const val DEFAULT_HOUR = 20
        const val DEFAULT_MINUTE = 0
    }
}
