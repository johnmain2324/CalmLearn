package com.education.calmlearn.ui.register

import com.education.calmlearn.R
import com.education.calmlearn.data.auth.Gender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun `fully valid input has no error on any field`() {
        assertTrue(RegisterFormValidator.errors(validInput()).isValid)
    }

    @Test
    fun `blank full name is rejected`() {
        val errors = RegisterFormValidator.errors(validInput().copy(fullName = "   "))
        assertEquals(R.string.register_error_fullname, errors.fullName)
    }

    @Test
    fun `invalid email is rejected`() {
        val errors = RegisterFormValidator.errors(validInput().copy(email = "not-an-email"))
        assertEquals(R.string.register_error_email, errors.email)
    }

    @Test
    fun `password shorter than minimum length is rejected`() {
        val errors = RegisterFormValidator.errors(validInput().copy(password = "123", confirmPassword = "123"))
        assertEquals(R.string.register_error_password, errors.password)
    }

    @Test
    fun `mismatched confirm password is rejected`() {
        val errors = RegisterFormValidator.errors(validInput().copy(confirmPassword = "different"))
        assertEquals(R.string.register_error_confirm_password, errors.confirmPassword)
    }

    @Test
    fun `missing gender selection is rejected`() {
        val errors = RegisterFormValidator.errors(validInput().copy(gender = null))
        assertEquals(R.string.register_error_gender, errors.gender)
    }

    @Test
    fun `terms not agreed is rejected`() {
        val errors = RegisterFormValidator.errors(validInput().copy(agreedToTerms = false))
        assertEquals(R.string.register_error_terms, errors.terms)
    }

    @Test
    fun `multiple invalid fields are all reported at once`() {
        val input = validInput().copy(fullName = "", email = "bad", agreedToTerms = false)
        val errors = RegisterFormValidator.errors(input)
        assertEquals(R.string.register_error_fullname, errors.fullName)
        assertEquals(R.string.register_error_email, errors.email)
        assertEquals(R.string.register_error_terms, errors.terms)
        // Cac truong con lai van hop le nen phai null, khong bi "an theo" loi dau tien nhu truoc.
        assertNull(errors.password)
        assertNull(errors.confirmPassword)
        assertNull(errors.gender)
    }

    @Test
    fun `changing base password updates confirm password error automatically`() {
        val touched = setOf(RegisterField.CONFIRM_PASSWORD)

        val stillMatching = RegisterFormValidator.errors(
            validInput().copy(password = "abcdef", confirmPassword = "abcdef")
        ).visibleTo(touched)
        assertNull(stillMatching.confirmPassword)

        // Nguoi dung doi mat khau goc sau khi da nhap xac nhan -> loi phai xuat hien lai vi
        // confirmPassword da duoc "touched" tu truoc.
        val nowMismatched = RegisterFormValidator.errors(
            validInput().copy(password = "zzzzzz", confirmPassword = "abcdef")
        ).visibleTo(touched)
        assertEquals(R.string.register_error_confirm_password, nowMismatched.confirmPassword)
    }

    @Test
    fun `errors are hidden until the field is touched`() {
        val errors = RegisterFormValidator.errors(validInput().copy(fullName = ""))
        assertEquals(R.string.register_error_fullname, errors.fullName)

        val hidden = errors.visibleTo(emptySet())
        assertNull(hidden.fullName)

        val shown = errors.visibleTo(setOf(RegisterField.FULL_NAME))
        assertEquals(R.string.register_error_fullname, shown.fullName)
    }
}
