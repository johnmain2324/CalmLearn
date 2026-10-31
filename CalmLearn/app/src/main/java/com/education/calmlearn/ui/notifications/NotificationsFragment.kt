package com.education.calmlearn.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.education.calmlearn.R
import com.education.calmlearn.data.mock.MockData
import com.education.calmlearn.data.model.Achievement
import com.education.calmlearn.data.notifications.NotificationPrefs
import com.education.calmlearn.data.progress.ProgressRepositoryProvider
import com.education.calmlearn.data.progress.ProgressResult
import com.education.calmlearn.data.reminder.ReminderPrefs
import com.education.calmlearn.databinding.FragmentNotificationsBinding
import com.education.calmlearn.ui.achievement.AchievementDetailActivity
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val adapter = NotificationAdapter { item -> onItemClicked(item) }

    private lateinit var reminderPrefs: ReminderPrefs
    private lateinit var notificationPrefs: NotificationPrefs

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reminderPrefs = ReminderPrefs(requireContext())
        notificationPrefs = NotificationPrefs(requireContext())

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            val result = ProgressRepositoryProvider.repository.loadProgress()
            if (result is ProgressResult.Loaded) MockData.applyAchievementProgress(result.progress)
            render()
        }
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null && ::reminderPrefs.isInitialized) render()
    }

    private fun render() {
        val items = NotificationItemsBuilder.build(
            reminderEnabled = reminderPrefs.isEnabled,
            reminderHour = reminderPrefs.hour,
            reminderMinute = reminderPrefs.minute,
            achievements = MockData.achievements,
            seenAchievementIds = notificationPrefs.seenAchievementIds()
        )
        adapter.submitList(items)
        binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun onItemClicked(item: NotificationItem) {
        when (item) {
            is NotificationItem.Reminder -> findNavController().navigate(R.id.action_global_settings)
            is NotificationItem.AchievementUnlocked -> {
                notificationPrefs.markAchievementSeen(item.achievement.id)
                startActivity(achievementDetailIntent(item.achievement))
            }
        }
    }

    private fun achievementDetailIntent(achievement: Achievement): Intent =
        Intent(requireContext(), AchievementDetailActivity::class.java).apply {
            putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_ID, achievement.id)
            putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_TITLE, achievement.title)
            putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_DESCRIPTION, achievement.description)
            putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_ICON_RES, achievement.iconRes)
            putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_UNLOCKED, achievement.isUnlocked)
            putExtra(AchievementDetailActivity.EXTRA_PROGRESS_CURRENT, achievement.progressCurrent)
            putExtra(AchievementDetailActivity.EXTRA_PROGRESS_TOTAL, achievement.progressTotal)
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
