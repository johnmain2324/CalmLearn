package com.example.calmlearn.ui.common

import com.example.calmlearn.data.auth.AuthErrorReason

/** Trang thai chung cho cac man hinh tai khoan (Dang nhap / Dang ky / Quen mat khau). */
sealed class FormUiState {
    object Idle : FormUiState()
    object Loading : FormUiState()
    data class Error(val reason: AuthErrorReason) : FormUiState()
    object Success : FormUiState()
}
