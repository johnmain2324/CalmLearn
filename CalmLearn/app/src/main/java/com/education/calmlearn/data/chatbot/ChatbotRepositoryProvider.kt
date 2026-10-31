package com.education.calmlearn.data.chatbot

object ChatbotRepositoryProvider {
    val repository: ChatbotRepository by lazy { GeminiChatbotRepository() }
}
