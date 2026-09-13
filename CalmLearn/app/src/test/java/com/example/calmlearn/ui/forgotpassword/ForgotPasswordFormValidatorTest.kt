package com.example.calmlearn.ui.forgotpassword

import com.example.calmlearn.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ForgotPasswordFormValidatorTest {

    @Test
    fun `valid email has no error`() {
        assertNull(ForgotPasswordFormValidator.firstError("a@example.com"))
    }

    @Test
    fun `blank email is rejected`() {
        assertEquals(R.string.register_error_email, ForgotPasswordFormValidator.firstError(""))
    }

    @Test
    fun `malformed email is rejected`() {
        assertEquals(R.string.register_error_email, ForgotPasswordFormValidator.firstError("not-an-email"))
    }
}
