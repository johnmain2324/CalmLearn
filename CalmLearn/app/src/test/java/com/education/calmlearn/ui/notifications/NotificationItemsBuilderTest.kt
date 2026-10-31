package com.education.calmlearn.ui.notifications

import com.education.calmlearn.data.model.Achievement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationItemsBuilderTest {

    private fun achievement(id: String, unlocked: Boolean) =
        Achievement(id, "Title $id", "Desc", 0, unlocked, 0, 1)

    private fun build(
        reminderEnabled: Boolean = false,
        achievements: List<Achievement> = emptyList(),
        seen: Set<String> = emptySet()
    ) = NotificationItemsBuilder.build(reminderEnabled, 20, 0, achievements, seen)

    @Test
    fun `nothing enabled and nothing unlocked gives an empty list`() {
        assertTrue(build().isEmpty())
    }

    @Test
    fun `enabled reminder shows a single reminder item with its time`() {
        assertEquals(listOf(NotificationItem.Reminder(20, 0)), build(reminderEnabled = true))
    }

    @Test
    fun `unlocked and unseen achievement is listed after the reminder`() {
        val unlocked = achievement("a", true)
        val items = build(reminderEnabled = true, achievements = listOf(unlocked))
        assertEquals(listOf(NotificationItem.Reminder(20, 0), NotificationItem.AchievementUnlocked(unlocked)), items)
    }

    @Test
    fun `locked achievements are never listed`() {
        assertTrue(build(achievements = listOf(achievement("a", false))).isEmpty())
    }

    @Test
    fun `an achievement already seen is not listed again`() {
        val items = build(achievements = listOf(achievement("a", true), achievement("b", true)), seen = setOf("a"))
        assertEquals(listOf("b"), items.map { (it as NotificationItem.AchievementUnlocked).achievement.id })
    }
}
