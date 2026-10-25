#!/usr/bin/env python3
"""Split the English HTTP query reference into MCP help topics.

Reads the canonical wiki page and writes:

  <out>/query-index.md
  <out>/queries/<anchor>.md

Sections are real level-2 headings with a docsify ``:id=`` anchor. Nested
headings stay in the parent section. Headings inside fenced code are ignored.
Same-page ``api-reference`` links become public docs URLs whose title is the
``help`` topic to open. Other ``/guide/...`` links become public docs URLs.

Default source is the sibling ``LifeUp-Wiki`` checkout. Default output is this
package's ``skills/lifeup-cloud/references`` directory. Re-running replaces
generated files and deletes stale ``queries/*.md`` only.
"""

from __future__ import annotations

import argparse
import re
from dataclasses import dataclass, field
from pathlib import Path
from urllib.parse import parse_qs, urlsplit

PUBLIC_ORIGIN = "https://docs.lifeupapp.fun/en/#"
PUBLIC_PAGE = f"{PUBLIC_ORIGIN}/guide/api-reference"
INDEX_TOPIC = "query-index"
ANCHOR_RE = re.compile(r"^[a-z0-9]+(?:-[a-z0-9]+)*$")
HEADING_RE = re.compile(r"(#{1,6})\s+(.*)")
ID_AT_END = re.compile(r"^(?P<title>.*?)\s*:id=(?P<anchor>\S+)\s*$")
LINK_RE = re.compile(r"(?<!!)\[([^\]\n]*)\]\(([^)\n]+)\)")
FENCE_OPEN_RE = re.compile(r"^(`{3,}|~{3,})")

MCP_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_OUT = MCP_ROOT / "skills" / "lifeup-cloud" / "references"
DEFAULT_WIKI = MCP_ROOT.parent.parent / "LifeUp-Wiki" / "docs" / "en" / "guide" / "api-reference.md"


class ExtractError(ValueError):
    """The wiki page cannot be split without dropping or inventing structure."""


@dataclass
class Section:
    anchor: str
    title: str
    lines: list[str]
    nested: list[str] = field(default_factory=list)

    def body(self) -> str:
        return "\n".join(self.lines) + "\n"


@dataclass
class Document:
    preamble: str
    sections: list[Section]
    anchors: dict[str, str]
    root_anchor: str | None


def normalize(text: str) -> str:
    if text.startswith("\ufeff"):
        text = text[1:]
    return text.replace("\r\n", "\n").replace("\r", "\n")


def scan_lines(text: str):
    fence: tuple[str, int] | None = None
    for lineno, line in enumerate(text.splitlines(), 1):
        if fence is not None:
            char, length = fence
            if re.fullmatch(rf"{re.escape(char)}{{{length},}}\s*", line):
                yield lineno, line, True
                fence = None
            else:
                yield lineno, line, True
            continue
        opener = FENCE_OPEN_RE.match(line)
        if opener:
            token = opener.group(1)
            fence = (token[0], len(token))
            yield lineno, line, True
            continue
        yield lineno, line, False


def parse_heading_anchor(raw: str, lineno: int) -> tuple[str | None, str]:
    if ":id=" not in raw:
        return None, raw
    match = ID_AT_END.fullmatch(raw)
    if not match or ":id=" in match.group("title"):
        raise ExtractError(f"invalid :id= on line {lineno}")
    return match.group("anchor"), match.group("title").strip()


def claim(anchors: dict[str, str], anchor: str, lineno: int) -> None:
    if not ANCHOR_RE.fullmatch(anchor):
        raise ExtractError(f"invalid id {anchor!r} on line {lineno}")
    if anchor in anchors:
        raise ExtractError(f"duplicate id {anchor!r} on line {lineno}")


def parse_document(text: str) -> Document:
    text = normalize(text)
    if not text.strip():
        raise ExtractError("empty source")

    preamble: list[str] = []
    sections: list[Section] = []
    current: Section | None = None
    anchors: dict[str, str] = {}
    root_anchor: str | None = None

    for lineno, line, in_fence in scan_lines(text):
        if not in_fence:
            heading = HEADING_RE.fullmatch(line)
            if heading:
                level = len(heading.group(1))
                anchor, title = parse_heading_anchor(heading.group(2).strip(), lineno)
                if level == 2:
                    if anchor is None:
                        raise ExtractError(f"missing :id= on level-2 heading at line {lineno}")
                    if not title:
                        raise ExtractError(f"invalid heading title on line {lineno}")
                    claim(anchors, anchor, lineno)
                    if current is not None:
                        sections.append(current)
                    current = Section(anchor, title, [line])
                    anchors[anchor] = f"query/{anchor}"
                    continue
                if anchor is not None:
                    if not title:
                        raise ExtractError(f"invalid heading title on line {lineno}")
                    claim(anchors, anchor, lineno)
                    if current is None:
                        anchors[anchor] = INDEX_TOPIC
                        if level == 1 and root_anchor is None:
                            root_anchor = anchor
                    else:
                        anchors[anchor] = f"query/{current.anchor}"
                        current.nested.append(anchor)
        if current is None:
            preamble.append(line)
        else:
            current.lines.append(line)

    if current is not None:
        sections.append(current)
    if not sections:
        raise ExtractError("no level-2 sections")

    preamble_text = "\n".join(preamble)
    if preamble:
        preamble_text += "\n"
    return Document(preamble_text, sections, anchors, root_anchor)


def split_url(dest: str) -> str:
    match = re.fullmatch(r"(\S+)(?:\s+\"[^\"]*\")?", dest.strip())
    if not match:
        raise ExtractError(f"unsupported link target {dest!r}")
    return match.group(1)


