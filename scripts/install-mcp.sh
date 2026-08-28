#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MCP_DIR="$ROOT_DIR/mcp"
DIST="$MCP_DIR/dist/index.js"

NO_BUILD=0
DRY_RUN=0
SELF_TEST=0
CLIENTS=auto

usage() {
  cat <<'EOF'
Usage: scripts/install-mcp.sh [options]

Build @lifeup/mcp and register it on detected MCP clients.
Re-runs update the existing `lifeup` entry instead of adding a second one.

Options:
  --clients=auto|all|codex,cursor,claude,claude-desktop,pi
  --no-build
  --dry-run
  --self-test
  -h, --help
EOF
}

info() { printf '==> %s\n' "$*"; }
die() { printf 'error: %s\n' "$*" >&2; exit 1; }

while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-build) NO_BUILD=1; shift ;;
    --dry-run) DRY_RUN=1; shift ;;
    --self-test) SELF_TEST=1; shift ;;
    --clients=*) CLIENTS="${1#*=}"; shift ;;
    --clients)
      [[ $# -ge 2 ]] || die "--clients needs a value"
      CLIENTS="$2"
      shift 2
      ;;
    -h|--help) usage; exit 0 ;;
    *) die "unknown argument: $1" ;;
  esac
done

NODE="$(command -v node)" || die "node not found"
[[ -x "$NODE" ]] || die "node not executable: $NODE"

if [[ "$SELF_TEST" != 1 && "$NO_BUILD" != 1 ]]; then
  info "building @lifeup/mcp"
  (
    cd "$MCP_DIR"
    npm install
    npm run build
  )
  [[ -f "$DIST" ]] || die "build did not produce $DIST"
elif [[ "$SELF_TEST" != 1 ]]; then
  [[ -f "$DIST" ]] || die "missing $DIST (run without --no-build)"
fi

python3 - "$NODE" "$DIST" "$CLIENTS" "$DRY_RUN" "$SELF_TEST" <<'PY'
from __future__ import annotations

import json
import os
import re
import sys
import tempfile
from pathlib import Path

node, dist, clients_arg, dry_run_s, self_test_s = sys.argv[1:]
dry_run = dry_run_s == "1"
self_test = self_test_s == "1"
dist = str(Path(dist).resolve()) if not self_test else dist
node = str(Path(node).resolve()) if Path(node).exists() else node

ALIASES = {"lifeup", "lifeup-cloud", "lifeup_cloud", "lifeup-mcp", "@lifeup/mcp"}
TABLE_HEAD = re.compile(r"^\[(.+)\]\s*$")


def home() -> Path:
    return Path.home()


def codex_config() -> Path:
    return Path(os.environ.get("CODEX_HOME", home() / ".codex")) / "config.toml"


def cursor_config() -> Path:
    return home() / ".cursor" / "mcp.json"


def claude_config() -> Path:
    return home() / ".claude.json"


def claude_desktop_config() -> Path:
    if sys.platform == "darwin":
        return home() / "Library" / "Application Support" / "Claude" / "claude_desktop_config.json"
    if os.name == "nt":
        return Path(os.environ.get("APPDATA", home() / "AppData" / "Roaming")) / "Claude" / "claude_desktop_config.json"
    return home() / ".config" / "Claude" / "claude_desktop_config.json"


def pi_config() -> Path:
    agent = Path(os.environ.get("PI_CODING_AGENT_DIR", home() / ".pi" / "agent"))
    return agent / "mcp.json"


def detect_clients() -> list[str]:
    found: list[str] = []
    if (home() / ".codex").exists() or os.environ.get("CODEX_HOME"):
        found.append("codex")
    if (home() / ".cursor").exists():
        found.append("cursor")
    if claude_config().exists():
        found.append("claude")
    desktop = claude_desktop_config()
    if desktop.exists() or desktop.parent.exists():
        found.append("claude-desktop")
    if pi_config().exists() or (home() / ".pi" / "agent").exists():
        found.append("pi")
    return found


