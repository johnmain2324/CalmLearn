package com.example.calmlearn.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun `valid email formats are accepted`() {
        assertTrue(Validators.isValidEmail("user@example.com"))
        assertTrue(Validators.isValidEmail("first.last@sub.example.co"))
        assertTrue(Validators.isValidEmail("  user@example.com  "))
    }

    @Test
    fun `invalid email formats are rejected`() {
        assertFalse(Validators.isValidEmail(""))
        assertFalse(Validators.isValidEmail("user"))
        assertFalse(Validators.isValidEmail("user@"))
        assertFalse(Validators.isValidEmail("user@.com"))
        assertFalse(Validators.isValidEmail("user example.com"))
    }

    @Test
    fun `isBlank treats whitespace-only text as blank`() {
        assertTrue(Validators.isBlank(""))
        assertTrue(Validators.isBlank("   "))
        assertFalse(Validators.isBlank("  a  "))
    }
}
