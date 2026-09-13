package com.example.calmlearn.ui.login

import com.example.calmlearn.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginFormValidatorTest {

    @Test
    fun `valid email and non-empty password has no error`() {
        assertNull(LoginFormValidator.firstError(LoginInput(email = "a@example.com", password = "anything")))
    }

    @Test
    fun `invalid email is rejected`() {
        val error = LoginFormValidator.firstError(LoginInput(email = "not-an-email", password = "anything"))
        assertEquals(R.string.login_error_email, error)
    }

    @Test
    fun `empty password is rejected`() {
        val error = LoginFormValidator.firstError(LoginInput(email = "a@example.com", password = ""))
        assertEquals(R.string.login_error_password, error)
    }

    @Test
    fun `password is not trimmed so a whitespace-only password is accepted by validator`() {
        // Chinh sach: khong tu trim mat khau - day la hanh vi co chu dich, khong phai bo sot.
        assertNull(LoginFormValidator.firstError(LoginInput(email = "a@example.com", password = "   ")))
    }
}
