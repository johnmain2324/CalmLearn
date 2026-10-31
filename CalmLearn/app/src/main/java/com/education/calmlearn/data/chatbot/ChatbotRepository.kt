package com.education.calmlearn.data.chatbot

interface ChatbotRepository {

    suspend fun sendMessage(history: List<ChatMessage>, userMessage: String): ChatbotResult
}
