package com.education.calmlearn.data.vocab

object VocabInput {

    fun parseSynonyms(raw: String): List<String> =
        raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
