package com.education.calmlearn.data.vocab

import org.junit.Assert.assertEquals
import org.junit.Test

class VocabInputTest {

    @Test
    fun `splits comma separated synonyms and trims whitespace`() {
        assertEquals(listOf("trip", "voyage"), VocabInput.parseSynonyms("trip, voyage"))
        assertEquals(listOf("trip", "voyage"), VocabInput.parseSynonyms("  trip ,voyage  "))
    }

    @Test
    fun `drops empty entries from stray commas`() {
        assertEquals(listOf("trip", "voyage"), VocabInput.parseSynonyms("trip,, voyage,"))
        assertEquals(listOf("trip"), VocabInput.parseSynonyms(",trip,"))
    }

    @Test
    fun `blank input returns an empty list`() {
        assertEquals(emptyList<String>(), VocabInput.parseSynonyms(""))
        assertEquals(emptyList<String>(), VocabInput.parseSynonyms("   "))
        assertEquals(emptyList<String>(), VocabInput.parseSynonyms(",,,"))
    }

    @Test
    fun `single synonym with no comma is kept as one entry`() {
        assertEquals(listOf("baggage"), VocabInput.parseSynonyms("baggage"))
    }
}
