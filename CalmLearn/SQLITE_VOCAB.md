# CSDL từ vựng bằng SQLite (CalmLearn)

> **Lưu ý cho lần đọc sau:** đây là tài liệu nộp bài, không phải ghi chú tạm —
> không tự xoá file này.

Tài liệu này mô tả phần lưu trữ từ vựng bằng SQLite thật (không dùng Room) vừa
thêm vào app CalmLearn: pipeline ETL offline lấy dữ liệu từ 4 nguồn thật, schema
quan hệ, lớp `SQLiteOpenHelper`, và các thao tác CRUD thật (`insert`/`query`/
`rawQuery`/`update`/`delete`) đứng sau một `VocabRepository`.

## 1. Nguồn dữ liệu (Phase 1 — đúng 40 từ đang dùng trong app, 8 chủ đề x 5 từ)

| # | Nguồn | Dùng để lấy | Cách lấy trong ETL |
|---|---|---|---|
| 1 | [Open English WordNet 2025](https://github.com/globalwordnet/english-wordnet) | POS, định nghĩa (gloss), ví dụ của WordNet, từ đồng nghĩa (cùng synset) | Tải gói `english-wordnet-2025-index.sense-fixed.zip` (bản WNDB cổ điển: `data.noun/verb/adj/adv` + `index.sense`), parse trực tiếp bằng Python thuần (không dùng thư viện `wn`) |
| 2 | [CEFR-J 1.5 (Octanove)](https://github.com/openlanguageprofiles/olp-en-cefrj) | CEFR **theo từ** (word-level) | Tải `cefrj-vocabulary-profile-1.5.csv`, khớp theo `(headword, pos)` |
| 3 | [CEFR-Annotated WordNet](https://huggingface.co/datasets/star092304/CEFR-Annotated-WordNet) | CEFR **theo sense** (sense-level, có thể khác CEFR theo từ) | Tải `wordnet_sensekey_cefr.tsv`, khớp theo `sense_key` (chuẩn Princeton WordNet 3.0 sense key — `index.sense` của OEWN vẫn giữ tương thích với chuẩn này) |
| 4 | [Tatoeba](https://tatoeba.org) | Câu ví dụ thật | Gọi API tìm kiếm công khai `api.tatoeba.org/unstable/sentences?lang=eng&q=<word>` cho từng từ (không tải nguyên dump Tatoeba vì chỉ cần 40 từ) |

**Không tự bịa:** field nào không khớp được ở bất kỳ nguồn nào thì để `NULL`,
không đoán. Xem coverage thật ở [`scripts/vocab_etl/out/report.md`](scripts/vocab_etl/out/report.md)
(sinh lại bằng `build_dataset.py`) — tóm tắt sau khi chạy lần này:

- 39/40 từ khớp được một *sense* thật trong OEWN (chỉ "battery life" không có —
  đây là cụm từ không tồn tại trong WordNet).
- 40/40 từ có ít nhất 1 câu ví dụ thật từ Tatoeba.
- 28/40 từ có CEFR theo từ (CEFR-J), 18/40 có CEFR theo sense (CEFR-Annotated
  WordNet) — phần còn lại để `NULL` vì CEFR-J/CEFR-Annotated WordNet không phủ
  hết mọi từ/sense.

**Với từ đa nghĩa** (vd "cast", "plot", "invite" có hàng chục sense trong
WordNet): script lọc theo đúng từ loại app đang dùng, rồi trong các sense cùng
từ loại, chọn sense có gloss OEWN trùng nhiều từ nhất với `definition_en` cũ của
app (so khớp thô bằng đếm từ chung, không dùng AI/embedding) — vẫn là một sense
**thật** trong WordNet, chỉ là heuristic chọn sense nào phù hợp nhất, có thể
chưa hoàn hảo 100%. Xem cột "match score" trong report.md.

### Trường không có trong 4 nguồn: nghĩa tiếng Việt + phiên âm IPA

Cả 4 nguồn trên đều **không có** tiếng Việt lẫn phiên âm IPA. App hiện tại đã
có sẵn 2 trường này (do đội ngũ tự biên soạn trước đây, không phải ETL bịa ra
lúc này). Quyết định (đã xác nhận với người yêu cầu task): giữ lại làm cột
riêng (`words.meaning_vi`, `words.phonetic`, `examples.sentence_vi`), gắn nhãn
nguồn `vi_source`/`examples.vi_source` = `"CalmLearn (bien soan thu cong, ngoai
4 nguon ETL)"` — **không bao giờ trộn lẫn** với cột `source`/`definitions.source`
của dữ liệu từ 4 nguồn thật. Cùng nguyên tắc đó áp dụng cho phần fallback tiếng
Anh (định nghĩa/ví dụ) của đúng 1 từ "battery life" — từ duy nhất OEWN không có
sense nào khớp.

## 2. Sơ đồ bảng (đã điều chỉnh nhẹ so với đề xuất ban đầu — xem lý do bên dưới)

```
words (id TEXT PK, topic_id, word, lemma, pos, cefr_level, frequency,
       meaning_vi, phonetic, vi_source, source, is_manual, created_at)
  │
  ├──< definitions (id PK, word_id FK, sense_key, definition, cefr_level, source)
  ├──< synonyms    (id PK, word_id FK, synonym, source)
  ├──< examples    (id PK, word_id FK, source, sentence, sentence_vi, vi_source)
  └──< user_words  (id PK, word_id FK, note, added_at)
```

- `words.id` **giữ nguyên** các id cũ (`travel_1`, `daily_2`, …) thay vì đánh số
  lại — vì `ProgressRepository`/Firestore đã lưu `learnedWordIds`/`favoriteWordIds`
  theo đúng các id này cho tài khoản đang dùng app; đổi id sẽ làm tiến độ cũ bị
  lệch.
- `definitions.cefr_level` tách riêng khỏi `words.cefr_level` đúng như đề xuất
  gốc: CEFR gắn theo **sense** (CEFR-Annotated WordNet) có thể khác CEFR gắn
  theo **từ** (CEFR-J).
- Thêm cột `source` ở `definitions`/`synonyms`/`examples` (đề xuất gốc chỉ có 1
  cột `source` ở `words`) — để mỗi định nghĩa/từ đồng nghĩa/ví dụ tự nói rõ đến
  từ OEWN, Tatoeba, hay là nội dung "legacy" có sẵn của app (không thuộc 4
  nguồn ETL) — tránh im lặng gộp 2 loại nguồn lại.
- **Bỏ `is_learned`/`is_favorite` khỏi `user_words`** (khác đề xuất gốc): 2
  trạng thái này đã có đúng MỘT nguồn sự thật là Firestore
  (`ProgressRepository.learnedWordIds`/`favoriteWordIds`, xem
  `data/progress/`) — áp dụng được cho MỌI từ kể cả từ người dùng tự thêm (chỉ
  là 1 tập hợp id dạng chuỗi). Thêm 2 cột này vào SQLite sẽ tạo ra 2 nơi lưu
  cùng một sự thật, dễ lệch nhau, và đi ngược đúng yêu cầu "Firestore giữ
  nguyên, không đụng vào" của task. `user_words` chỉ giữ phần SQLite thực sự bổ
  sung: `note` (ghi chú cá nhân, tính năng mới) và cờ `words.is_manual` (từ
  nào là do người dùng tự thêm, quyết định quyền sửa/xoá).

## 3. `SQLiteOpenHelper` thật — [`VocabDbHelper.kt`](app/src/main/java/com/education/calmlearn/data/vocab/VocabDbHelper.kt)

```kotlin
class VocabDbHelper(private val context: Context) :
    SQLiteOpenHelper(context, VocabContract.DATABASE_NAME, null, VocabContract.DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE ${VocabContract.Words.TABLE} (
              ${VocabContract.Words.ID} TEXT PRIMARY KEY,
              ${VocabContract.Words.TOPIC_ID} TEXT NOT NULL,
              ${VocabContract.Words.WORD} TEXT NOT NULL,
              ${VocabContract.Words.LEMMA} TEXT NOT NULL,
              ${VocabContract.Words.POS} TEXT,
              ${VocabContract.Words.CEFR_LEVEL} TEXT,
              ${VocabContract.Words.FREQUENCY} REAL,
              ${VocabContract.Words.MEANING_VI} TEXT,
              ${VocabContract.Words.PHONETIC} TEXT,
              ${VocabContract.Words.VI_SOURCE} TEXT,
              ${VocabContract.Words.SOURCE} TEXT,
              ${VocabContract.Words.IS_MANUAL} INTEGER NOT NULL DEFAULT 0,
              ${VocabContract.Words.CREATED_AT} INTEGER NOT NULL
            )
        """.trimIndent())
        // ... definitions / synonyms / examples / user_words + 5 CREATE INDEX
        // (nguyen van, xem file that - da rut gon o day cho de doc)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Du an sinh vien: nang cap = xoa + tao lai toan bo bang, roi de
        // ensureDatabaseCopiedFromAssets() ghi de bang du lieu moi.
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.UserWords.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.Examples.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.Synonyms.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.Definitions.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${VocabContract.Words.TABLE}")
        onCreate(db)
    }

    /** Pattern "prepackaged SQLite database": copy nguyen byte assets/vocab.db
     *  (da co san du lieu tu ETL) vao dung thu muc database cua app NEU CHUA
     *  TON TAI - phai goi TRUOC lan dau mo database de framework khong tu tao
     *  database rong roi goi onCreate(). */
    fun ensureDatabaseCopiedFromAssets() {
        val destFile: File = context.getDatabasePath(VocabContract.DATABASE_NAME)
        if (destFile.exists()) return
        destFile.parentFile?.mkdirs()
        context.assets.open(VocabContract.DATABASE_NAME).use { input ->
            FileOutputStream(destFile).use { output -> input.copyTo(output) }
        }
    }
}
```

Vì database đã có sẵn dữ liệu (copy từ `assets/`) trước khi
`SQLiteOpenHelper` mở nó lần đầu, `onCreate()` thường **không** được framework
gọi trong lúc chạy bình thường — nhưng vẫn là code CREATE TABLE thật, chạy
được độc lập nếu vì lý do nào đó bước copy chưa xảy ra (fallback an toàn), và
là bằng chứng dùng đúng API `SQLiteOpenHelper.onCreate()`/`onUpgrade()` như yêu
cầu.

> **Lỗi thực tế đã gặp khi test trên emulator (đã sửa):** `SQLiteOpenHelper`
> quyết định có gọi `onCreate()` hay không dựa vào `PRAGMA user_version` **của
> chính file `.db`**, không phải dựa vào việc bảng đã tồn tại hay chưa. File
> `vocab.db` ban đầu do `build_sqlite.py` tạo ra không set `user_version` nên
> mặc định là `0` — Android thấy `0 < DATABASE_VERSION (1)` nên tưởng đây là
> database rỗng cần tạo mới, gọi `onCreate()` trên một database **đã có sẵn**
> bảng `words` → crash `SQLiteException: table words already exists`. Đã sửa
> bằng cách thêm `conn.execute("PRAGMA user_version = 1")` vào cuối
> `build_sqlite.py` (khớp đúng `VocabContract.DATABASE_VERSION`) trước khi ghi
> file ra `assets/`.

## 4. CRUD thật — [`SqliteVocabRepository.kt`](app/src/main/java/com/education/calmlearn/data/vocab/SqliteVocabRepository.kt)

**INSERT** (dùng `ContentValues`, bọc trong 1 transaction vì ghi vào 4 bảng
cùng lúc) — trích từ `insertUserWord()`:

```kotlin
val wordValues = ContentValues().apply {
    put(VocabContract.Words.ID, wordId)
    put(VocabContract.Words.TOPIC_ID, topicId)
    put(VocabContract.Words.WORD, word)
    put(VocabContract.Words.IS_MANUAL, 1)
    put(VocabContract.Words.CREATED_AT, now)
    // ... cac cot con lai
}
db.insert(VocabContract.Words.TABLE, null, wordValues)
```

**QUERY + Cursor** — trích từ `readWords()`/`cursorToWord()`:

```kotlin
db.query(
    VocabContract.Words.TABLE,
    null, selection, selectionArgs, null, null,
    "${VocabContract.Words.TOPIC_ID} ASC, ${VocabContract.Words.WORD} ASC"
).use { cursor ->
    while (cursor.moveToNext()) { words += cursorToWord(db, cursor) }
}
```

**rawQuery + LIKE** (tìm kiếm) — trích từ `searchWords()`:

```kotlin
db.rawQuery(
    """SELECT id FROM words WHERE word LIKE ? COLLATE NOCASE OR meaning_vi LIKE ?
       ORDER BY word ASC""",
    arrayOf(like, like)
)
```

**UPDATE** — trích từ `updateUserWordNote()`:

```kotlin
val values = ContentValues().apply { put(VocabContract.UserWords.NOTE, note) }
db.update(VocabContract.UserWords.TABLE, values, "word_id = ?", arrayOf(wordId))
```

**DELETE** (chỉ cho từ `is_manual = 1`, xoá đúng thứ tự khoá ngoại) — trích từ
`deleteUserWord()`:

```kotlin
db.delete(VocabContract.UserWords.TABLE, "word_id = ?", arrayOf(wordId))
db.delete(VocabContract.Examples.TABLE, "word_id = ?", arrayOf(wordId))
db.delete(VocabContract.Synonyms.TABLE, "word_id = ?", arrayOf(wordId))
db.delete(VocabContract.Definitions.TABLE, "word_id = ?", arrayOf(wordId))
db.delete(VocabContract.Words.TABLE, "id = ? AND is_manual = 1", arrayOf(wordId))
```

Toàn bộ nằm sau `VocabRepository` (interface) + `VocabRepositoryProvider`
(singleton, cùng pattern với `AuthRepositoryProvider`/`ProgressRepositoryProvider`
đã có sẵn trong project) — Fragment **không bao giờ** đụng trực tiếp
`VocabDbHelper`/`Cursor`.

## 5. Luồng demo (build đã chạy `./gradlew assembleDebug` thành công, `assets/vocab.db` đã có trong APK)

1. Mở app → tab **Bài học** → mục **"Xem danh sách từ vựng (SQLite)"** (ngay
   trên lưới 8 chủ đề) → mở màn **Danh sách từ** — danh sách 40 từ đọc thật từ
   `words`/`definitions`/`synonyms`/`examples` qua `VocabRepository.getAllWords()`.
2. Gõ vào ô tìm kiếm (vd "journey") → gọi `VocabRepository.searchWords()`
   (`rawQuery` + `LIKE`) → danh sách lọc còn đúng các từ khớp.
3. Bấm dấu **+** góc trên phải → màn **Thêm từ** → điền "Từ mới" (bắt buộc) +
   các trường còn lại → **Lưu từ mới** → gọi
   `VocabRepository.insertUserWord()` (INSERT thật vào `words`/`definitions`/
   `synonyms`/`examples`/`user_words`).
4. Quay lại **Danh sách từ** (tự nạp lại ở `onResume()`) → từ vừa thêm xuất
   hiện ngay trong danh sách — chứng minh dữ liệu đã nằm trong SQLite, không
   phải chỉ giữ tạm trong bộ nhớ.
5. Kiểm tra trực tiếp bằng `adb` (tuỳ chọn, xác nhận độc lập với UI):
   ```
   adb shell run-as com.education.calmlearn \
     sqlite3 databases/vocab.db "select id, word, is_manual from words where is_manual=1;"
   ```
6. Bấm vào 1 từ trong danh sách (kể cả 40 từ ETL) → mở **Chi tiết từ** — vẫn
   màn hình cũ, chỉ khác nguồn dữ liệu giờ là SQLite (qua
   `MockData.ensureVocabWordsLoaded()` gọi `VocabRepository.getAllWords()`)
   thay vì literal hardcode như trước.

## 6. Danh sách file đã thêm/sửa

**ETL (Python, chạy trên máy tính lúc phát triển — `CalmLearn/scripts/vocab_etl/`):**
`requirements.txt`, `seed_words.py`, `fetch_sources.py`, `build_dataset.py`,
`build_sqlite.py`, `README.md` (`raw/`, `out/` không commit — xem `.gitignore`).

**Android — mới:**
`app/src/main/assets/vocab.db` (sinh bởi ETL) ·
`data/vocab/VocabContract.kt` · `data/vocab/VocabDbHelper.kt` ·
`data/vocab/VocabRepository.kt` · `data/vocab/VocabRepositoryProvider.kt` ·
`data/vocab/SqliteVocabRepository.kt` · `data/vocab/VocabInput.kt` ·
`ui/vocab/VocabListFragment.kt` · `ui/vocab/AddWordFragment.kt` ·
`res/layout/fragment_vocab_list.xml` · `res/layout/fragment_add_word.xml` ·
`res/drawable/ic_add.xml` ·
`app/src/test/java/.../data/vocab/VocabInputTest.kt`

**Android — sửa:** `data/mock/MockData.kt` (bỏ 40 `VocabWord(...)` hardcode,
nạp qua `ensureVocabWordsLoaded()`) · `data/model/VocabWord.kt` (thêm
`cefrLevel`, `isManual`) · `MainActivity.kt` (`VocabRepositoryProvider.init()`) ·
`ui/lessons/topic/TopicDetailFragment.kt` · `ui/lessons/topic/WordListAdapter.kt` ·
`ui/lessons/word/WordDetailFragment.kt` · `ui/lessons/flashcard/FlashcardFragment.kt` ·
`ui/home/HomeFragment.kt` · `ui/progress/ProgressFragment.kt` ·
`ui/lessons/LessonsFragment.kt` · `res/layout/fragment_lessons.xml` ·
`res/navigation/nav_graph.xml` · `res/values/strings.xml` · `.gitignore` (gốc repo).

## 7. Phạm vi KHÔNG làm trong lần này (đã đọc code, xác nhận lý do)

- **01 Splash, 02/03/04 Đăng nhập/Đăng ký/Quên mật khẩu, 10 Trang cá nhân**: đã
  có sẵn (Splash chủ động không làm — xem comment `MainActivity.applyStartDestination`),
  không đụng vào.
- **07 Quiz**: `MockData.quizQuestions` là câu hỏi trắc nghiệm ngữ pháp lẫn từ
  vựng, không map trực tiếp lên `words`/`definitions` — giữ nguyên nguồn
  `MockData` theo đúng điều khoản "chỉ đổi nếu hợp lý" của yêu cầu gốc.
- **08 Ôn tập**: `PracticeFragment` chỉ là menu điều hướng, không có dữ liệu
  riêng để đổi nguồn.
- **11 Chỉnh sửa hồ sơ, 12 Cài đặt, 14 Thông báo nhắc**: không liên quan CSDL
  từ vựng (11/12 là Firebase hồ sơ/tuỳ chọn app, 14 là thông báo hệ thống) —
  ngoài phạm vi yêu cầu SQLite từ vựng lần này, chưa làm.

## 8. Mở rộng sau Phase 1

Thêm từ mới vào `scripts/vocab_etl/seed_words.py` (được phép để
`meaning_vi`/`phonetic` = `None` nếu chưa ai dịch) rồi chạy lại
`fetch_sources.py` → `build_dataset.py` → `build_sqlite.py` — file
`assets/vocab.db` sẽ được ghi đè bằng bản mới có thêm từ, tra cứu qua đúng 4
nguồn y hệt 40 từ hiện tại.
