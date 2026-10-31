package com.education.calmlearn.ui.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.education.calmlearn.data.chatbot.ChatMessage
import com.education.calmlearn.databinding.ItemChatMessageAiBinding
import com.education.calmlearn.databinding.ItemChatMessageUserBinding

class ChatMessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()

    fun submitList(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    inner class UserViewHolder(val binding: ItemChatMessageUserBinding) : RecyclerView.ViewHolder(binding.root)

    inner class AiViewHolder(val binding: ItemChatMessageAiBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int =
        if (messages[position].role == ChatMessage.ROLE_USER) VIEW_TYPE_USER else VIEW_TYPE_AI

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            UserViewHolder(ItemChatMessageUserBinding.inflate(inflater, parent, false))
        } else {
            AiViewHolder(ItemChatMessageAiBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserViewHolder -> holder.binding.tvMessage.text = message.text
            is AiViewHolder -> holder.binding.tvMessage.text = message.text
        }
    }

    override fun getItemCount(): Int = messages.size

    private companion object {
        const val VIEW_TYPE_USER = 0
        const val VIEW_TYPE_AI = 1
    }
}
