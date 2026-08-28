# Discovery

Cloud advertises NSD/mDNS:

- name contains `lifeup_cloud`
- type `_lifeup._tcp`
- TXT `port` is the HTTP port (SRV now advertises the same port; TXT `ipv4` is a hint)
- default HTTP port `13276`

Connect order:

1. `host` argument (`host:port` or URL)
2. `LIFEUP_HOST`
3. `~/.lifeup-mcp.json` host (mode `0600`)
4. mDNS — one result auto-connects; many results must be chosen. Empty is normal on corporate Wi-Fi: multicast `_lifeup._tcp` often does not cross APs/VLANs. Read the IP:port from the Cloud app and `connect` `{ host }`.

Token:

- Cloud optional setting, sent as `Authorization: <raw token>` (not `Bearer`)
- `LIFEUP_TOKEN` is process-only and never written to disk
- persisted token is bound to `{host,port}` and is not sent to a newly discovered different host
- persist a token only when `connect` received `token` and the `/info` probe succeeded
- HTTP 401 → ask for token
- envelope `code=10001` → LifeUp app is not running
