"""
Tai du lieu THO tu 4 nguon da chot (xem CalmLearn/SQLITE_VOCAB.md muc "Nguon du
lieu") ve thu muc ./raw/ - chi tai 1 lan, lan sau chay lai se dung ban cache neu
da co san file (tranh tai lai nhieu lan khi chinh sua build_dataset.py).

Chay: python fetch_sources.py
"""
import json
import time
import zipfile
from pathlib import Path

import requests

from seed_words import SEED_WORDS

RAW_DIR = Path(__file__).parent / "raw"
RAW_DIR.mkdir(exist_ok=True)

HEADERS = {"User-Agent": "CalmLearn-vocab-etl/1.0 (school project, non-commercial)"}

# Goi zip nay chua toan bo du lieu WordNet dang WNDB co dien (data.noun/verb/adj/adv +
# index.sense) - du de lay POS/dinh nghia/vi du/dong nghia VA khop sense_key voi
# CEFR-Annotated WordNet ma khong can tai them file LMF XML rieng.
OEWN_RELEASE = "https://github.com/globalwordnet/english-wordnet/releases/download/2025-edition"
OEWN_SENSE_INDEX_URL = f"{OEWN_RELEASE}/english-wordnet-2025-index.sense-fixed.zip"

CEFRJ_URL = "https://raw.githubusercontent.com/openlanguageprofiles/olp-en-cefrj/master/cefrj-vocabulary-profile-1.5.csv"

CEFR_WORDNET_URL = (
    "https://huggingface.co/datasets/star092304/CEFR-Annotated-WordNet/resolve/main/"
    "cefr-level_annotation/wordnet_sensekey_cefr.tsv"
)

TATOEBA_SEARCH_URL = "https://api.tatoeba.org/unstable/sentences"


def download(url: str, dest: Path) -> None:
    if dest.exists() and dest.stat().st_size > 0:
        print(f"[skip cached] {dest.name}")
        return
    print(f"[downloading] {url}")
    resp = requests.get(url, headers=HEADERS, timeout=120, stream=True)
    resp.raise_for_status()
    with open(dest, "wb") as f:
        for chunk in resp.iter_content(chunk_size=1 << 16):
            f.write(chunk)
    print(f"[saved] {dest} ({dest.stat().st_size} bytes)")


def fetch_tatoeba_examples() -> None:
    """Goi API tim kiem cong khai cua Tatoeba cho tung lemma trong SEED_WORDS
    (thay vi tai toan bo Tatoeba dump - vai tram MB - chi de lay 40 tu)."""
    dest = RAW_DIR / "tatoeba_examples.json"
    if dest.exists() and dest.stat().st_size > 0:
        print(f"[skip cached] {dest.name}")
        return

    lemmas = sorted({w["word"] for w in SEED_WORDS})
    results: dict[str, list[dict]] = {}
    for lemma in lemmas:
        print(f"[tatoeba] searching: {lemma}")
        try:
            resp = requests.get(
                TATOEBA_SEARCH_URL,
                params={"lang": "eng", "q": lemma, "sort": "relevance", "limit": 5},
                headers=HEADERS,
                timeout=30,
            )
            resp.raise_for_status()
            data = resp.json()
            results[lemma] = [
                {"id": s["id"], "text": s["text"]} for s in data.get("data", [])
            ]
        except Exception as exc:  # network hiccup for one word shouldn't kill the run
            print(f"  !! failed for '{lemma}': {exc}")
            results[lemma] = []
        time.sleep(0.3)  # be polite to the public API

    with open(dest, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    print(f"[saved] {dest}")


def extract_oewn_wndb() -> None:
    extract_dir = RAW_DIR / "oewn_wndb"
    if (extract_dir / "oewn2025" / "index.sense").exists():
        print("[skip cached] oewn_wndb/ already extracted")
        return
    zip_path = RAW_DIR / "english-wordnet-2025-index.sense-fixed.zip"
    with zipfile.ZipFile(zip_path) as z:
        z.extractall(extract_dir)
    print(f"[extracted] {zip_path.name} -> {extract_dir}")


def main() -> None:
    download(OEWN_SENSE_INDEX_URL, RAW_DIR / "english-wordnet-2025-index.sense-fixed.zip")
    extract_oewn_wndb()
    download(CEFRJ_URL, RAW_DIR / "cefrj-vocabulary-profile-1.5.csv")
    download(CEFR_WORDNET_URL, RAW_DIR / "wordnet_sensekey_cefr.tsv")
    fetch_tatoeba_examples()
    print("\nDone. Raw files are in ./raw/")


if __name__ == "__main__":
    main()
