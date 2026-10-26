package com.education.calmlearn.ui.common

import com.education.calmlearn.data.auth.AuthErrorReason

sealed class FormUiState {
    object Idle : FormUiState()
    object Loading : FormUiState()

    data class Error(val reason: AuthErrorReason) : FormUiState()

    object Success : FormUiState()

    object RequiresNextStep : FormUiState()
}