def format_anchor_link(label: str, anchor: str, anchors: dict[str, str]) -> str:
    topic = anchors.get(anchor)
    if topic is None:
        raise ExtractError(f"unknown anchor {anchor!r}")
    public = f"{PUBLIC_PAGE}?id={anchor}"
    return f'[{label}]({public} "help topic {topic}")'


def replace_link(match: re.Match[str], anchors: dict[str, str]) -> str:
    label = match.group(1)
    url = split_url(match.group(2))
    if url.startswith(("https://", "http://", "mailto:")):
        return match.group(0)
    if url.startswith("#"):
        return format_anchor_link(label, url[1:], anchors)
    if url.startswith("/"):
        parts = urlsplit(url)
        if parts.path.rstrip("/") == "/guide/api-reference":
            query = parse_qs(parts.query)
            anchor = query.get("id", [""])[0] or parts.fragment
            if not anchor:
                return f'[{label}]({PUBLIC_PAGE} "help topic {INDEX_TOPIC}")'
            return format_anchor_link(label, anchor, anchors)
        public = PUBLIC_ORIGIN + parts.path
        if parts.query:
            public += "?" + parts.query
        if parts.fragment:
            public += "#" + parts.fragment
        return f"[{label}]({public})"
    raise ExtractError(f"unsupported link target {url!r}")


def rewrite_links(text: str, anchors: dict[str, str]) -> str:
    if text == "":
        return ""
    out_lines: list[str] = []
    fence: tuple[str, int] | None = None
    for line in text.splitlines():
        if fence is not None:
            out_lines.append(line)
            char, length = fence
            if re.fullmatch(rf"{re.escape(char)}{{{length},}}\s*", line):
                fence = None
            continue
        opener = FENCE_OPEN_RE.match(line)
        if opener:
            token = opener.group(1)
            fence = (token[0], len(token))
            out_lines.append(line)
            continue
        out_lines.append(LINK_RE.sub(lambda match: replace_link(match, anchors), line))
    rendered = "\n".join(out_lines)
    if text.endswith("\n") or rendered:
        rendered += "\n"
    return rendered


def render_index(doc: Document, preamble: str) -> str:
    source = f"{PUBLIC_PAGE}?id={doc.root_anchor}" if doc.root_anchor else PUBLIC_PAGE
    lines = [
        "# Query reference index",
        "",
        "Catalog only. Do **not** read every section file.",
        "Need HTTP request parameters or a response model? `help` with `topic` = `query/<id>` (e.g. `query/task-history-models`, `query/task-queries`).",
        "This file is `help` topic `query-index`.",
        "Pass the topic cell exactly. Nested anchors are headings inside that file, not separate help topics.",
        "Rewritten links use a public docs URL. The link title is the `help` topic to open.",
        f"Source: {source}",
        "",
    ]
    if doc.root_anchor:
        lines.append(
            f"Document anchor `{doc.root_anchor}` is this file (`query-index`), not `query/{doc.root_anchor}`."
        )
        lines.append("")
    body = preamble.strip("\n")
    if body:
        lines.append(body)
        lines.append("")
    lines.extend(
        [
            "| topic | section | nested anchors |",
            "| --- | --- | --- |",
        ]
    )
    for section in doc.sections:
        title = section.title.replace("|", "\\|")
        nested = ", ".join(f"`{item}`" for item in section.nested) if section.nested else "-"
        lines.append(f"| `query/{section.anchor}` | {title} | {nested} |")
    return "\n".join(lines) + "\n"


def render_section(section: Section, body: str) -> str:
    topic = f"query/{section.anchor}"
    source = f"{PUBLIC_PAGE}?id={section.anchor}"
    text = body.strip("\n") + "\n"
    return f"# {section.title}\n\nSource: {source}\n\nHelp topic: `{topic}`\n\n{text}"


def build_files(text: str) -> dict[str, str]:
    doc = parse_document(text)
    files = {"query-index.md": render_index(doc, rewrite_links(doc.preamble, doc.anchors))}
    for section in doc.sections:
        files[f"queries/{section.anchor}.md"] = render_section(
            section, rewrite_links(section.body(), doc.anchors)
        )
    return files


def write_output(out: Path, files: dict[str, str]) -> None:
    out.mkdir(parents=True, exist_ok=True)
    queries = out / "queries"
    queries.mkdir(parents=True, exist_ok=True)
    written: set[str] = set()
    for rel, content in files.items():
        path = out / rel
        if path.parent != out and path.parent != queries:
            raise ExtractError(f"refusing to write {rel}")
        path.write_text(content, encoding="utf-8", newline="\n")
        if path.parent == queries:
            written.add(path.name)
    for old in queries.glob("*.md"):
        if old.name not in written:
            old.unlink()


def main(argv: list[str] | None = None) -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--wiki", type=Path, default=DEFAULT_WIKI, help="path to api-reference.md")
    parser.add_argument("--out", type=Path, default=DEFAULT_OUT, help="references directory to write")
    args = parser.parse_args(argv)
    if not args.wiki.is_file():
        raise SystemExit(f"wiki source not found: {args.wiki}")
    try:
        files = build_files(args.wiki.read_text(encoding="utf-8"))
    except ExtractError as exc:
        raise SystemExit(f"error: {exc}") from exc
    write_output(args.out, files)
    count = sum(1 for name in files if name.startswith("queries/"))
    print(f"wrote {count} query sections")


if __name__ == "__main__":
    main()
