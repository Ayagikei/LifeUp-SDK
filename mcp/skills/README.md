# LifeUp skills

Source of truth: [`lifeup-cloud/`](lifeup-cloud/).

The MCP server loads these files for `help`. Cursor / Claude Desktop do not need a separate skill install.

Navigation: **`help` `query`** (MCP listing) · **`help` `query-index`** / **`help` `query/<anchor>`** (HTTP JSON models, generated from LifeUp Wiki) · **`help` `api-index`** / method names (actions).

To customize (Claude Code, Pi, or your own agent):

```text
copy mcp/skills/lifeup-cloud  →  <your-agent>/skills/lifeup-cloud
```

Edit `SKILL.md` and `references/` there. Do not publish a second package.
