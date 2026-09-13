package com.example.calmlearn.ui.common

import com.example.calmlearn.data.auth.AuthErrorReason

/** Trang thai chung cho cac man hinh tai khoan (Dang nhap / Dang ky / Quen mat khau). */
sealed class FormUiState {
    object Idle : FormUiState()
    object Loading : FormUiState()

    /** Loi tu dich vu (backend/mang) - khac voi loi nhap lieu (xem *FieldErrors rieng cua tung man hinh). */
    data class Error(val reason: AuthErrorReason) : FormUiState()

    /** Thanh cong va co the dieu huong tiep (vd: vao Trang chu). Day la trang thai MOT LAN - xem
     *  ham consumeTerminalState() o cac ViewModel de tranh phat lai gay dieu huong nhieu lan. */
    object Success : FormUiState()

    /**
     * Dang ky (tai khoan da tao nhung chua xac minh email/chua luu xong ho so) hoac Dang nhap
     * (phat hien tai khoan chua xac minh/chua co ho so) deu co the roi vao trang thai nay - Fragment
     * se dieu huong sang man hinh Xac minh email/Hoan tat ho so thay vi vao thang Trang chu.
     */
    object RequiresNextStep : FormUiState()
}
