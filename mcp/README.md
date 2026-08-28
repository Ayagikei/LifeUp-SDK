# @lifeup/mcp

MCP server for [LifeUp Cloud](https://github.com/Ayagikei/LifeUp-SDK). One package: tools + bundled skills.

Phone must run LifeUp and LifeUp Cloud on the same LAN. Grant **Read LifeUp Data** in Cloud.

## Install

Until npm publish:

```json
{
  "mcpServers": {
    "lifeup": {
      "command": "npx",
      "args": ["-y", "github:Ayagikei/LifeUp-SDK#feat/mcp"]
    }
  }
}
```

`npx` clones the repo and `prepare` builds `mcp/`. After `feat/mcp` lands on `main`, drop `#feat/mcp`.

From a local clone, prefer the installer (builds MCP and upserts detected clients; re-runs do not register a second copy):

```bash
./scripts/install-mcp.sh
```

Default clients: Codex, Cursor, Claude Code, Claude Desktop, Pi. Pi configs that `imports: ["codex"]` inherit Codex and are not duplicated.

macOS GUI clients often lack `npx` on `PATH` — use an absolute `npx`/`node`, or:

```bash
git clone -b feat/mcp https://github.com/Ayagikei/LifeUp-SDK.git
cd LifeUp-SDK/mcp && npm install && npm run build
```

```json
{
  "mcpServers": {
    "lifeup": {
      "command": "node",
      "args": ["/ABS/PATH/LifeUp-SDK/mcp/dist/index.js"]
    }
  }
}
```

Optional env:

- `LIFEUP_HOST=192.168.1.8:13276`
- `LIFEUP_TOKEN=...` (raw Authorization value, never persisted)
- `LIFEUP_MCP_CONFIG=/path/to/config.json`

If Cloud set an API token, pass it to `connect` or `LIFEUP_TOKEN`. Header is the raw token, not `Bearer`.

## Skills (in this repo)

Canonical files: [`mcp/skills/lifeup-cloud/`](skills/lifeup-cloud/). Same copy is bundled into the MCP (`help`).

- **Cursor / Claude Desktop / WorkBuddy:** install the MCP only. Do not install a second skill.
- **Claude Code / Pi / your own agent:** point the skill path at `mcp/skills/lifeup-cloud`, or copy that folder and edit it.

There is no separate npm skill package. Fork the folder if you want a custom workflow.

## First tools

`discover` (auto-connects if one Cloud) → `list_tasks` / `complete_task` / `reward` / `call_api`.
