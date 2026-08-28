# QR code scanning (LifeUp Cloud app)

LifeUp Cloud on the phone has a **Scan** button (top-right on the home screen; **?** opens a short in-app guide). It reads a QR-encoded URL and opens it via Android Intent — **no MCP HTTP call required**.

This is a **phone UI feature**, not an MCP tool. Agents cannot scan QR codes remotely; help the user design scannable URLs or printable codes instead.

## Supported URL forms

| URL | Behavior |
| --- | --- |
| `lifeup://api/…` | Runs a LifeUp API (complete task, pomodoro, reward, shop dialogs, …). Same as tapping a lifeup link in notes. |
| `http(s)://…` | Opens in the system browser (or another handler). |
| Other app schemes (`weixin://`, `intent://`, …) | Opens the target app if installed. LifeUp Cloud does **not** maintain third-party scheme lists. |

Scanning is **not limited to LifeUp APIs** — any URL/scheme the phone can handle works.

## Typical agent solutions (scan-related)

**Physical check-in:** Print QR codes for `lifeup://api/complete?name=…` or pomodoro URLs; user scans with LifeUp Cloud at gym/desk/workstation.

**Open a web page:** Encode `https://…` in a QR code; scan opens the browser.

**Other apps:** Encode `weixin://` or other installed-app schemes.

**Hybrid sticker:** QR encodes `https://…` for a page that redirects to `lifeup://api/…`.

## Not QR scanning

**Shop item link effects (type 9)** — user taps **Use** on an item; no QR involved. See `help` `item_structures` § Item Effects.

**External web ↔ LifeUp demos** (e.g. wiki Wordle page calling `lifeup://api/reward` on win) — browser / item-use flow, not Cloud Scan. See wiki community contributions / Wordle example.

## User-facing docs

Wiki LifeUp Cloud page: QR section + scenario table. In-app: Scan + **?** help dialog.

When users ask about QR / NFC-style check-in / “scan to complete task”, point them to LifeUp Cloud Scan and suggest concrete scannable URLs.