def resolve_clients(arg: str) -> list[str]:
    known = ["codex", "cursor", "claude", "claude-desktop", "pi"]
    if arg in ("auto", ""):
        return detect_clients()
    if arg == "all":
        return known
    picked = [c.strip() for c in arg.split(",") if c.strip()]
    bad = [c for c in picked if c not in known]
    if bad:
        raise SystemExit(f"unknown client(s): {', '.join(bad)}")
    return picked


def is_lifeup_table(name: str, dist_path: str) -> bool:
    n = name.strip().strip('"').strip("'")
    if n == "mcp_servers.lifeup" or n.startswith("mcp_servers.lifeup."):
        return True
    norm = n.replace("\\", "/").lower()
    if norm == dist_path.replace("\\", "/").lower():
        return True
    return norm.endswith("/mcp/dist/index.js")

def upsert_codex(path: Path, command: str, dist_path: str, *, write: bool) -> str:
    text = path.read_text() if path.exists() else ""
    env_lines: list[str] = []
    out: list[str] = []
    skipping = False
    in_env = False
    existed = False
    for line in text.splitlines():
        m = TABLE_HEAD.match(line.strip())
        if m:
            name = m.group(1)
            if is_lifeup_table(name, dist_path):
                existed = True
                skipping = True
                in_env = name.strip().strip('"') == "mcp_servers.lifeup.env"
                continue
            skipping = False
            in_env = False
        if skipping:
            stripped = line.strip()
            if in_env and stripped and not stripped.startswith("#"):
                env_lines.append(line.rstrip())
            continue
        out.append(line.rstrip())
    while out and out[-1] == "":
        out.pop()
    block = [
        "",
        "[mcp_servers.lifeup]",
        f'command = "{command}"',
        f'args = [ "{dist_path}" ]',
    ]
    if env_lines:
        block += ["", "[mcp_servers.lifeup.env]", *env_lines]
    block.append("")
    new_text = ("\n".join(out) + "\n" + "\n".join(block)).lstrip("\n")
    action = "updated" if existed else "added"
    if write:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(new_text)
    return f"{action} {path}"


def load_json(path: Path) -> dict:
    if not path.exists():
        return {}
    raw = path.read_text().strip()
    if not raw:
        return {}
    data = json.loads(raw)
    if not isinstance(data, dict):
        raise SystemExit(f"{path} is not a JSON object")
    return data


def dump_json(path: Path, data: dict, *, write: bool) -> None:
    if write:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n")


def alias_keys(servers: dict) -> list[str]:
    return [k for k in list(servers) if str(k).lower() in ALIASES]


def upsert_json(path: Path, command: str, dist_path: str, *, write: bool, create: bool) -> str:
    created = not path.exists()
    if created and not create:
        return f"skipped {path} (missing)"
    data = load_json(path)
    servers = data.get("mcpServers")
    if not isinstance(servers, dict):
        servers = {}
        data["mcpServers"] = servers
    found = alias_keys(servers)
    merged: dict = {}
    for key in found:
        value = servers.pop(key)
        if isinstance(value, dict):
            merged.update(value)
    merged["command"] = command
    merged["args"] = [dist_path]
    servers["lifeup"] = merged
    dump_json(path, data, write=write)
    if created:
        return f"created {path}"
    if not found:
        return f"added lifeup in {path}"
    if len(found) > 1 or found[0] != "lifeup":
        return f"updated {path} (merged {', '.join(found)} \u2192 lifeup)"
    return f"updated {path}"


def upsert_pi(path: Path, command: str, dist_path: str, *, write: bool, inherit_codex: bool) -> str:
    if not path.exists():
        if inherit_codex:
            return f"skipped {path} (missing; would inherit Codex)"
        return upsert_json(path, command, dist_path, write=write, create=True)
    data = load_json(path)
    imports = [str(x).lower() for x in (data.get("imports") or [])]
    servers = data.get("mcpServers")
    if not isinstance(servers, dict):
        servers = {}
        data["mcpServers"] = servers
    found = alias_keys(servers)
    if inherit_codex and "codex" in imports:
        if not found:
            return f"skipped {path} (imports Codex)"
        for key in found:
            servers.pop(key)
        dump_json(path, data, write=write)
        return f"removed duplicate {path} (inherits Codex)"
    return upsert_json(path, command, dist_path, write=write, create=False)


