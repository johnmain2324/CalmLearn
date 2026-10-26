package com.education.calmlearn.data.auth

data class UserProfile(
    val uid: String,
    val fullName: String,
    val email: String,
    val gender: Gender?
)
