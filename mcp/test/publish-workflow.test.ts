import assert from "node:assert/strict"
import { execFileSync, spawnSync } from "node:child_process"
import { existsSync, mkdirSync, mkdtempSync, readFileSync, realpathSync, rmSync, writeFileSync } from "node:fs"
import { tmpdir } from "node:os"
import { join } from "node:path"
import { test, type TestContext } from "node:test"

const workflow = readFileSync(new URL("../../.github/workflows/publish.yml", import.meta.url), "utf8")
const npm = realpathSync(execFileSync("which", ["npm"], { encoding: "utf8" }).trim())

// Execute the workflow's actual shell blocks; only registry access and remote Git operations are stubbed.
function stepScript(name: string): string {
  const step = workflow.split(`      - name: ${name}\n`)[1]?.split(/\n      - /)[0]
  const script = step?.split("        run: |\n")[1]
  assert.ok(script, `Missing shell block: ${name}`)
  return script.split("\n").map((line) => line.replace(/^          /, "")).join("\n")
}

function fixture(t: TestContext, version = "0.1.4") {
  const root = mkdtempSync(join(tmpdir(), "lifeup-mcp-publish-"))
  t.after(() => rmSync(root, { recursive: true, force: true }))
  const bin = join(root, "bin")
  const cwd = join(root, "mcp")
  mkdirSync(bin)
  mkdirSync(cwd)
  writeFileSync(join(cwd, "package.json"), JSON.stringify({ name: "@lifeup/mcp", version }))
  writeFileSync(join(cwd, "package-lock.json"), JSON.stringify({
    name: "@lifeup/mcp", version, lockfileVersion: 3,
    packages: { "": { name: "@lifeup/mcp", version } },
  }))
  writeFileSync(join(bin, "npm"), `#!/bin/bash
if [ "$1" = view ]; then
  if [ "$REGISTRY_FAILURE" = 1 ]; then exit 42; fi
  printf '%s\\n' "$REGISTRY_VERSION"
else
  exec '${npm.replace(/'/g, "'\\''")}' "$@" --ignore-scripts --offline
fi
`, { mode: 0o755 })
  const env = {
    ...process.env,
    PATH: `${bin}:${process.env.PATH}`,
    GITHUB_OUTPUT: join(root, "output"),
    REGISTRY_VERSION: "0.1.4",
    npm_config_cache: join(root, "npm-cache"),
    npm_config_update_notifier: "false",
  }
  return { root, bin, cwd, env }
}

for (const { local, latest, expected } of [
  { local: "0.1.4", latest: "0.1.4", expected: "0.1.5" },
  { local: "0.1.5", latest: "0.1.4", expected: "0.1.5" },
  { local: "0.1.4", latest: "0.1.9", expected: "0.1.10" },
]) {
  test(`published ${latest} with local ${local} resolves to ${expected}`, (t) => {
    const f = fixture(t, local)
    const result = spawnSync("bash", ["-e", "-o", "pipefail", "-c", stepScript("Resolve next patch version")], {
      cwd: f.cwd, env: { ...f.env, REGISTRY_VERSION: latest }, encoding: "utf8",
    })
    assert.equal(result.status, 0, result.stderr)
    const pkg = JSON.parse(readFileSync(join(f.cwd, "package.json"), "utf8"))
    const lock = JSON.parse(readFileSync(join(f.cwd, "package-lock.json"), "utf8"))
    assert.equal(pkg.version, expected)
    assert.equal(lock.version, expected)
    assert.equal(lock.packages[""].version, expected)
    assert.equal(readFileSync(f.env.GITHUB_OUTPUT, "utf8"), `target=${expected}\n`)
  })
}

for (const registry of [
  { REGISTRY_FAILURE: "1", REGISTRY_VERSION: "0.1.4" },
  { REGISTRY_FAILURE: "0", REGISTRY_VERSION: "not-a-version" },
]) {
  test(`registry failure or invalid version stops before version output (${JSON.stringify(registry)})`, (t) => {
    const f = fixture(t)
    const result = spawnSync("bash", ["-e", "-o", "pipefail", "-c", stepScript("Resolve next patch version")], {
      cwd: f.cwd, env: { ...f.env, ...registry }, encoding: "utf8",
    })
    assert.notEqual(result.status, 0)
    assert.equal(JSON.parse(readFileSync(join(f.cwd, "package.json"), "utf8")).version, "0.1.4")
    assert.equal(existsSync(f.env.GITHUB_OUTPUT), false)
  })
}

for (const scenario of [
  { name: "already committed version is a no-op", diff: "0", pull: "0", pushes: false, succeeds: true },
  { name: "version changes are rebased and normally pushed", diff: "1", pull: "0", pushes: true, succeeds: true },
  { name: "rebase conflict stops without pushing", diff: "1", pull: "1", pushes: false, succeeds: false },
]) {
  test(scenario.name, (t) => {
    const f = fixture(t)
    const log = join(f.root, "git.log")
    writeFileSync(join(f.bin, "git"), `#!/bin/bash
printf '%s\\n' "$*" >> "$GIT_LOG"
case "$1" in
  diff) exit "$DIFF_STATUS" ;;
  pull) exit "$PULL_STATUS" ;;
esac
`, { mode: 0o755 })
    const result = spawnSync("bash", ["-e", "-o", "pipefail", "-c", stepScript("Commit version bump back")], {
      cwd: f.root,
      env: { ...f.env, TARGET: "0.1.5", GIT_LOG: log, DIFF_STATUS: scenario.diff, PULL_STATUS: scenario.pull },
      encoding: "utf8",
    })
    assert.equal(result.status === 0, scenario.succeeds, result.stderr)
    const calls = readFileSync(log, "utf8").trim().split("\n")
    assert.ok(calls.includes("add mcp/package.json mcp/package-lock.json"))
    assert.equal(calls.some((line) => line.startsWith("push ")), scenario.pushes)
    if (scenario.diff === "0") {
      assert.equal(calls.some((line) => line.startsWith("commit ")), false)
    } else {
      assert.ok(calls.includes("commit -m chore(mcp): bump version to 0.1.5 [skip ci]"))
      assert.ok(calls.includes("pull --rebase origin main"))
    }
    if (scenario.pushes) assert.equal(calls.at(-1), "push origin HEAD:main")
  })
}

for (const checkout of ["trigger-sha", "different-sha"]) {
  test(`provenance source check accepts only the triggering SHA (${checkout})`, (t) => {
    const f = fixture(t)
    writeFileSync(join(f.bin, "git"), `#!/bin/bash
printf '%s\\n' "$CHECKOUT_SHA"
`, { mode: 0o755 })
    const result = spawnSync("bash", ["-e", "-o", "pipefail", "-c", stepScript("Verify source revision")], {
      cwd: f.root,
      env: { ...f.env, GITHUB_SHA: "trigger-sha", CHECKOUT_SHA: checkout },
      encoding: "utf8",
    })
    assert.equal(result.status === 0, checkout === "trigger-sha", result.stderr)
  })
}
