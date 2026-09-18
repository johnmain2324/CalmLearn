# Vocab ETL (offline, Python)

Sinh ra `app/src/main/assets/vocab.db` (SQLite da co san du lieu, dong goi trong
app) tu 4 nguon that: Open English Wordnet 2025, CEFR-J 1.5 (Octanove), CEFR-
Annotated WordNet, Tatoeba. Xem giai thich day du (schema, quyet dinh thiet ke,
nguon tung truong) tai [`CalmLearn/SQLITE_VOCAB.md`](../../SQLITE_VOCAB.md).

Chi chay tren MAY TINH khi phat trien (khong chay tren dien thoai/trong app luc
runtime) - dung "prepackaged SQLite database" pattern: file `.db` da hoan chinh
duoc dong goi san trong `assets/`.

## Chay lai tu dau

```bash
cd CalmLearn/scripts/vocab_etl
pip install -r requirements.txt

python fetch_sources.py    # tai + cache 4 nguon vao ./raw/ (~20MB, can internet)
python build_dataset.py    # gop 4 nguon cho 40 tu trong seed_words.py -> ./out/*.csv + report.md
python build_sqlite.py     # doc ./out/*.csv -> ghi app/src/main/assets/vocab.db
```

`./raw/` va `./out/` khong commit vao git (xem `.gitignore`) - chi `vocab.db` da
build xong (trong `assets/`) va cac script Python la deliverable can luu lai.

## Mo rong sau nay (ngoai Phase 1)

Them tu moi: them 1 dict vao `SEED_WORDS` trong `seed_words.py` (co the de
`meaning_vi`/`phonetic` la `None` neu chua co ai dich - script da xu ly duoc gia
tri rong) roi chay lai 3 buoc tren. Tu do se tu dong duoc tra cuu qua ca 4 nguon
y het 40 tu hien tai.
