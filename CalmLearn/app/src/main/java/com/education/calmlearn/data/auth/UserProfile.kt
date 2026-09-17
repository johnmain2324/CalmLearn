package com.education.calmlearn.data.auth

/**
 * Ho so nguoi dung dang dang nhap, doc tu dich vu xac thuc/co so du lieu that (khong phai mock).
 *
 * [gender] la NULLABLE va chi co nghia "chua biet/chua ghi nhan duoc" khi null - KHONG duoc nham
 * voi [Gender.OTHER] (la lua chon that su nguoi dung bam chon "Khac" luc dang ky). Moi noi doc
 * gia tri nay phai tu quyet dinh hien thi the nao khi null (vd "Chua cap nhat"), khong duoc tu
 * gan mot gia tri Gender cu the thay cho no.
 */
data class UserProfile(
    val uid: String,
    val fullName: String,
    val email: String,
    val gender: Gender?
)
