package com.education.calmlearn.ui.chatbot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimpleMarkdownTest {

    @Test
    fun `text without markers comes back unchanged as one plain segment`() {
        assertEquals(listOf(Segment("Hello there!", false)), SimpleMarkdown.parse("Hello there!"))
    }

    @Test
    fun `an empty string gives no segments`() {
        assertTrue(SimpleMarkdown.parse("").isEmpty())
    }

    @Test
    fun `a pair in the middle of a sentence is split into three segments`() {
        assertEquals(
            listOf(Segment("We say ", false), Segment("instruments", true), Segment(" in plural.", false)),
            SimpleMarkdown.parse("We say **instruments** in plural.")
        )
    }

    @Test
    fun `several pairs in one sentence are all converted`() {
        assertEquals(
            listOf(
                Segment("Use ", false), Segment("a", true), Segment(" before consonants and ", false),
                Segment("an", true), Segment(" before vowels", false)
            ),
            SimpleMarkdown.parse("Use **a** before consonants and **an** before vowels")
        )
    }

    @Test
    fun `a pair at the very start or the very end leaves no empty segments`() {
        assertEquals(listOf(Segment("Great", true), Segment(" job", false)), SimpleMarkdown.parse("**Great** job"))
        assertEquals(listOf(Segment("Good ", false), Segment("work", true)), SimpleMarkdown.parse("Good **work**"))
    }

    @Test
    fun `a lone marker without a closing pair is kept as written`() {
        assertEquals(listOf(Segment("2 ** 3 is power", false)), SimpleMarkdown.parse("2 ** 3 is power"))
        assertEquals(
            listOf(Segment("a ", false), Segment("b", true), Segment(" and ** c", false)),
            SimpleMarkdown.parse("a **b** and ** c")
        )
    }

    @Test
    fun `bold content may contain spaces and punctuation`() {
        assertEquals(
            listOf(Segment("Try ", false), Segment("\"I'd like a coffee, please.\"", true)),
            SimpleMarkdown.parse("Try **\"I'd like a coffee, please.\"**")
        )
    }

    @Test
    fun `bold content may span a line break`() {
        assertEquals(listOf(Segment("line one\nline two", true)), SimpleMarkdown.parse("**line one\nline two**"))
    }

    @Test
    fun `single asterisks are not treated as bold`() {
        assertEquals(listOf(Segment("2 * 3 and *star*", false)), SimpleMarkdown.parse("2 * 3 and *star*"))
    }
}
