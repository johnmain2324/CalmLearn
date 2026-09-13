package com.example.calmlearn.ui.common

import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import com.example.calmlearn.R

/**
 * Bat/tat hien mat khau ma van giu nguyen noi dung da nhap va vi tri con tro.
 * Tra ve trang thai hien thi moi (de goi lai the goi sau).
 */
fun toggleEditTextPasswordVisibility(editText: EditText, toggleIcon: ImageView, currentlyVisible: Boolean): Boolean {
    val selection = editText.selectionStart.coerceAtLeast(0)
    val newlyVisible = !currentlyVisible
    editText.inputType = if (newlyVisible) {
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    } else {
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    }
    toggleIcon.setImageResource(if (newlyVisible) R.drawable.ic_eye else R.drawable.ic_eye_off)
    editText.setSelection(selection.coerceIn(0, editText.text?.length ?: 0))
    return newlyVisible
}
