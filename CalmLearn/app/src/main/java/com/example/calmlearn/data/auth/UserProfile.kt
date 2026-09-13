package com.example.calmlearn.data.auth

/** Ho so nguoi dung dang dang nhap, doc tu dich vu xac thuc/co so du lieu that (khong phai mock). */
data class UserProfile(
    val uid: String,
    val fullName: String,
    val email: String,
    val gender: Gender
)
