package com.education.calmlearn.ui.common

import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import com.education.calmlearn.R

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

fun toggleEditTextPasswordVisibility(editText: EditText, toggleIcon: ImageView, currentlyVisible: Boolean): Boolean {
    val newlyVisible = !currentlyVisible
    applyEditTextPasswordVisibility(editText, toggleIcon, newlyVisible)
    return newlyVisible
}
