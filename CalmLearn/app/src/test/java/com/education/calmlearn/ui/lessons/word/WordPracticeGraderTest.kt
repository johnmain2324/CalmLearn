package com.education.calmlearn.ui.lessons.word

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordPracticeGraderTest {

    @Test
    fun `first line DAT passes and keeps the remaining lines as feedback`() {
        val result = WordPracticeGrader.parse("DAT\nCâu 1: tốt\nCâu 2: tốt")
        assertEquals(GradeResult.Passed("Câu 1: tốt\nCâu 2: tốt"), result)
    }

    @Test
    fun `first line CHUA_DAT does not pass`() {
        val result = WordPracticeGrader.parse("CHUA_DAT\nCâu 3: sai thì")
        assertEquals(GradeResult.NotPassed("Câu 3: sai thì"), result)
    }

    @Test
    fun `surrounding whitespace and blank lines before the verdict are ignored`() {
        assertTrue(WordPracticeGrader.parse("\n  DAT  \nok") is GradeResult.Passed)
    }

    @Test
    fun `a verdict that is not an exact match is never treated as a pass`() {
        assertTrue(WordPracticeGrader.parse("**DAT**\nok") is GradeResult.Unparsable)
        assertTrue(WordPracticeGrader.parse("Dat\nok") is GradeResult.Unparsable)
        assertTrue(WordPracticeGrader.parse("DAT, nhưng còn lỗi") is GradeResult.Unparsable)
        assertTrue(WordPracticeGrader.parse("Câu 1 ổn. DAT") is GradeResult.Unparsable)
    }

    @Test
    fun `an empty reply is unparsable`() {
        assertEquals(GradeResult.Unparsable(""), WordPracticeGrader.parse("   "))
    }

    @Test
    fun `prompt lists the word and numbered sentences`() {
        val prompt = WordPracticeGrader.buildPrompt("routine", listOf("a", "b"))
        assertEquals("Từ cần dùng: routine\nCâu 1: a\nCâu 2: b", prompt)
    }
}
