package com.education.calmlearn.ui.common

import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import com.education.calmlearn.R

/**
 * Ap dung trang thai hien/an mat khau MOT CACH IDEMPOTENT (khong dao nguoc trang thai), giu nguyen
 * noi dung da nhap va vi tri con tro. Dung ham nay de dong bo lai UI voi trang thai that luu trong
 * ViewModel (vd sau khi xoay man hinh, luc EditText bi tao lai va mat inputType da toggle truoc do
 * vi cac o mat khau duoc dat android:saveEnabled="false").
 */
fun applyEditTextPasswordVisibility(editText: EditText, toggleIcon: ImageView, visible: Boolean) {
    val selection = editText.selectionStart.coerceAtLeast(0)
    editText.inputType = if (visible) {
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    } else {
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    }
    toggleIcon.setImageResource(if (visible) R.drawable.ic_eye else R.drawable.ic_eye_off)
    editText.setSelection(selection.coerceIn(0, editText.text?.length ?: 0))
}

/**
 * Bat/tat hien mat khau ma van giu nguyen noi dung da nhap va vi tri con tro.
 * Tra ve trang thai hien thi moi (goi ngay sau do de luu lai vao ViewModel, tranh mat trang thai
 * khi xoay man hinh).
 */
fun toggleEditTextPasswordVisibility(editText: EditText, toggleIcon: ImageView, currentlyVisible: Boolean): Boolean {
    val newlyVisible = !currentlyVisible
    applyEditTextPasswordVisibility(editText, toggleIcon, newlyVisible)
    return newlyVisible
}
