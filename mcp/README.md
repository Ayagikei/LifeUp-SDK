# @lifeup/mcp

MCP server for [LifeUp Cloud](https://github.com/Ayagikei/LifeUp-SDK). One package: tools + bundled skills.

Phone must run LifeUp and LifeUp Cloud on the same LAN. Grant **Read LifeUp Data** in Cloud.

## Install

**npm (recommended):**

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

If the default npm registry is slow or unreachable, add `--registry=https://registry.npmmirror.com` to the `npx` command line.

**GitHub** (tracks repo `main`, builds via `prepare`):

```json
{
  "mcpServers": {
    "lifeup": {
      "command": "npx",
      "args": ["-y", "github:Ayagikei/LifeUp-SDK"]
    }
  }
}
```

`npx` clones the repo and builds `mcp/`.

From a local clone, prefer the installer (builds MCP and upserts detected clients; re-runs do not register a second copy):

```bash
./scripts/install-mcp.sh
```

Default clients: Codex, Cursor, Claude Code, Claude Desktop, Pi. Pi configs that `imports: ["codex"]` inherit Codex and are not duplicated.

macOS GUI clients often lack `npx` on `PATH` — use an absolute `npx`/`node`, or:

```bash
git clone https://github.com/Ayagikei/LifeUp-SDK.git
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
- `LIFEUP_TIMEOUT_MS` — override the default **30000** ms HTTP timeout for each Cloud request (headers and body share one budget; it does not reset partway through). Must be a **positive integer** milliseconds value from **1** through **120000** inclusive. Any other value makes the MCP process exit at startup. Saved `connect` host, port, and token are **not** cleared or altered by this setting.

If Cloud set an API token, pass it to `connect` or `LIFEUP_TOKEN`. Header is the raw token, not `Bearer`.

## Troubleshooting (client ↔ Cloud)

These notes describe the MCP client contract in this repository. Cloud support for `X-LifeUp-Concurrency: limited` and `withSubTasks=false` arrives with the Cloud app; older Clouds ignore both.

- **Timeouts:** Errors name the HTTP method, path and configured waiting budget. Headers and the body share that budget; query strings are omitted.
- **Command or launch timeouts:** The result is unknown: Cloud or LifeUp may still be working. Check state before repeating a mutation. MCP does not automatically retry, reconnect or resend commands.
- **`/info` vs open port:** The root page or an open TCP port does not prove Provider access works. A fresh `GET /info` reads versions without taking the Scheme mutex, but can still wait on LifeUp startup or Binder and does not make later business calls immune to those delays. MCP `status` only reports last-known connection information.
- **Concurrency:** Every Cloud HTTP request sends `X-LifeUp-Concurrency: limited`. Clouds that understand it leave light reads unqueued, queue heavy reads (max 4) and commands (max 2), and stop waiting after 20s. A queue timeout is HTTP **503** with body `code` **10003**: the request was **not** started. Back off and retry later; do not hammer. MCP does not retry. Older Clouds ignore the header. Provider work still runs on **`Dispatchers.IO`**, and URLs inside one `call_api_batch` still run **sequentially**. A client timeout or disconnect does not prove a write was cancelled. Any other **503** is not this busy signal.
- **List limits:** List tools use `limit` 1–100, default 20. A request outside that range is clamped, and the envelope then adds `limitCapped: true` and `requestedLimit`. Compact task lists send `withSubTasks=false` (`/tasks`, `/tasks/{id}`, `/smart_lists/{type}/tasks`) so Cloud can skip subtask lookups. `detail=true` omits that parameter and returns full objects.

## Skills (in this repo)

Canonical files: [`mcp/skills/lifeup-cloud/`](skills/lifeup-cloud/). Same copy is bundled into the MCP (`help`).

- **Cursor / Claude Desktop / WorkBuddy:** install the MCP only. Do not install a second skill.
- **Claude Code / Pi / your own agent:** point the skill path at `mcp/skills/lifeup-cloud`, or copy that folder and edit it.

There is no separate npm skill package. Fork the folder if you want a custom workflow.

## First tools

`discover` (auto-connects if one Cloud) → `list_tasks` / `complete_task` / `reward` / `call_api`.

## Help topics

- **`help` `query`** — compact MCP list/read usage (`list_data`, paging, compact vs `detail=true`).
- **`help` `query-index`** → **`help` `query/<anchor>`** — generated HTTP query reference (wiki `api-reference.md` via `mcp/scripts/extract-wiki-queries.py`).
- **`help` `api-index`** → **`help` `<method>`** — URL Scheme / action parameters (unchanged).
- **`get_diagnostics`** / **`GET /diagnostics`** — safe Cloud process snapshot without a successful `connect` or `/info`; optional `{ host, token }` only for this call. See bundled skill [`skills/lifeup-cloud/references/diagnostics.md`](skills/lifeup-cloud/references/diagnostics.md). Use **`help`** topic **`diagnostics`** (`{ "topic": "diagnostics" }`) alongside that file.

## Releases

npm publishing for `@lifeup/mcp` is automated by [`.github/workflows/publish.yml`](../.github/workflows/publish.yml). LifeUp Cloud versioning and release cadence stay independent of this package.

**When it runs**

- **Push to `main`** that touches `mcp/**` triggers a publish job. Publishing runs serially.
- **Manual:** GitHub → **Actions** → **Publish @lifeup/mcp** → **Run workflow**, branch **`main`**. There is no version input; the workflow computes the target version.

**Version rule**

The job reads **`@lifeup/mcp@latest` from the npm registry**, rather than using the repo version as its baseline, and increments the patch number. After publishing, it commits `mcp/package.json` and `mcp/package-lock.json` back to **`main`** if they changed, with `[skip ci]` in the commit message. It does **not** watch, create, or push git tags.

**Gates and auth**

- Checkout is pinned to the triggering commit, and its SHA is verified before publishing so npm provenance matches the source.
- Steps: `npm ci`, `npm run typecheck`, then `npm publish`, whose **`prepublishOnly`** hook runs tests and build before upload.
- Publishing uses **npm trusted publishing (OIDC)** and `npm publish --provenance`.

**After merge**

Once MCP changes land on `main`, CI publishes the next patch if checks pass. A follow-up version-sync commit is only needed when the manifests changed; the bot's `GITHUB_TOKEN` push and `[skip ci]` prevent a recursive publish.
