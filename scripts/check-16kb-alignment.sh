#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET_DIR="$ROOT_DIR/http/build/intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib/arm64-v8a"

if ! command -v objdump >/dev/null 2>&1; then
  echo "objdump is required to validate ELF alignment." >&2
  exit 1
fi

if [[ ! -d "$TARGET_DIR" ]]; then
  echo "Missing native library directory: $TARGET_DIR" >&2
  echo "Run './gradlew :http:assembleRelease' before executing this check." >&2
  exit 1
fi

shopt -s nullglob
native_libs=("$TARGET_DIR"/*.so)
if [[ ${#native_libs[@]} -eq 0 ]]; then
  echo "No arm64 native libraries found under $TARGET_DIR" >&2
  exit 1
fi

failed=0
for native_lib in "${native_libs[@]}"; do
  while IFS= read -r alignment_bits; do
    if (( alignment_bits < 14 )); then
      echo "FAIL: $native_lib uses LOAD align 2**$alignment_bits (< 2**14)." >&2
      failed=1
    fi
  done < <(objdump -p "$native_lib" | awk '
    /^[[:space:]]*LOAD[[:space:]]/ {
      getline
      if (match($0, /align 2\*\*[0-9]+/)) {
        print substr($0, RSTART + 10, RLENGTH - 10)
      }
    }
  ')
done

if (( failed != 0 )); then
  exit 1
fi

echo "All arm64 native libraries are aligned for 16 KB page size."
