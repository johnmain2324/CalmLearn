package com.education.calmlearn.ui.chatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.education.calmlearn.data.chatbot.ChatHistoryRepository
import com.education.calmlearn.data.chatbot.ChatHistoryRepositoryProvider
import com.education.calmlearn.data.chatbot.ChatHistoryResult
import com.education.calmlearn.data.chatbot.ChatMessage
import com.education.calmlearn.data.chatbot.ChatbotErrorReason
import com.education.calmlearn.data.chatbot.ChatbotRepository
import com.education.calmlearn.data.chatbot.ChatbotRepositoryProvider
import com.education.calmlearn.data.chatbot.ChatbotResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class ChatbotError(val reason: ChatbotErrorReason, val failedText: String = "")

class ChatbotViewModel(
    private val chatbot: ChatbotRepository = ChatbotRepositoryProvider.repository,
    private val history: ChatHistoryRepository = ChatHistoryRepositoryProvider.repository
) : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _isReplying = MutableLiveData(false)
    val isReplying: LiveData<Boolean> = _isReplying

    private val _error = MutableLiveData<ChatbotError?>(null)
    val error: LiveData<ChatbotError?> = _error

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val result = try {
                history.loadRecentMessages()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (unexpected: Exception) {
                ChatHistoryResult.Error(ChatbotErrorReason.HISTORY_READ_FAILED)
            }
            when (result) {
                is ChatHistoryResult.Loaded -> _messages.value = result.messages + _messages.value.orEmpty()
                is ChatHistoryResult.Error -> _error.value = ChatbotError(result.reason)
                else -> Unit
            }
        }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _isReplying.value == true) return

        val userMessage = ChatMessage(ChatMessage.ROLE_USER, trimmed)
        val previous = _messages.value.orEmpty()
        _messages.value = previous + userMessage
        _isReplying.value = true

        viewModelScope.launch {
            val result = try {
                chatbot.sendMessage(previous, trimmed)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (unexpected: Exception) {
                ChatbotResult.Error(ChatbotErrorReason.UNKNOWN)
            }

            when (result) {
                is ChatbotResult.Success -> {
                    val reply = ChatMessage(ChatMessage.ROLE_MODEL, result.reply)
                    _messages.value = _messages.value.orEmpty() + reply
                    _isReplying.value = false
                    saveMessages(userMessage, reply)
                }
                is ChatbotResult.Error -> {
                    _messages.value = _messages.value.orEmpty().filterNot { it === userMessage }
                    _isReplying.value = false
                    _error.value = ChatbotError(result.reason, trimmed)
                }
            }
        }
    }

    private suspend fun saveMessages(vararg messages: ChatMessage) {
        for (message in messages) {
            val result = try {
                history.saveMessage(message)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (unexpected: Exception) {
                ChatHistoryResult.Error(ChatbotErrorReason.HISTORY_SAVE_FAILED)
            }
            if (result is ChatHistoryResult.Error) {
                _error.value = ChatbotError(result.reason)
                return
            }
        }
    }

    fun consumeError() {
        _error.value = null
    }
}
