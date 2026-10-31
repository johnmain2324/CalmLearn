package com.education.calmlearn.ui.notifications

import com.education.calmlearn.data.model.Achievement

sealed class NotificationItem {
    data class Reminder(val hour: Int, val minute: Int) : NotificationItem()
    data class AchievementUnlocked(val achievement: Achievement) : NotificationItem()
}

object NotificationItemsBuilder {

    fun build(
        reminderEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int,
        achievements: List<Achievement>,
        seenAchievementIds: Set<String>
    ): List<NotificationItem> {
        val items = mutableListOf<NotificationItem>()
        if (reminderEnabled) items.add(NotificationItem.Reminder(reminderHour, reminderMinute))
        achievements
            .filter { it.isUnlocked && it.id !in seenAchievementIds }
            .forEach { items.add(NotificationItem.AchievementUnlocked(it)) }
        return items
    }
}
