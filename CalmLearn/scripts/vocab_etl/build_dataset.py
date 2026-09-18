"""
Chuan hoa + gop 4 nguon (da tai ve ./raw/ boi fetch_sources.py) cho dung 40 tu
trong seed_words.py, sinh ra 4 file CSV trung gian (./out/words.csv,
definitions.csv, synonyms.csv, examples.csv) + 1 bao cao coverage (./out/report.md).

Nguyen tac bat buoc: KHONG bia field nao - neu khong tim duoc khop trong 4
nguon thi de trong (NULL khi ghi vao SQLite o build_sqlite.py). Cac truong
"legacy" (meaning_vi, phonetic, va phan dinh nghia/vi du tieng Anh fallback khi
OEWN/Tatoeba khong co) duoc lay dung nguyen van tu seed_words.py (da co san
trong app tu truoc, khong phai AI bia ra luc chay ETL nay) va LUON duoc gan
mot gia tri "source" rieng, khong bao gio tron lan voi cot source cua du lieu
tu OEWN/CEFR-J/CEFR-Annotated WordNet/Tatoeba.

Cach chon "sense" dung cho tu da nghia (vd "cast", "plot", "invite" co hang
chuc nghia trong WordNet): loc theo dung tu loai (pos) da co san trong app,
roi trong cac nghia cung tu loai, chon nghia co gloss (dinh nghia tieng Anh
cua OEWN) trung nhieu tu nhat voi definition_en cu cua app (do tuong dong tho,
dem tu chung - khong dung AI/embedding). Neu hoa ra khong khop tot, van la
mot nghia THAT cua tu do trong WordNet (khong bia), chi la co the chua sat
nghia app dang dung - duoc ghi lai trong report.md de biet gioi han.

Chay: python build_dataset.py  (sau khi da chay fetch_sources.py)
"""
import csv
import json
import re
from pathlib import Path

from seed_words import SEED_WORDS, VI_SOURCE_LABEL

RAW_DIR = Path(__file__).parent / "raw"
OUT_DIR = Path(__file__).parent / "out"
OUT_DIR.mkdir(exist_ok=True)

OEWN_SOURCE_LABEL = "Open English Wordnet 2025 (globalwordnet/english-wordnet)"
CEFRJ_SOURCE_LABEL = "CEFR-J Vocabulary Profile 1.5 (Octanove / openlanguageprofiles)"
CEFR_WORDNET_SOURCE_LABEL = "CEFR-Annotated WordNet (huggingface.co/datasets/star092304/CEFR-Annotated-WordNet)"
TATOEBA_SOURCE_LABEL = "Tatoeba"
MANUAL_SOURCE_LABEL = VI_SOURCE_LABEL  # dung chung 1 nhan cho moi noi dung "khong thuoc 4 nguon ETL"

# app pos string -> WordNet single-letter pos code(s) to try, in order
POS_TO_WORDNET = {
    "n.": ["n"],
    "v.": ["v"],
    "adj.": ["a"],
    "adv.": ["r"],
    "n./v.": ["n", "v"],
    "v./n.": ["v", "n"],
    "n./v.'": ["n", "v"],
}
WORDNET_CODE_TO_LABEL = {"n": "n.", "v": "v.", "a": "adj.", "r": "adv.", "s": "adj."}
# sense_key ss_type digit -> wordnet single-letter code (classic WNDB / sense-key convention)
SENSE_KEY_SSTYPE_TO_CODE = {"1": "n", "2": "v", "3": "a", "4": "r", "5": "a"}
POS_TO_DATA_FILE = {"n": "data.noun", "v": "data.verb", "a": "data.adj", "r": "data.adv"}


def word_pos_candidates(app_pos: str) -> list[str]:
    return POS_TO_WORDNET.get(app_pos.strip(), ["n"])


def load_index_sense() -> dict[str, tuple[str, str, int]]:
    """sense_key -> (synset_offset, wn_pos_code, sense_number)."""
    path = RAW_DIR / "oewn_wndb" / "oewn2025" / "index.sense"
    result: dict[str, tuple[str, str, int]] = {}
    with open(path, encoding="utf-8") as f:
        for line in f:
            parts = line.split()
            if len(parts) < 3:
                continue
            sense_key, offset, sense_number = parts[0], parts[1], parts[2]
            ss_type_digit = sense_key.split("%", 1)[1][0] if "%" in sense_key else None
            wn_pos = SENSE_KEY_SSTYPE_TO_CODE.get(ss_type_digit, "n")
            result[sense_key] = (offset, wn_pos, int(sense_number))
    return result


def parse_gloss(raw_gloss: str) -> tuple[str, list[str]]:
    """WNDB gloss format: 'definition; "example one"; "example two"'."""
    examples = re.findall(r'"([^"]*)"', raw_gloss)
    definition = raw_gloss.split('"', 1)[0].strip()
    definition = definition.rstrip("; ").strip()
    return definition, examples


_data_file_cache: dict[str, dict[str, dict]] = {}


