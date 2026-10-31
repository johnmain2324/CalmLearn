package com.education.calmlearn.data.chatbot

data class ChatMessage(
    val role: String,
    val text: String,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    companion object {
        const val ROLE_USER = "user"
        const val ROLE_MODEL = "model"
    }
}
