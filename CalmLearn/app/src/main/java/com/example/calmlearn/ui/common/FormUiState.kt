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
     * Rieng cho Dang ky: tai khoan da duoc tao nhung dich vu CHUA cap phien dang nhap ngay (vd can
     * xac minh email / dang nhap lai). KHONG duoc tu dong dieu huong vao Trang chu khi gap trang
     * thai nay.
     */
    object RequiresNextStep : FormUiState()
}
