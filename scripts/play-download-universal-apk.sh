#!/usr/bin/env bash
# Download Play-signed universal APK for a versionCode.
# Env: SERVICE_ACCOUNT_JSON, VERSION_CODE
# Optional: PACKAGE_NAME, OUT, MAX_TRIES, SLEEP_SECS
set -euo pipefail

PACKAGE_NAME="${PACKAGE_NAME:-net.lifeupapp.lifeup.http}"
OUT="${OUT:-app-release.apk}"
MAX_TRIES="${MAX_TRIES:-20}"
SLEEP_SECS="${SLEEP_SECS:-30}"

: "${SERVICE_ACCOUNT_JSON:?SERVICE_ACCOUNT_JSON is required}"
: "${VERSION_CODE:?VERSION_CODE is required}"

b64url() { base64 -w0 | tr '+/' '-_' | tr -d '='; }

CLIENT_EMAIL=$(printf '%s' "$SERVICE_ACCOUNT_JSON" | jq -r '.client_email')
PRIVATE_KEY=$(printf '%s' "$SERVICE_ACCOUNT_JSON" | jq -r '.private_key')
KEY_FILE=$(mktemp)
trap 'rm -f "$KEY_FILE"' EXIT
printf '%s\n' "$PRIVATE_KEY" > "$KEY_FILE"

SCOPES="https://www.googleapis.com/auth/androidpublisher"
NOW=$(date +%s)
JWT_HEADER=$(printf '%s' '{"alg":"RS256","typ":"JWT"}' | b64url)
JWT_CLAIM=$(printf '%s' "{\"iss\":\"$CLIENT_EMAIL\",\"scope\":\"$SCOPES\",\"aud\":\"https://oauth2.googleapis.com/token\",\"exp\":$((NOW+3600)),\"iat\":$NOW}" | b64url)
JWT_SIG=$(printf '%s' "$JWT_HEADER.$JWT_CLAIM" | openssl dgst -sha256 -sign "$KEY_FILE" | b64url)
ACCESS_TOKEN=$(curl -fsS -X POST "https://oauth2.googleapis.com/token" \
  -d "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=$JWT_HEADER.$JWT_CLAIM.$JWT_SIG" | jq -r '.access_token')

API_BASE="https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${PACKAGE_NAME}/generatedApks/${VERSION_CODE}"

DOWNLOAD_ID=""
for ((i = 1; i <= MAX_TRIES; i++)); do
  HTTP=$(curl -sS -o /tmp/generatedapks.json -w '%{http_code}' \
    "$API_BASE" -H "Authorization: Bearer $ACCESS_TOKEN" || true)
  if [ "$HTTP" = "200" ]; then
    DOWNLOAD_ID=$(jq -r '.generatedApks[0].generatedUniversalApk.downloadId // empty' /tmp/generatedapks.json)
    if [ -n "$DOWNLOAD_ID" ]; then
      echo "Got universal APK downloadId on poll #$i"
      break
    fi
    echo "List 200 but universal APK not ready (poll #$i)"
  else
    echo "generatedApks list HTTP $HTTP (poll #$i)"
  fi
  if [ "$i" -lt "$MAX_TRIES" ]; then
    sleep "$SLEEP_SECS"
  fi
done

if [ -z "$DOWNLOAD_ID" ]; then
  echo "Play-signed universal APK not ready for versionCode=$VERSION_CODE" >&2
  exit 2
fi

curl -fsSL -o "$OUT" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  "${API_BASE}/downloads/${DOWNLOAD_ID}:download?alt=media"
echo "Wrote $OUT"