def load_synset(offset: str, wn_pos: str) -> dict | None:
    """Parses one WNDB synset line (classic Princeton/OEWN 'data.<pos>' format):
    offset lex_filenum ss_type w_cnt word lex_id [word lex_id ...] p_cnt [ptr...] | gloss
    """
    filename = POS_TO_DATA_FILE[wn_pos]
    if filename not in _data_file_cache:
        cache: dict[str, dict] = {}
        path = RAW_DIR / "oewn_wndb" / "oewn2025" / filename
        with open(path, encoding="utf-8") as f:
            for line in f:
                if not line[:1].isdigit():
                    continue  # license header lines
                off = line[:8]
                head, _, gloss_part = line.partition("|")
                tokens = head.split()
                w_cnt = int(tokens[3], 16)
                words = [tokens[4 + 2 * i] for i in range(w_cnt)]
                definition, examples = parse_gloss(gloss_part.strip())
                cache[off] = {"words": words, "definition": definition, "examples": examples}
        _data_file_cache[filename] = cache
    return _data_file_cache[filename].get(offset)


def load_index_sense_by_lemma(index_sense: dict[str, tuple[str, str, int]]) -> dict[str, list[str]]:
    by_lemma: dict[str, list[str]] = {}
    for sense_key in index_sense:
        lemma_part = sense_key.split("%", 1)[0]
        by_lemma.setdefault(lemma_part, []).append(sense_key)
    return by_lemma


def load_cefr_wordnet() -> dict[str, str]:
    path = RAW_DIR / "wordnet_sensekey_cefr.tsv"
    result: dict[str, str] = {}
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\n")
            if not line:
                continue
            sense_key, cefr = line.split("\t")
            result.setdefault(sense_key, cefr)  # keep first if duplicate key
    return result


def load_cefrj() -> dict[tuple[str, str], str]:
    """(headword_lower, pos_label) -> CEFR level. pos_label uses app-style 'n.'/'v.'/'adj.'/'adv.'."""
    pos_map = {
        "noun": "n.", "verb": "v.", "adjective": "adj.", "adverb": "adv.",
        "be-verb": "v.", "do-verb": "v.", "have-verb": "v.", "modal auxiliary": "v.",
    }
    path = RAW_DIR / "cefrj-vocabulary-profile-1.5.csv"
    result: dict[tuple[str, str], str] = {}
    with open(path, encoding="utf-8") as f:
        for row in csv.DictReader(f):
            pos_label = pos_map.get(row["pos"].strip())
            if not pos_label:
                continue
            for variant in row["headword"].split("/"):
                result[(variant.strip().lower(), pos_label)] = row["CEFR"].strip()
    return result


def load_tatoeba() -> dict[str, list[str]]:
    path = RAW_DIR / "tatoeba_examples.json"
    with open(path, encoding="utf-8") as f:
        raw = json.load(f)
    return {lemma: [s["text"] for s in sentences] for lemma, sentences in raw.items()}


def score_overlap(a: str, b: str) -> int:
    tokens_a = set(re.findall(r"[a-z']+", a.lower()))
    tokens_b = set(re.findall(r"[a-z']+", b.lower()))
    return len(tokens_a & tokens_b)


def pick_best_sense(lemma_key: str, wn_pos_options: list[str], legacy_definition: str,
                     index_sense: dict, by_lemma: dict) -> dict | None:
    candidates = []
    for sense_key in by_lemma.get(lemma_key, []):
        offset, wn_pos, sense_number = index_sense[sense_key]
        if wn_pos not in wn_pos_options:
            continue
        synset = load_synset(offset, wn_pos)
        if not synset:
            continue
        score = score_overlap(synset["definition"], legacy_definition)
        candidates.append((score, -sense_number, sense_key, wn_pos, synset))
    if not candidates:
        return None
    candidates.sort(reverse=True)
    score, neg_sense_number, sense_key, wn_pos, synset = candidates[0]
    return {
        "sense_key": sense_key,
        "wn_pos": wn_pos,
        "sense_number": -neg_sense_number,
        "match_score": score,
        "definition": synset["definition"],
        "examples": synset["examples"],
        "synonyms": [w.replace("_", " ") for w in synset["words"]],
    }


