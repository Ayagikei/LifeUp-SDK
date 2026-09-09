<p align="center">
 <img src="https://github.com/Ayagikei/LifeUp-SDK/raw/main/imgs/01.jpg" style="height:600px" />
 <img src="https://github.com/Ayagikei/LifeUp-SDK/raw/main/imgs/02.jpg" style="height:600px" />
</p>
<h2 align="center" padding="100">LifeUp Cloud (SDKs)</h2>

<p align="center">LifeUp SDK, LAN HTTP bridge, MCP server, and bundled agent skills.</p>

### Installation

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=net.lifeupapp.lifeup.http">
    <img src="https://img.shields.io/static/v1?labelColor=56595b&color=97db99&logo=google-play&logoColor=ffffff&label=google play&style=for-the-badge&message=get"/>
  </a>

  <a href="https://github.com/Ayagikei/LifeUp-SDK/releases">
    <img src="https://img.shields.io/static/v1?labelColor=56595b&color=a6c6ff&logo=github&logoColor=ffffff&label=Github%20Release&style=for-the-badge&message=get"/>
  </a>
</p>

<br/>

### What's it?

**LifeUp Cloud** turns your phone into a **LAN HTTP bridge** for
**[LifeUp: Gamify To-Do & Habit](https://play.google.com/store/apps/details?id=net.sarasarasa.lifeup)**.
Call `lifeup://api/…` from a computer, script, desktop app, or AI agent — complete tasks, query data, reward coins, buy items, and more.

This repo contains:

| Module | What it is |
| --- | --- |
| `http/` | The **LifeUp Cloud** Android app (Ktor/Netty HTTP server) |
| `core/` | Shared SDK: URL-scheme helpers, ContentProvider clients, models |
| `mcp/` | **`@lifeup/mcp`** — MCP server + bundled **`lifeup-cloud`** skills |
| `desktop/` | Pointer to [LifeUp Desktop](https://github.com/Ayagikei/LifeUp-Desktop) |

Requires **LifeUp 1.106.0+** and **LifeUp Cloud 3.0.0+** for the full feature set (journals, statistics, level curve, events).

### Use cases

| Scenario | For | In one line |
| --- | --- | --- |
| **HTTP + scripts** | Developers, automation | Python, web pages, or shell scripts on your LAN — no Android dev needed |
| **LifeUp Desktop** | Desktop users | Query tasks, shop, history; mutate via `/api/contentprovider` |
| **AI agents (MCP)** | Cursor, Claude, Pi, etc. | One sentence to plan tasks, analyze stats, or build a full RPG theme |
| **QR scanning** | Physical check-ins | Print QR codes for tasks, pomodoro, rewards, or external links |
| **Start / stop shortcuts** | Tasker, NFC, automation | `lifeupcloud://start` / `lifeupcloud://stop` without opening the switch |

**Example (AI):** *"Clear the sample tasks and shop items, then build a complete indie-game-developer theme: task lists, attributes, shop, and achievements."*

### MCP & Skills

Install **`@lifeup/mcp`** once — tools and docs ship together. No separate skill package.

```json
{
  "mcpServers": {
    "lifeup": {
      "command": "npx",
      "args": ["-y", "@lifeup/mcp"]
    }
  }
}
```

From a local clone: `./scripts/install-mcp.sh` (builds MCP and upserts detected clients).

**Flow:** `discover` → `list_tasks` / `list_data` → `complete_task` / `add_task` / `reward` / `call_api`.

- **Cursor / Claude Desktop:** MCP only — `help` reads bundled skills; do not install a second copy.
- **Claude Code / Pi / custom agents:** copy [`mcp/skills/lifeup-cloud/`](mcp/skills/lifeup-cloud/) or point your skill path at it.

Details: [`mcp/README.md`](mcp/README.md) · [Wiki: MCP & Skills (EN)](https://docs.lifeupapp.fun/en/#/guide/api_mcp) · [Wiki (ZH)](https://docs.lifeupapp.fun/zh-cn/guide/api_mcp.md)

### Data & events

**Read lists** over HTTP: tasks, history, shop, skills, achievements, feelings, pomodoro records, coin/exp/inventory journals, statistics, step history, and more. Grant **Read LifeUp Data** in LifeUp Cloud first.

**Write / mutate** via `/api/contentprovider` (recommended) or `/api` (UI launch when needed).

**Events:** enable *Experimental → Broadcast events* in LifeUp. Cloud exposes `GET /events` and `WS /events` for task completion, rewards, and other app broadcasts.

<p align="center">
 <img src="https://github.com/Ayagikei/LifeUp-Desktop/raw/master/imgs/cloud.png"/>
</p>

### Documentation

| Topic | Link |
| --- | --- |
| LifeUp URL APIs | [docs.lifeupapp.fun — api](https://docs.lifeupapp.fun/en/#/guide/api) |
| LifeUp Cloud HTTP | [docs.lifeupapp.fun — api_cloud](https://docs.lifeupapp.fun/en/#/guide/api_cloud) |
| MCP & Skills | [docs.lifeupapp.fun — api_mcp](https://docs.lifeupapp.fun/en/#/guide/api_mcp) |
| SDK entry (`LifeUpApiDef`) | [core/.../LifeUpApiDef.kt](core/src/main/java/net/lifeupapp/lifeup/api/LifeUpApiDef.kt) |
| HTTP routes (source of truth) | [http/.../KtorService.kt](http/src/main/java/net/lifeupapp/lifeup/http/service/KtorService.kt) |
| MCP package | [mcp/README.md](mcp/README.md) |
| Agent notes | [AGENTS.md](AGENTS.md) |

Wiki pages may lag; **`KtorService.kt`** and **`mcp/skills/lifeup-cloud/`** are the live contracts.

<br/>

### Contribution

Pull requests welcome. Use [Conventional Commits](https://www.conventionalcommits.org/) with an emoji after the scope (see recent history). No special process beyond that.
