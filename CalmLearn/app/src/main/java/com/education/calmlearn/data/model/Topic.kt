package com.education.calmlearn.data.model

import androidx.annotation.DrawableRes

data class Topic(
    val id: String,
    val title: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val backgroundRes: Int,
    val totalWords: Int,
    val learnedWords: Int
) {
    val percent: Int get() = if (totalWords == 0) 0 else (learnedWords * 100) / totalWords
}
