package com.example.calmlearn.data.model

import androidx.annotation.DrawableRes

data class OnboardingPage(
    @DrawableRes val iconRes: Int,
    val title: String,
    val description: String
)
