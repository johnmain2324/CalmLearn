package com.education.calmlearn.data.chatbot

enum class ChatbotErrorReason {
    SERVICE_NOT_CONFIGURED,
    NETWORK_ERROR,
    API_KEY_REJECTED,
    RATE_LIMITED,
    SERVER_ERROR,
    EMPTY_REPLY,
    HISTORY_READ_FAILED,
    HISTORY_SAVE_FAILED,
    UNKNOWN
}

sealed class ChatbotResult {
    data class Success(val reply: String) : ChatbotResult()
    data class Error(val reason: ChatbotErrorReason) : ChatbotResult()
}

sealed class ChatHistoryResult {
    data class Loaded(val messages: List<ChatMessage>) : ChatHistoryResult()
    object Saved : ChatHistoryResult()
    object NotSignedIn : ChatHistoryResult()
    data class Error(val reason: ChatbotErrorReason) : ChatHistoryResult()
}
