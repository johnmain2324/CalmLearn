package com.education.calmlearn.ui.chatbot

import androidx.annotation.StringRes
import com.education.calmlearn.R
import com.education.calmlearn.data.chatbot.ChatbotErrorReason

@StringRes
fun ChatbotErrorReason.toMessageRes(): Int = when (this) {
    ChatbotErrorReason.SERVICE_NOT_CONFIGURED -> R.string.chatbot_error_not_configured
    ChatbotErrorReason.NETWORK_ERROR -> R.string.chatbot_error_network
    ChatbotErrorReason.API_KEY_REJECTED -> R.string.chatbot_error_api_key
    ChatbotErrorReason.RATE_LIMITED -> R.string.chatbot_error_rate_limited
    ChatbotErrorReason.SERVER_ERROR -> R.string.chatbot_error_server
    ChatbotErrorReason.EMPTY_REPLY -> R.string.chatbot_error_empty_reply
    ChatbotErrorReason.HISTORY_READ_FAILED -> R.string.chatbot_error_history_read
    ChatbotErrorReason.HISTORY_SAVE_FAILED -> R.string.chatbot_error_history_save
    ChatbotErrorReason.UNKNOWN -> R.string.chatbot_error_unknown
}
