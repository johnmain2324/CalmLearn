package com.education.calmlearn.data.notifications

import android.content.Context

class NotificationPrefs(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun seenAchievementIds(): Set<String> = prefs.getStringSet(KEY_SEEN_ACHIEVEMENTS, null)?.toSet() ?: emptySet()

    fun markAchievementSeen(achievementId: String) {
        prefs.edit().putStringSet(KEY_SEEN_ACHIEVEMENTS, seenAchievementIds() + achievementId).apply()
    }

    private companion object {
        const val PREFS_NAME = "calmlearn_notifications"
        const val KEY_SEEN_ACHIEVEMENTS = "seen_achievement_ids"
    }
}
