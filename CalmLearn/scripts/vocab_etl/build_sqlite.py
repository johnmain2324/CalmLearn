"""
Doc 4 file CSV trung gian trong ./out/ (sinh boi build_dataset.py) + tao file
SQLite vocab.db voi dung schema ma app Android se doc (xem
app/.../data/vocab/VocabDbHelper.kt - schema o day PHAI khop voi onCreate() ben
do, day la file duoc dong goi san (pre-populated) roi copy vao
app/src/main/assets/vocab.db, luc app chay lan dau se copy tu assets/ vao thu
muc database that cua app (xem VocabDbHelper.copyFromAssetsIfNeeded()).

Chay: python build_sqlite.py  (sau khi da chay build_dataset.py)
"""
import csv
import sqlite3
import time
from pathlib import Path

OUT_DIR = Path(__file__).parent / "out"
ASSETS_DB_PATH = Path(__file__).parent.parent.parent / "app" / "src" / "main" / "assets" / "vocab.db"

# Phai khop 1-1 voi onCreate() trong VocabDbHelper.kt
SCHEMA_SQL = """
CREATE TABLE words (
  id TEXT PRIMARY KEY,
  topic_id TEXT NOT NULL,
  word TEXT NOT NULL,
  lemma TEXT NOT NULL,
  pos TEXT,
  cefr_level TEXT,
  frequency REAL,
  meaning_vi TEXT,
  phonetic TEXT,
  vi_source TEXT,
  source TEXT,
  is_manual INTEGER NOT NULL DEFAULT 0,
  created_at INTEGER NOT NULL
);

CREATE TABLE definitions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  word_id TEXT NOT NULL REFERENCES words(id),
  sense_key TEXT,
  definition TEXT NOT NULL,
  cefr_level TEXT,
  source TEXT
);

CREATE TABLE synonyms (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  word_id TEXT NOT NULL REFERENCES words(id),
  synonym TEXT NOT NULL,
  source TEXT
);

CREATE TABLE examples (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  word_id TEXT NOT NULL REFERENCES words(id),
  source TEXT NOT NULL,
  sentence TEXT NOT NULL,
  sentence_vi TEXT,
  vi_source TEXT
);

CREATE TABLE user_words (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  word_id TEXT NOT NULL REFERENCES words(id),
  note TEXT,
  added_at INTEGER NOT NULL
);

CREATE INDEX idx_definitions_word_id ON definitions(word_id);
CREATE INDEX idx_synonyms_word_id ON synonyms(word_id);
CREATE INDEX idx_examples_word_id ON examples(word_id);
CREATE INDEX idx_user_words_word_id ON user_words(word_id);
CREATE INDEX idx_words_topic_id ON words(topic_id);
"""


def read_csv(name: str) -> list[dict]:
    with open(OUT_DIR / name, encoding="utf-8") as f:
        return list(csv.DictReader(f))


def nullable(value: str) -> str | None:
    return value if value not in ("", None) else None


def main() -> None:
    ASSETS_DB_PATH.parent.mkdir(parents=True, exist_ok=True)
    if ASSETS_DB_PATH.exists():
        ASSETS_DB_PATH.unlink()

    conn = sqlite3.connect(ASSETS_DB_PATH)
    conn.executescript(SCHEMA_SQL)
    # QUAN TRONG: SQLiteOpenHelper cua Android quyet dinh co goi onCreate()/onUpgrade()
    # hay khong dua vao PRAGMA user_version cua chinh file .db (khong phai vao viec
    # bang da ton tai hay chua). Neu khong set dong bang DATABASE_VERSION trong
    # VocabContract.kt, file nay se co user_version mac dinh = 0 -> Android tuong day
    # la database RONG can tao moi -> goi onCreate() -> "table already exists" crash
    # ngay ca khi du lieu da co san day du. Phai set dung so nay.
    conn.execute("PRAGMA user_version = 1")

    now_ms = int(time.time() * 1000)

    words = read_csv("words.csv")
    conn.executemany(
        """INSERT INTO words (id, topic_id, word, lemma, pos, cefr_level, frequency,
             meaning_vi, phonetic, vi_source, source, is_manual, created_at)
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        [
            (w["id"], w["topic_id"], w["word"], w["lemma"], nullable(w["pos"]),
             nullable(w["cefr_level"]), float(w["frequency"]) if w["frequency"] else None,
             nullable(w["meaning_vi"]), nullable(w["phonetic"]), nullable(w["vi_source"]),
             nullable(w["source"]), int(w["is_manual"]), now_ms)
            for w in words
        ],
    )

    definitions = read_csv("definitions.csv")
    conn.executemany(
        """INSERT INTO definitions (word_id, sense_key, definition, cefr_level, source)
           VALUES (?, ?, ?, ?, ?)""",
        [
            (d["word_id"], nullable(d["sense_key"]), d["definition"],
             nullable(d["cefr_level"]), nullable(d["source"]))
            for d in definitions
        ],
    )

    synonyms = read_csv("synonyms.csv")
    conn.executemany(
        "INSERT INTO synonyms (word_id, synonym, source) VALUES (?, ?, ?)",
        [(s["word_id"], s["synonym"], nullable(s["source"])) for s in synonyms],
    )

    examples = read_csv("examples.csv")
    conn.executemany(
        """INSERT INTO examples (word_id, source, sentence, sentence_vi, vi_source)
           VALUES (?, ?, ?, ?, ?)""",
        [
            (e["word_id"], e["source"], e["sentence"], nullable(e["sentence_vi"]), nullable(e["vi_source"]))
            for e in examples
        ],
    )

    conn.commit()

    counts = {
        table: conn.execute(f"SELECT COUNT(*) FROM {table}").fetchone()[0]
        for table in ("words", "definitions", "synonyms", "examples", "user_words")
    }
    conn.close()

    print(f"Wrote {ASSETS_DB_PATH} ({ASSETS_DB_PATH.stat().st_size} bytes)")
    print("Row counts:", counts)


if __name__ == "__main__":
    main()
