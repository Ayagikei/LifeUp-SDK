#!/usr/bin/env python3
"""Extract wiki method tables into progressive-disclosure skill files."""

from __future__ import annotations

import re
from pathlib import Path

WIKI = Path("/Users/kei/workspace/project/lifeup-wiki/docs/zh-cn/guide/api.md")
OUT = Path(__file__).resolve().parents[1] / "skills/lifeup-cloud/references"
METHODS_DIR = OUT / "methods"


def slug(method: str) -> str:
    return method.replace("/", "-")


def main() -> None:
    text = WIKI.read_text()
    start = text.find("**方法名：**")
    if start < 0:
        raise SystemExit("no methods found")
    body = text[start:]
    parts = re.split(r"\*\*方法名：\*\*", body)
    methods: list[tuple[str, str, str]] = []
    for part in parts[1:]:
        lines = part.splitlines()
        name = lines[0].strip()
        desc = ""
        for line in lines[:30]:
            if line.startswith("**说明：**"):
                desc = line.replace("**说明：**", "").strip()
                break
        content = "\n".join(lines[1:]).strip()
        leaked = list(re.finditer(r"\n### ", content))
        if leaked:
            content = content[: leaked[-1].start()]
        content = content.strip() + "\n"

        methods.append((name, desc, content))


    METHODS_DIR.mkdir(parents=True, exist_ok=True)
    for old in METHODS_DIR.glob("*.md"):
        old.unlink()

    index_rows = [
        "# API index",
        "",
        "Catalog only. Do **not** read every method file.",
        "Need params? `help` with `topic` = the method name (e.g. `add_task`).",
        "Wiki may lag: https://docs.lifeupapp.fun/zh-cn/#/guide/api",
        "",
        "| method | 功能 |",
        "| --- | --- |",
    ]
    for name, desc, content in methods:
        index_rows.append(f"| `{name}` | {desc or name} |")
        path = METHODS_DIR / f"{slug(name)}.md"
        path.write_text(
            f"# {name}\n\n"
            f"Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).\n\n"
            f"**方法名：**{name}\n\n"
            f"{content}"
        )

    index_rows.append("")
    (OUT / "api-index.md").write_text("\n".join(index_rows) + "\n")
    print(f"wrote {len(methods)} methods")


if __name__ == "__main__":
    main()