def run(command: str, dist_path: str, clients: list[str], *, write: bool) -> list[str]:
    reports = []
    inherit_codex = "codex" in clients
    for client in clients:
        if client == "codex":
            reports.append(upsert_codex(codex_config(), command, dist_path, write=write))
        elif client == "cursor":
            reports.append(upsert_json(cursor_config(), command, dist_path, write=write, create=(home() / ".cursor").exists()))
        elif client == "claude":
            reports.append(upsert_json(claude_config(), command, dist_path, write=write, create=False))
        elif client == "claude-desktop":
            path = claude_desktop_config()
            reports.append(upsert_json(path, command, dist_path, write=write, create=path.parent.exists()))
        elif client == "pi":
            reports.append(upsert_pi(pi_config(), command, dist_path, write=write, inherit_codex=inherit_codex))
        else:
            raise SystemExit(f"unknown client: {client}")
    return reports


def self_check() -> None:
    td = Path(tempfile.mkdtemp(prefix="lifeup-mcp-install-"))
    os.environ["CODEX_HOME"] = str(td / "codex")
    os.environ["PI_CODING_AGENT_DIR"] = str(td / "pi")
    global home

    def fake_home() -> Path:
        return td / "home"

    globals()["home"] = fake_home
    (fake_home() / ".cursor").mkdir(parents=True)
    (fake_home() / ".codex").mkdir(parents=True)
    (td / "codex").mkdir(parents=True)
    (td / "pi").mkdir(parents=True)

    command = "/usr/bin/node"
    dist_path = "/tmp/LifeUp-SDK/mcp/dist/index.js"

    toml = td / "codex" / "config.toml"
    toml.write_text(
        '[mcp_servers.lifeup]\n'
        'command = "/old/node"\n'
        'args = [ "/old/dist/index.js" ]\n'
        '\n'
        '[ "/old/LifeUp-SDK/mcp/dist/index.js" ]\n'
        '\n'
        '[mcp_servers.other]\n'
        'command = "x"\n'
    )
    upsert_codex(toml, command, dist_path, write=True)
    upsert_codex(toml, command, dist_path, write=True)
    text = toml.read_text()
    assert text.count("[mcp_servers.lifeup]") == 1, text
    assert 'args = [ "/tmp/LifeUp-SDK/mcp/dist/index.js" ]' in text, text
    assert '[ "/old/LifeUp-SDK/mcp/dist/index.js" ]' not in text, text
    assert "[mcp_servers.other]" in text, text

    js = fake_home() / ".cursor" / "mcp.json"
    js.write_text('{"mcpServers":{"lifeup-cloud":{"command":"npx","env":{"A":"1"}}}}')
    msg = upsert_json(js, command, dist_path, write=True, create=True)
    assert "merged" in msg, msg
    data = json.loads(js.read_text())
    assert list(data["mcpServers"]) == ["lifeup"]
    assert data["mcpServers"]["lifeup"]["env"] == {"A": "1"}
    upsert_json(js, command, dist_path, write=True, create=True)
    assert list(json.loads(js.read_text())["mcpServers"]) == ["lifeup"]

    pi = td / "pi" / "mcp.json"
    pi.write_text('{"imports":["codex"],"mcpServers":{"lifeup":{"command":"old"},"fastctx":{"disabled":true}}}')
    msg = upsert_pi(pi, command, dist_path, write=True, inherit_codex=True)
    assert "removed duplicate" in msg, msg
    data = json.loads(pi.read_text())
    assert "lifeup" not in data["mcpServers"]
    assert "fastctx" in data["mcpServers"]
    print("self-test ok")


if self_test:
    self_check()
    sys.exit(0)

clients = resolve_clients(clients_arg)
if not clients:
    print("no MCP clients detected; pass --clients=codex,cursor,...")
    sys.exit(1)
if dry_run:
    print("dry-run")
for line in run(node, dist, clients, write=not dry_run):
    print(line)
PY

if [[ "$SELF_TEST" != 1 && "$DRY_RUN" != 1 ]]; then
  info "done. Restart the client (or /reload) to pick up LifeUp MCP."
fi
