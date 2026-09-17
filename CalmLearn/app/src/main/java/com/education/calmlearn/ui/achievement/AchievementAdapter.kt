package com.education.calmlearn.ui.achievement

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.education.calmlearn.R
import com.education.calmlearn.data.model.Achievement
import com.education.calmlearn.databinding.ItemAchievementRowBinding

class AchievementAdapter(
    private val achievements: List<Achievement>,
    private val onAchievementClick: (Achievement) -> Unit
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    private val seenIds = mutableSetOf<String>()

    fun markSeen(achievementId: String) {
        if (seenIds.add(achievementId)) {
            val index = achievements.indexOfFirst { it.id == achievementId }
            if (index >= 0) notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position], seenIds.contains(achievements[position].id))
    }

    override fun getItemCount(): Int = achievements.size

    inner class AchievementViewHolder(
        private val binding: ItemAchievementRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(achievement: Achievement, isSeen: Boolean) {
            val context = binding.root.context

            binding.rowAchievementTitle.text = achievement.title
            binding.rowAchievementDesc.text = achievement.description
            binding.rowAchievementProgress.text = context.getString(
                R.string.achievement_progress_format,
                achievement.progressCurrent,
                achievement.progressTotal
            )
            binding.rowAchievementIcon.setImageResource(achievement.iconRes)
            binding.rowAchievementSeenBadge.visibility =
                if (isSeen) android.view.View.VISIBLE else android.view.View.GONE

            if (achievement.isUnlocked) {
                binding.rowAchievementIcon.setBackgroundResource(R.drawable.bg_icon_circle_amber_light)
                binding.rowAchievementIcon.setColorFilter(context.getColor(R.color.amber))
                binding.rowAchievementTitle.setTextColor(context.getColor(R.color.text_primary))
                binding.rowAchievementStatus.text = context.getString(R.string.achievement_status_unlocked)
                binding.rowAchievementStatus.setBackgroundResource(R.drawable.bg_pill_teal_light)
                binding.rowAchievementStatus.setTextColor(context.getColor(R.color.teal_primary))
            } else {
                binding.rowAchievementIcon.setBackgroundResource(R.drawable.bg_icon_circle_gray)
                binding.rowAchievementIcon.setColorFilter(context.getColor(R.color.text_hint))
                binding.rowAchievementTitle.setTextColor(context.getColor(R.color.text_hint))
                binding.rowAchievementStatus.text = context.getString(R.string.achievement_status_locked)
                binding.rowAchievementStatus.setBackgroundResource(R.drawable.bg_pill_light_gray)
                binding.rowAchievementStatus.setTextColor(context.getColor(R.color.text_hint))
            }

            binding.root.setOnClickListener { onAchievementClick(achievement) }
        }
    }
}
