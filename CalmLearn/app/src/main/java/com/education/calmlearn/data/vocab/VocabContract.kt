package com.education.calmlearn.data.vocab

/**
 * Ten bang/cot cua vocab.db - PHAI khop dung voi schema sinh boi
 * scripts/vocab_etl/build_sqlite.py (xem SQLITE_VOCAB.md o goc project).
 */
object VocabContract {

    const val DATABASE_NAME = "vocab.db"
    const val DATABASE_VERSION = 1

    object Words {
        const val TABLE = "words"
        const val ID = "id"
        const val TOPIC_ID = "topic_id"
        const val WORD = "word"
        const val LEMMA = "lemma"
        const val POS = "pos"
        const val CEFR_LEVEL = "cefr_level"
        const val FREQUENCY = "frequency"
        const val MEANING_VI = "meaning_vi"
        const val PHONETIC = "phonetic"
        const val VI_SOURCE = "vi_source"
        const val SOURCE = "source"
        const val IS_MANUAL = "is_manual"
        const val CREATED_AT = "created_at"
    }

    object Definitions {
        const val TABLE = "definitions"
        const val ID = "id"
        const val WORD_ID = "word_id"
        const val SENSE_KEY = "sense_key"
        const val DEFINITION = "definition"
        const val CEFR_LEVEL = "cefr_level"
        const val SOURCE = "source"
    }

    object Synonyms {
        const val TABLE = "synonyms"
        const val ID = "id"
        const val WORD_ID = "word_id"
        const val SYNONYM = "synonym"
        const val SOURCE = "source"
    }

    object Examples {
        const val TABLE = "examples"
        const val ID = "id"
        const val WORD_ID = "word_id"
        const val SOURCE = "source"
        const val SENTENCE = "sentence"
        const val SENTENCE_VI = "sentence_vi"
        const val VI_SOURCE = "vi_source"
    }

    object UserWords {
        const val TABLE = "user_words"
        const val ID = "id"
        const val WORD_ID = "word_id"
        const val NOTE = "note"
        const val ADDED_AT = "added_at"
    }
}
