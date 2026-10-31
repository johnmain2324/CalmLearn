package com.education.calmlearn.data.chatbot

interface ChatHistoryRepository {

    suspend fun loadRecentMessages(): ChatHistoryResult

    suspend fun saveMessage(message: ChatMessage): ChatHistoryResult
}
