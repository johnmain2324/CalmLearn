package com.education.calmlearn.data.model

data class User(
    val name: String,
    val email: String,
    val level: String,
    val xp: Int,
    val streakDays: Int,
    val avatarInitials: String
)
