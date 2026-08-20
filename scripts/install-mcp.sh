#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MCP_DIR="$ROOT_DIR/mcp"
DIST="$MCP_DIR/dist/index.js"
CODEX_CONFIG="${CODEX_HOME:-$HOME/.codex}/config.toml"

info() { printf '==> %s\n' "$*"; }
die() { printf 'error: %s\n' "$*" >&2; exit 1; }

NODE="$(command -v node)" || die "node not found"
[[ -x "$NODE" ]] || die "node not executable: $NODE"

info "building @lifeup/mcp"
(
  cd "$MCP_DIR"
  npm install
  npm run build
)
[[ -f "$DIST" ]] || die "build did not produce $DIST"

python3 - "$CODEX_CONFIG" "$NODE" "$DIST" <<'PY'
import pathlib, re, sys

config_path, node, dist = map(pathlib.Path, sys.argv[1:])
block = (
    "[mcp_servers.lifeup]\n"
    f'command = "{node}"\n'
    f'args = [ "{dist}" ]\n'
)
text = config_path.read_text() if config_path.exists() else ""
pattern = re.compile(r"\[mcp_servers\.lifeup\][^\[]*", re.M)
if pattern.search(text):
    text = pattern.sub(block + "\n", text, count=1)
    action = "updated"
else:
    if text and not text.endswith("\n"):
        text += "\n"
    text += "\n" + block
    action = "added"
config_path.parent.mkdir(parents=True, exist_ok=True)
config_path.write_text(text)
print(f"{action} {config_path}: {node} {dist}")
PY

info "done. Restart Codex (or /reload) to pick up the LifeUp MCP."
