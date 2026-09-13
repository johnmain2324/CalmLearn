package com.example.calmlearn.ui.common

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/** Dong ban phim ao dang hien thi (dung khi xu ly nut Done tren ban phim de tu chuyen giao dien). */
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(windowToken, 0)
}
