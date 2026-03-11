# AGENTS.md

> Use English for agent-facing notes and commit messages in this repository.

## Repository Overview

`LifeUp-SDK` contains the public integration surface for LifeUp and the Android HTTP bridge that exposes those APIs over LAN.

### Modules
- `core/`: Main SDK layer. Contains URL-scheme helpers, ContentProvider APIs, serializable models, and parsing logic.
- `http/`: Android app module built on Ktor/Netty. Exposes the SDK through HTTP routes such as `/tasks`, `/history`, `/items`, and `/api`.
- `desktop/`: Desktop-related assets / experiments. Do not touch unless the task is explicitly desktop-scoped.
- `scripts/`: Small project utilities.
- `docs/`: Local notes. `docs/plans/` is ignored and is not part of normal commits.

## Current Task/Data Contract Notes

These are easy to break and should be preserved unless the user explicitly asks for a contract change.

- `TasksApi.listTasks()` parses both:
  - `countProgress`
  - `repeatEndCondition`
- `TasksApi.listHistory()` parses:
  - `countProgress`
  - but **must not** expose `repeatEndCondition`
- Task parsing is intentionally centralized in `core/src/main/java/net/lifeupapp/lifeup/api/content/tasks/TasksApi.kt`.
- Provider-backed JSON fields can be malformed in real environments; prefer tolerant parsing for optional fields instead of failing the whole query.

## Working Rules

- Keep changes small and focused.
- Prefer the canonical path over adapters or duplicate parsing branches.
- When adding fields to SDK models, update both parsing logic and tests in the same change.
- Preserve `@Serializable` on public transport/data models when they are already serialized by SDK or HTTP code.
- Avoid introducing breaking API shape changes unless the user explicitly approves them.

## Testing

Run the narrowest relevant checks first.

### Recommended commands
- `bash ./gradlew :core:testDebugUnitTest --no-daemon`
- `bash ./gradlew :core:compileDebugAndroidTestKotlin :http:compileDebugKotlin --no-daemon`

### When touching HTTP routes
Also inspect:
- `http/src/main/java/net/lifeupapp/lifeup/http/service/KtorService.kt`
- route/query parameter compatibility
- response shape consistency with `HttpResponse<T>`

## File-Level Guidance

### `core/src/main/java/net/lifeupapp/lifeup/api/content/tasks/*`
- Keep task parsing semantics explicit.
- Distinguish current-cycle progress from repeat-end semantics.
- Do not silently merge unrelated concepts into one field.

### `core/src/test/java/net/lifeupapp/lifeup/api/content/tasks/*`
- Add or update focused parsing tests whenever task/provider fields change.
- Cover malformed optional payloads when parser tolerance is part of the contract.

## Commit Style

Follow the recent repository convention:
- Conventional Commit format
- English subject line
- Emoji after the scope, for example:
  - `feat(tasks): ✨ expose count progress and repeat end condition`
  - `docs(repo): 📝 add agent guidance`
  - `fix(http): 🐛 handle malformed payloads safely`

## Git Hygiene

- Stage only files that belong to the current task.
- Do not include local IDE files, build outputs, or `docs/plans/` in normal commits.
- If the repository already has unrelated staged changes, commit your scoped files explicitly.
