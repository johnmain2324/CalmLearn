package com.example.calmlearn.data.onboarding

import android.content.Context

/**
 * Luu trang thai "da xem/bo qua Onboarding" bang SharedPreferences don gian (project chua dung
 * DataStore/Room). Day CHI la co dieu huong man hinh gioi thieu, KHONG phai bang chung xac thuc -
 * phien dang nhap that phai lay tu AuthRepository.isAuthenticated() (xem MainActivity).
 */
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
