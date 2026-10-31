package com.education.calmlearn.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.education.calmlearn.R
import com.education.calmlearn.databinding.ItemNotificationBinding

class NotificationAdapter(
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private val items = mutableListOf<NotificationItem>()

    fun submitList(newItems: List<NotificationItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class NotificationViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = items[position]
        val context = holder.binding.root.context
        when (item) {
            is NotificationItem.Reminder -> {
                holder.binding.notificationIcon.setImageResource(R.drawable.ic_bell)
                holder.binding.notificationIcon.setBackgroundResource(R.drawable.bg_icon_circle_teal_light)
                holder.binding.notificationIcon.setColorFilter(context.getColor(R.color.teal_primary))
                holder.binding.notificationText.text =
                    context.getString(R.string.notification_reminder_format, item.hour, item.minute)
            }
            is NotificationItem.AchievementUnlocked -> {
                holder.binding.notificationIcon.setImageResource(R.drawable.ic_medal)
                holder.binding.notificationIcon.setBackgroundResource(R.drawable.bg_icon_circle_amber_light)
                holder.binding.notificationIcon.setColorFilter(context.getColor(R.color.amber))
                holder.binding.notificationText.text =
                    context.getString(R.string.notification_achievement_format, item.achievement.title)
            }
        }
        holder.binding.root.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
