package com.education.calmlearn.ui.login

import com.education.calmlearn.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginFormValidatorTest {

    @Test
    fun `valid email and non-empty password has no error`() {
        assertTrue(LoginFormValidator.errors(LoginInput(email = "a@example.com", password = "anything")).isValid)
    }

    @Test
    fun `invalid email is rejected`() {
        val errors = LoginFormValidator.errors(LoginInput(email = "not-an-email", password = "anything"))
        assertEquals(R.string.login_error_email, errors.email)
    }

    @Test
    fun `empty password is rejected`() {
        val errors = LoginFormValidator.errors(LoginInput(email = "a@example.com", password = ""))
        assertEquals(R.string.login_error_password, errors.password)
    }

    @Test
    fun `password is not trimmed so a whitespace-only password is accepted by validator`() {
        // Chinh sach: khong tu trim mat khau - day la hanh vi co chu dich, khong phai bo sot.
        assertNull(LoginFormValidator.errors(LoginInput(email = "a@example.com", password = "   ")).password)
    }

    @Test
    fun `both fields invalid are reported independently`() {
        val errors = LoginFormValidator.errors(LoginInput(email = "bad", password = ""))
        assertEquals(R.string.login_error_email, errors.email)
        assertEquals(R.string.login_error_password, errors.password)
    }
}
