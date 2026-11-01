package com.education.calmlearn.ui.chatbot

data class Segment(val text: String, val bold: Boolean)

object SimpleMarkdown {

    private val BOLD = Regex("\\*\\*(.+?)\\*\\*", RegexOption.DOT_MATCHES_ALL)

    fun parse(raw: String): List<Segment> {
        val segments = mutableListOf<Segment>()
        var cursor = 0
        for (match in BOLD.findAll(raw)) {
            if (match.range.first > cursor) {
                segments.add(Segment(raw.substring(cursor, match.range.first), bold = false))
            }
            segments.add(Segment(match.groupValues[1], bold = true))
            cursor = match.range.last + 1
        }
        if (cursor < raw.length) {
            segments.add(Segment(raw.substring(cursor), bold = false))
        }
        return segments
    }
}
