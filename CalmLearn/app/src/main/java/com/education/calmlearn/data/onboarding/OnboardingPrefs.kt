package com.education.calmlearn.data.onboarding

import android.content.Context

class OnboardingPrefs(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isCompleted(): Boolean = prefs.getBoolean(KEY_COMPLETED, false)

    fun setCompleted() {
        prefs.edit().putBoolean(KEY_COMPLETED, true).apply()
    }

    private companion object {
        const val PREFS_NAME = "calmlearn_onboarding"
        const val KEY_COMPLETED = "completed"
    }
}
