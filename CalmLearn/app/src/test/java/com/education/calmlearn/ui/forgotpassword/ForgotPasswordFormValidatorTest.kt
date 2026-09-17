package com.education.calmlearn.ui.forgotpassword

import com.education.calmlearn.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ForgotPasswordFormValidatorTest {

    @Test
    fun `valid email has no error`() {
        assertNull(ForgotPasswordFormValidator.emailError("a@example.com"))
    }

    @Test
    fun `blank email is rejected`() {
        assertEquals(R.string.register_error_email, ForgotPasswordFormValidator.emailError(""))
    }

    @Test
    fun `malformed email is rejected`() {
        assertEquals(R.string.register_error_email, ForgotPasswordFormValidator.emailError("not-an-email"))
    }
}
