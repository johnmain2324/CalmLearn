package com.education.calmlearn.ui.common

import java.util.Locale

object SpeechMatcher {

    fun matches(spoken: String, target: String): Boolean {
        val normalizedSpoken = normalize(spoken)
        val normalizedTarget = normalize(target)
        if (normalizedTarget.isEmpty()) return false
        return normalizedSpoken == normalizedTarget ||
            normalizedSpoken.contains(normalizedTarget) ||
            normalizedTarget.contains(normalizedSpoken)
    }

    private fun normalize(text: String): String =
        text.lowercase(Locale.US).replace(Regex("[^a-z0-9 ]"), "").trim().replace(Regex("\\s+"), " ")
}
