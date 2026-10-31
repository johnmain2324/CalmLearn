package com.education.calmlearn.ui.chatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object ChatBubbleGate {

    private val _quizInProgress = MutableLiveData(false)
    val quizInProgress: LiveData<Boolean> = _quizInProgress

    fun setQuizInProgress(inProgress: Boolean) {
        _quizInProgress.value = inProgress
    }
}
