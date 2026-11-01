package com.education.calmlearn.ui.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechMatcherTest {

    @Test
    fun `case and punctuation are ignored`() {
        assertTrue(SpeechMatcher.matches("Do your chores", "Do your chores."))
    }

    @Test
    fun `a spoken text that contains the target or the other way round matches`() {
        assertTrue(SpeechMatcher.matches("please do your chores now", "do your chores"))
        assertTrue(SpeechMatcher.matches("chores", "do your chores"))
    }

    @Test
    fun `unrelated speech does not match and an empty target never matches`() {
        assertFalse(SpeechMatcher.matches("hello world", "do your chores"))
        assertFalse(SpeechMatcher.matches("anything", "..."))
    }
}
