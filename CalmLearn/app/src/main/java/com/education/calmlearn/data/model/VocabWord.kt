package com.education.calmlearn.data.model

data class VocabWord(
    val id: String,
    val topicId: String,
    val word: String,
    val phonetic: String,
    val wordType: String,
    val meaningVi: String,
    val definitionEn: String,
    val exampleEn: String,
    val exampleVi: String,
    val synonyms: List<String>,
    var isFavorite: Boolean = false,
    var isLearned: Boolean = false,
    /** CEFR (A1-C2) cua tu, doc tu SQLite (cot words.cefr_level) - null neu chua co
     *  nguon xac nhan (xem data/vocab/). Dung de hien thi trong man "Danh sach tu". */
    val cefrLevel: String? = null,
    /** true neu tu nay do nguoi dung tu them qua man "Them tu" (SQLite words.is_manual),
     *  false neu tu ETL co san. Quyet dinh co cho sua/xoa tu man Danh sach tu hay khong. */
    val isManual: Boolean = false
)
