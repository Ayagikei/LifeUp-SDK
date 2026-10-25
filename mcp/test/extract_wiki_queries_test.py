#!/usr/bin/env python3
"""Generator fixtures for the HTTP query-reference extractor."""

from __future__ import annotations

import importlib.util
import re
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "scripts" / "extract-wiki-queries.py"


def load_module():
    spec = importlib.util.spec_from_file_location("extract_wiki_queries", SCRIPT)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"cannot load {SCRIPT}")
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


MOD = load_module()

FIXTURE = """# HTTP API reference :id=http-query-reference

Intro needs [LifeUp Cloud](/guide/api_cloud) and [common](/guide/api-reference?id=http-query-reference).

## Real section :id=real-section

Before [other](#other-section) and [page](/guide/api-reference?id=nested-anchor).

```md
## Fake :id=NOT_VALID

[keep](/guide/api)
[keep-hash](#real-section)
```

### Nested :id=nested-anchor

Nested body stays here.

## Other :id=other-section

Back to [fields](#nested-anchor).
"""


class ExtractWikiQueriesTest(unittest.TestCase):
    def test_script_has_no_machine_specific_path(self) -> None:
        source = SCRIPT.read_text(encoding="utf-8")
        self.assertNotIn("/Users/", source)
        self.assertIn("--wiki", source)
        self.assertIn("--out", source)

    def test_fences_are_not_sections_and_links_are_rewritten(self) -> None:
        files = MOD.build_files(FIXTURE)
        self.assertEqual(
            set(files),
            {
                "query-index.md",
                "queries/real-section.md",
                "queries/other-section.md",
            },
        )
        index = files["query-index.md"]
        self.assertTrue(index.endswith("\n"))
        self.assertFalse(index.endswith("\n\n"))
        self.assertIn("Intro needs", index)
        self.assertIn("not separate help topics", index)
        self.assertIn("`query/real-section`", index)
        self.assertIn("`nested-anchor`", index)
        self.assertNotIn("query/fake", index)
        self.assertNotIn("NOT_VALID", index)
        self.assertIn("https://docs.lifeupapp.fun/en/#/guide/api_cloud", index)
        self.assertNotIn("](/guide", index)
        self.assertIn("Document anchor `http-query-reference` is this file (`query-index`)", index)

        real = files["queries/real-section.md"]
        self.assertIn("## Fake :id=NOT_VALID", real)
        self.assertIn("[keep](/guide/api)", real)
        self.assertIn("[keep-hash](#real-section)", real)
        self.assertIn("### Nested :id=nested-anchor", real)
        self.assertIn("Nested body stays here.", real)
        self.assertIn(
            '[other](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=other-section "help topic query/other-section")',
            real,
        )
        self.assertIn(
            '[page](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=nested-anchor "help topic query/real-section")',
            real,
        )
        self.assertIn("Source: https://docs.lifeupapp.fun/en/#/guide/api-reference?id=real-section", real)
        self.assertIn("Help topic: `query/real-section`", real)
        outside = _outside_fences(real)
        self.assertNotIn("](/guide", outside)
        self.assertNotIn("](#", outside)

        other = files["queries/other-section.md"]
        self.assertIn(
            '[fields](https://docs.lifeupapp.fun/en/#/guide/api-reference?id=nested-anchor "help topic query/real-section")',
            other,
        )
        self.assertNotIn("queries/nested-anchor.md", files)

    def test_duplicate_missing_and_invalid_ids_are_rejected(self) -> None:
        cases = {
            "duplicate": "# Doc :id=root\n\n## A :id=same\n\na\n\n## B :id=same\n\nb\n",
            "missing": "# Doc :id=root\n\n## No id here\n\ntext\n",
            "invalid": "# Doc :id=root\n\n## Bad :id=Not_Valid\n\ntext\n",
        }
        for label, text in cases.items():
            with self.subTest(label=label):
                with self.assertRaises(MOD.ExtractError) as caught:
                    MOD.build_files(text)
                self.assertIn(label if label != "invalid" else "invalid", str(caught.exception))

    def test_duplicate_nested_id_is_rejected(self) -> None:
        text = "# Doc :id=root\n\n## Parent :id=parent\n\n### Child :id=parent\n"
        with self.assertRaises(MOD.ExtractError) as caught:
            MOD.build_files(text)
        self.assertIn("duplicate", str(caught.exception))

    def test_traversal_anchor_is_invalid(self) -> None:
        text = "# Doc :id=root\n\n## Bad :id=../etc\n\nx\n"
        with self.assertRaises(MOD.ExtractError) as caught:
            MOD.build_files(text)
        self.assertIn("invalid", str(caught.exception))

    def test_empty_source_and_no_sections(self) -> None:
        for text in ("", "\n\n  \n", "\ufeff\n"):
            with self.assertRaises(MOD.ExtractError) as caught:
                MOD.build_files(text)
            self.assertIn("empty source", str(caught.exception))
        with self.assertRaises(MOD.ExtractError) as caught:
            MOD.build_files("# Doc :id=root\n\nJust intro.\n")
        self.assertIn("no level-2 sections", str(caught.exception))

    def test_build_is_deterministic(self) -> None:
        self.assertEqual(MOD.build_files(FIXTURE), MOD.build_files(FIXTURE))

    def test_cli_is_idempotent_and_deletes_only_stale_query_files(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            wiki = root / "api-reference.md"
            wiki.write_text(FIXTURE, encoding="utf-8")
            out = root / "references"
            self._run(wiki, out)
            first = _snapshot(out)
            (out / "query.md").write_text("workflow\n", encoding="utf-8")
            methods = out / "methods"
            methods.mkdir()
            (methods / "toast.md").write_text("method\n", encoding="utf-8")
            (out / "queries" / "stale-section.md").write_text("stale\n", encoding="utf-8")
            (out / "queries" / "notes.txt").write_text("note\n", encoding="utf-8")
            nested = out / "queries" / "extra"
            nested.mkdir()
            (nested / "keep.md").write_text("keep\n", encoding="utf-8")
            (out / "queries" / "real-section.md").write_text("mutated\n", encoding="utf-8")
            self._run(wiki, out)
            second = _snapshot(out)
            self.assertEqual(first["query-index.md"], second["query-index.md"])
            self.assertEqual(first["queries/real-section.md"], second["queries/real-section.md"])
            self.assertEqual(first["queries/other-section.md"], second["queries/other-section.md"])
            self.assertFalse((out / "queries" / "stale-section.md").exists())
            self.assertEqual((out / "query.md").read_text(encoding="utf-8"), "workflow\n")
            self.assertEqual((methods / "toast.md").read_text(encoding="utf-8"), "method\n")
            self.assertEqual((out / "queries" / "notes.txt").read_text(encoding="utf-8"), "note\n")
            self.assertEqual((nested / "keep.md").read_text(encoding="utf-8"), "keep\n")

    def test_failed_cli_does_not_touch_output(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            wiki = root / "empty.md"
            wiki.write_text("", encoding="utf-8")
            out = root / "references"
            queries = out / "queries"
            queries.mkdir(parents=True)
            (out / "query.md").write_text("keep\n", encoding="utf-8")
            (queries / "old.md").write_text("old\n", encoding="utf-8")
            result = subprocess.run(
                [sys.executable, str(SCRIPT), "--wiki", str(wiki), "--out", str(out)],
                capture_output=True,
                text=True,
                check=False,
            )
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("empty source", result.stderr)
            self.assertEqual((out / "query.md").read_text(encoding="utf-8"), "keep\n")
            self.assertEqual((queries / "old.md").read_text(encoding="utf-8"), "old\n")
            self.assertFalse((out / "query-index.md").exists())

    def test_real_wiki_matches_committed_help_files(self) -> None:
        if not MOD.DEFAULT_WIKI.is_file():
            self.skipTest("sibling wiki is not checked out")
        text = MOD.DEFAULT_WIKI.read_text(encoding="utf-8")
        files = MOD.build_files(text)
        self.assertIn("queries/task-history-models.md", files)
        self.assertIn("queries/task-queries.md", files)
        self.assertNotIn("queries/task-fields.md", files)
        self.assertNotIn("queries/get-tasks.md", files)
        joined = "\n".join(files.values())
        self.assertNotIn("](/guide", joined)
        self.assertNotIn("](#", joined)
        self.assertNotIn("/Users/", joined)
        self.assertIn("1.106.0+", files["query-index.md"])
        self.assertIn("`query/task-history-models`", files["query-index.md"])
        self.assertIn("`task-fields`", files["query-index.md"])
        self.assertIn("countProgress", files["queries/task-history-models.md"])
        self.assertIn("repeatEndCondition", files["queries/task-history-models.md"])
        self.assertIn("GET /history", files["queries/task-queries.md"])
        self.assertIn(
            "https://github.com/Ayagikei/LifeUp-SDK/blob/main/http/src/main/java/net/lifeupapp/lifeup/http/service/KtorService.kt",
            files["queries/api-common-sources.md"],
        )
        self.assertEqual(MOD.build_files(text), files)
        for rel, content in files.items():
            path = MOD.DEFAULT_OUT / rel
            self.assertTrue(path.is_file(), rel)
            self.assertEqual(path.read_text(encoding="utf-8"), content, rel)
        extra = {path.name for path in (MOD.DEFAULT_OUT / "queries").glob("*.md")} - {
            Path(rel).name for rel in files if rel.startswith("queries/")
        }
        self.assertEqual(extra, set())

    def _run(self, wiki: Path, out: Path) -> None:
        result = subprocess.run(
            [sys.executable, str(SCRIPT), "--wiki", str(wiki), "--out", str(out)],
            capture_output=True,
            text=True,
            check=False,
        )
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn("wrote ", result.stdout)


def _snapshot(out: Path) -> dict[str, str]:
    files: dict[str, str] = {}
    for path in out.rglob("*"):
        if path.is_file():
            files[path.relative_to(out).as_posix()] = path.read_text(encoding="utf-8")
    return files


def _outside_fences(text: str) -> str:
    kept: list[str] = []
    fence: tuple[str, int] | None = None
    for line in text.splitlines():
        if fence is not None:
            char, length = fence
            if re.fullmatch(rf"{re.escape(char)}{{{length},}}\s*", line):
                fence = None
            continue
        opener = re.match(r"^(`{3,}|~{3,})", line)
        if opener:
            token = opener.group(1)
            fence = (token[0], len(token))
            continue
        kept.append(line)
    return "\n".join(kept)


if __name__ == "__main__":
    unittest.main()
