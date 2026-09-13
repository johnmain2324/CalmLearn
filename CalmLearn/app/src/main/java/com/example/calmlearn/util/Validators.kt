package com.example.calmlearn.util

/**
 * Ham kiem tra du lieu thuan Kotlin (khong phu thuoc android.util.Patterns) de co the
 * unit test bang JUnit thuong ma khong can Robolectric/instrumentation.
 */
object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun isBlank(text: String): Boolean = text.trim().isEmpty()
}
