package com.example.calmlearn.ui.register

import com.example.calmlearn.R
import com.example.calmlearn.data.auth.Gender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegisterFormValidatorTest {

    private fun validInput() = RegisterInput(
        fullName = "Nguyen Van A",
        email = "a@example.com",
        password = "123456",
        confirmPassword = "123456",
        gender = Gender.OTHER,
        agreedToTerms = true
    )

    @Test
    fun `fully valid input has no error`() {
        assertNull(RegisterFormValidator.firstError(validInput()))
    }

    @Test
    fun `blank full name is rejected`() {
        val input = validInput().copy(fullName = "   ")
        assertEquals(R.string.register_error_fullname, RegisterFormValidator.firstError(input))
    }

    @Test
    fun `invalid email is rejected`() {
        val input = validInput().copy(email = "not-an-email")
        assertEquals(R.string.register_error_email, RegisterFormValidator.firstError(input))
    }

    @Test
    fun `password shorter than minimum length is rejected`() {
        val input = validInput().copy(password = "123", confirmPassword = "123")
        assertEquals(R.string.register_error_password, RegisterFormValidator.firstError(input))
    }

    @Test
    fun `mismatched confirm password is rejected`() {
        val input = validInput().copy(confirmPassword = "different")
        assertEquals(R.string.register_error_confirm_password, RegisterFormValidator.firstError(input))
    }

    @Test
    fun `missing gender selection is rejected`() {
        val input = validInput().copy(gender = null)
        assertEquals(R.string.register_error_gender, RegisterFormValidator.firstError(input))
    }

    @Test
    fun `terms not agreed is rejected`() {
        val input = validInput().copy(agreedToTerms = false)
        assertEquals(R.string.register_error_terms, RegisterFormValidator.firstError(input))
    }
}
