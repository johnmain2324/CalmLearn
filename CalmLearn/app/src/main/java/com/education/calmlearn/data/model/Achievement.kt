package com.education.calmlearn.data.model

import androidx.annotation.DrawableRes

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    @DrawableRes val iconRes: Int,
    var isUnlocked: Boolean,
    var progressCurrent: Int,
    val progressTotal: Int
)
