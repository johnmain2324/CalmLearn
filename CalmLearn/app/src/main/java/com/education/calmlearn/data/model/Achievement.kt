package com.education.calmlearn.data.model

import androidx.annotation.DrawableRes

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    @DrawableRes val iconRes: Int,
    val isUnlocked: Boolean,
    val progressCurrent: Int,
    val progressTotal: Int
)
