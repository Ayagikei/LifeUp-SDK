# @lifeup/mcp

MCP server for [LifeUp Cloud](https://github.com/Ayagikei/LifeUp-SDK). One package: tools + bundled skills.

Phone must run LifeUp and LifeUp Cloud on the same LAN. Grant **Read LifeUp Data** in Cloud.

## Install

```bash
cd mcp
npm install
npm run build
```

Cursor / Claude Desktop / Codex:

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

## Skills are inside the MCP

You do **not** copy a skill folder for Cursor/Claude Desktop.

- Server `description` tells the model how to connect
- `help` with no topic = workflow
- `help` `api-index` lists methods; `help` `add_task` (any method) returns that wiki table

Claude Code / Pi can additionally point at `mcp/skills/lifeup-cloud` if they want a disk skill. Same files, not a second source of truth.

## First tools

`discover` (auto-connects if one Cloud) → `list_tasks` / `complete_task` / `reward` / `call_api`.