def main() -> None:
    index_sense = load_index_sense()
    by_lemma = load_index_sense_by_lemma(index_sense)
    cefr_wordnet = load_cefr_wordnet()
    cefrj = load_cefrj()
    tatoeba = load_tatoeba()

    words_rows, definitions_rows, synonyms_rows, examples_rows = [], [], [], []
    coverage = {"oewn_sense_matched": 0, "sense_cefr_found": 0, "word_cefr_found": 0,
                "tatoeba_example_found": 0, "oewn_example_found": 0, "total": len(SEED_WORDS)}
    report_lines = ["# ETL coverage report (Phase 1, 40 seed words)\n",
                    "| word | topic | OEWN sense matched | match score | sense CEFR | word CEFR | # Tatoeba ex. | # OEWN ex. |",
                    "|---|---|---|---|---|---|---|---|"]

    for w in SEED_WORDS:
        lemma_key = w["word"].replace(" ", "_")
        wn_pos_options = word_pos_candidates(w["pos"])
        best = pick_best_sense(lemma_key, wn_pos_options, w["definition_en"], index_sense, by_lemma)

        word_cefr = None
        for pos_label in {w["pos"].split("/")[0].strip() if "/" in w["pos"] else w["pos"], "n.", "v.", "adj.", "adv."}:
            word_cefr = cefrj.get((w["word"].lower(), pos_label))
            if word_cefr:
                break

        oewn_matched = best is not None
        sense_cefr = cefr_wordnet.get(best["sense_key"]) if best else None
        word_source_parts = []
        if oewn_matched:
            word_source_parts.append(OEWN_SOURCE_LABEL)
            coverage["oewn_sense_matched"] += 1
        if word_cefr:
            word_source_parts.append(CEFRJ_SOURCE_LABEL)
            coverage["word_cefr_found"] += 1
        if sense_cefr:
            coverage["sense_cefr_found"] += 1

        words_rows.append({
            "id": w["id"], "topic_id": w["topic_id"], "word": w["word"], "lemma": w["word"],
            "pos": (WORDNET_CODE_TO_LABEL[best["wn_pos"]] if best else w["pos"]),
            "cefr_level": word_cefr or "", "frequency": "",
            "meaning_vi": w["meaning_vi"], "phonetic": w["phonetic"],
            "vi_source": MANUAL_SOURCE_LABEL,
            "source": "; ".join(word_source_parts),
            "is_manual": 0,
        })

        if oewn_matched:
            definitions_rows.append({
                "word_id": w["id"], "sense_key": best["sense_key"], "definition": best["definition"],
                "cefr_level": sense_cefr or "", "source": OEWN_SOURCE_LABEL,
            })
            for syn in best["synonyms"]:
                if syn.lower() != w["word"].lower():
                    synonyms_rows.append({"word_id": w["id"], "synonym": syn, "source": OEWN_SOURCE_LABEL})
        else:
            definitions_rows.append({
                "word_id": w["id"], "sense_key": "", "definition": w["definition_en"],
                "cefr_level": "", "source": MANUAL_SOURCE_LABEL,
            })
            for syn in w["synonyms"]:
                synonyms_rows.append({"word_id": w["id"], "synonym": syn, "source": MANUAL_SOURCE_LABEL})

        tatoeba_sentences = tatoeba.get(w["word"], [])
        for sentence in tatoeba_sentences:
            examples_rows.append({
                "word_id": w["id"], "source": TATOEBA_SOURCE_LABEL, "sentence": sentence,
                "sentence_vi": "", "vi_source": "",
            })
        if tatoeba_sentences:
            coverage["tatoeba_example_found"] += 1

        oewn_examples = best["examples"] if best else []
        for sentence in oewn_examples:
            examples_rows.append({
                "word_id": w["id"], "source": "OEWN", "sentence": sentence,
                "sentence_vi": "", "vi_source": "",
            })
        if oewn_examples:
            coverage["oewn_example_found"] += 1

        if not tatoeba_sentences and not oewn_examples:
            # Khong nguon nao co vi du that -> dung lai vi du tieng Anh cu cua app (gan nhan ro rang)
            examples_rows.append({
                "word_id": w["id"], "source": MANUAL_SOURCE_LABEL, "sentence": w["example_en"],
                "sentence_vi": w["example_vi"], "vi_source": MANUAL_SOURCE_LABEL,
            })

        report_lines.append(
            f"| {w['word']} | {w['topic_id']} | {'yes (' + best['sense_key'] + ')' if best else 'NO (fallback)'} "
            f"| {best['match_score'] if best else '-'} | {sense_cefr or '-'} | {word_cefr or '-'} "
            f"| {len(tatoeba_sentences)} | {len(oewn_examples)} |"
        )

    report_lines.append("\n## Summary\n")
    for key, value in coverage.items():
        report_lines.append(f"- {key}: {value}")

    _write_csv(OUT_DIR / "words.csv", words_rows,
               ["id", "topic_id", "word", "lemma", "pos", "cefr_level", "frequency",
                "meaning_vi", "phonetic", "vi_source", "source", "is_manual"])
    _write_csv(OUT_DIR / "definitions.csv", definitions_rows,
               ["word_id", "sense_key", "definition", "cefr_level", "source"])
    _write_csv(OUT_DIR / "synonyms.csv", synonyms_rows, ["word_id", "synonym", "source"])
    _write_csv(OUT_DIR / "examples.csv", examples_rows,
               ["word_id", "source", "sentence", "sentence_vi", "vi_source"])

    with open(OUT_DIR / "report.md", "w", encoding="utf-8") as f:
        f.write("\n".join(report_lines) + "\n")

    print(f"words={len(words_rows)} definitions={len(definitions_rows)} "
          f"synonyms={len(synonyms_rows)} examples={len(examples_rows)}")
    print("Coverage:", coverage)
    print(f"Wrote CSVs + report to {OUT_DIR}")


def _write_csv(path: Path, rows: list[dict], fieldnames: list[str]) -> None:
    with open(path, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)


if __name__ == "__main__":
    main()
