package com.education.calmlearn.data.chatbot

object ChatHistoryRepositoryProvider {
    val repository: ChatHistoryRepository by lazy { FirestoreChatHistoryRepository() }
}
